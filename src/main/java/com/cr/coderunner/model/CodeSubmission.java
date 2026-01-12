package com.cr.coderunner.model;

import com.cr.coderunner.controller.IDEController;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.tomcat.util.http.fileupload.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CodeSubmission {
    public static final int TIME_LIMIT_SECS = 10;
    //Shared object used for locking all build operations (so files aren't misread)
    public static final Object buildLock = new Object();

    public final String code;
    public final String language;
    public final String problemName;

    @JsonCreator
    public CodeSubmission(@JsonProperty(value = "code", required = true) String code, @JsonProperty(value = "language", required = true) String language, @JsonProperty(value = "problem", required = true) String problemName) {
        if (code == null || language == null || problemName == null) {
            throw new IllegalArgumentException("NULL Parameters. Required fields: 'code', 'language', 'input', 'problem'.");
        }
        this.code = code;
        this.language = language;
        this.problemName = problemName;
    }

    public String getExtensionByLang(String language) {
        return switch (language) {
            case "Java" -> ".java";
            case "C" -> ".c";
            case "Python" -> ".py";
            default -> null;
        };
    }

    public List<String> getCommandByFiles(String language, File codeFile, File dirFile, File inputFile) {
        List<String> cmds = new java.util.ArrayList<>(List.of("docker", "run", "--name", dirFile.getName(), "--pids-limit=64", "--memory=256m", "--cpus=0.5", "--rm", "-v", dirFile.getAbsolutePath() + ":/sandbox"));
        cmds.addAll(switch (language) {
            case "C" -> List.of("alpine:latest", "sh", "-c", "apk add --no-cache gcc musl-dev > none.txt && " + "gcc sandbox/" + codeFile.getName() + " -o sandbox/main && ./sandbox/main < sandbox/" + inputFile.getName());
            case "Python" -> List.of("python:3.12-alpine", "sh", "-c", "python3 sandbox/" + codeFile.getName() + " < sandbox/" + inputFile.getName());
            case "Java" -> List.of("eclipse-temurin:21-alpine", "sh", "-c", "java sandbox/" + codeFile.getName() + " < sandbox/" + inputFile.getName());
            default -> List.of("FAILURE");
        });
        if (cmds.getLast().equals("FAILURE")) {
            return null;
        } else {
            return cmds;
        }
    }

    public File build(ProcessBuilder processBuilder, CodeExecution exec) {
        //Get the current directory
        File userDir = new File(System.getProperty("user.dir"));
        File execDir = new File(userDir, ".test");

        //Check if the extension is valid and save it
        String extension = getExtensionByLang(this.language);
        if (extension == null)
            throw new InputMismatchException(this.language + " is not a supported language in CodeRunner.");

        //Make a new directory if needed
        if (execDir.mkdir())
            IDEController.logText("testing directory not detected; new directory created.");

        //Files for code/input to be pulled from
        File dirFile, codeFile, inputFile;

        try {
            //Create a temporary directory to store the code
            Path tempDir = Files.createTempDirectory(execDir.toPath(),"run");

            //Create temporary code/input files with an appropriate filename
            codeFile = Files.createTempFile(tempDir,"code-", extension).toFile();
            inputFile = Files.createTempFile(tempDir,"input-", ".txt").toFile();
            dirFile = new File(tempDir.toUri());

            IDEController.logText("Files made.");

            //Ensure files are temporary only
            codeFile.deleteOnExit();
            inputFile.deleteOnExit();
            dirFile.deleteOnExit();

            //Overwrite existing text files
            Files.writeString(codeFile.toPath(),  this.code,  StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.writeString(inputFile.toPath(), exec.input, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            //Catch exception if files cannot be written to
        } catch (IOException e) {
            exec.exitStatus += "could not write to code file.";
            return null;
        }

//        processBuilder.redirectInput(inputFile);
        processBuilder.directory(execDir);

        IDEController.logText("Running code on enter.");
        //Run different execution methods for different languages
        processBuilder.command(getCommandByFiles(language, codeFile, dirFile, inputFile));

        return dirFile;
    }

    /** runs the code provided, w/ input from exec and outputting into exec
     * @param exec contains input text and will be filled with status, output, runtime information and more.
     */
    public void run(CodeExecution exec) {
        //Prepare ProcessBuilder to run code file accordingly (e.g. java xxx.java)
        ProcessBuilder processBuilder = new ProcessBuilder();
        Process process;

        //Blank values for now:
        exec.success = false;
        exec.runtime = -1;
        exec.output = "";
        exec.error = "";
        exec.exitStatus = "";

        //Build code (AKA write data to files);
        File dirFile = build(processBuilder, exec);

        //If build failed, stop running
        if (dirFile == null) {
            return;
        }

        //Try to start the process
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            closeRun(dirFile, exec, "could not start program.");
            return;
        }

        //TODO: use Docker to ensure dev env has all needed build tools

        //Run the process, wait until complete
        runProcess(process, processBuilder, exec, language, dirFile);
        closeRun(dirFile, exec, "");


    }

    public void closeRun(File dirFile, CodeExecution exec, String newStatus) {
        exec.exitStatus += newStatus;
        //TODO: Remove unnecessary log statements here
        //Print out latest output for now for testing purposes
        IDEController.logText("====OUT:\n" + exec.output);
        //Print out latest error for testing purposes
        IDEController.logText("====ERR:\n" + exec.error);

        //Delete temporary files, if possible
        try {
            FileUtils.cleanDirectory(dirFile);
        } catch (IOException e) {
            exec.exitStatus += "code files failed to delete; terminating";
            e.printStackTrace();
            return;
        }

        try {
            Files.delete(dirFile.toPath());
        } catch (IOException e) {
            exec.exitStatus += "Failed to delete temp directory; System issues.";
            return;
        }

        //Set success to true/false depending on status
        exec.success = exec.exitStatus.isEmpty();
        if (exec.success)
            exec.exitStatus = "success";
    }

    /** Runs protected process with Time and Output Limits. Returns status depending on if those limits are hit
     * @param process Process to be run
     * @param exec Stores process outputs and status info
     * @throws IOException if program output cannot be accessed
     * @throws InterruptedException for thread.sleep calls on the main process (Spring Boot server)
     */
    public void runProcess(Process process, ProcessBuilder builder, CodeExecution exec, String language, File dirFile) {
        //Use BufferedReader/StringBuilder to store outputs
        BufferedReader errorBuffer = process.errorReader();
        BufferedReader outputBuffer = process.inputReader();
        StringBuilder outputs = new StringBuilder();
        StringBuilder errors = new StringBuilder();


        exec.exitStatus = "";

        //Thread to read out the buffer values for output
        Thread readOut = new Thread() {
            public void run() {
                String outLine = "";
                //While there's more to be read:
                while (outLine != null) {
                    try {
                        //Read from the buffer only if not null
                        outLine = outputBuffer.readLine();
                        if (outLine != null) {
                            outputs.append(outLine); outputs.append("\n");
                        }
                    } catch (OutOfMemoryError | IOException e) {
                        //Notify user if reading failed
                        exec.exitStatus += "Output Limit Exceeded\n";
                        break;
                    }
                }
            }
        };

        //Thread to read out the buffer values for input
        Thread readErr = new Thread() {
            public void run() {
                String errLine = "";
                //While there's more to be read:
                while (errLine != null) {
                    try {
                        //Read from the buffer only if not null
                        errLine = errorBuffer.readLine();
                        if (errLine != null) {
                            errors.append(errLine); errors.append("\n");
                        }
                    } catch (OutOfMemoryError | IOException e) {
                        //Notify user if reading failed
                        exec.exitStatus += "Error Limit Exceeded\n";
                        break;
                    }
                }
            }
        };

        //Begin reading stdout
        readOut.start();
        readErr.start();

        //wait for the process to finish
        try {
            process.waitFor(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            exec.exitStatus += "Failed to poll program\n";
        }

        //Time limit exceeded
        if (process.isAlive()) {
            exec.exitStatus += "Time Limit Exceeded.\n";
        }

        try {
            // Stop and remove the container by name (dirFile => container name), wait until fully removed
            builder.command("docker", "rm", "-f", dirFile.getName());
            Process removal = builder.start();
            removal.waitFor(); // This blocks until removal completes

            if (removal.isAlive()) {
                exec.exitStatus += "Failed to close docker container\n";
            }
            IDEController.logText("Waited for thread.");
        } catch (IOException | InterruptedException e) {
            exec.exitStatus += "Code failed to exit.\n";
            IDEController.logText("Failure here");
            e.printStackTrace();
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        IDEController.logText("Processes killed.");

        //If the process exited improperly and an error wasn't caught, note it
        if (process.isAlive()) {
            exec.exitStatus += "Process has not exited properly.\n";
        } else if (process.exitValue() != 0 && exec.exitStatus.equals("success")) {
            exec.exitStatus += "Program exited with incorrect return value: " + process.exitValue() + "\n";
        }

        //Stop reading input/error data, close buffers
        try {
            readOut.join();
            readErr.join();
            outputBuffer.close();
            errorBuffer.close();
        } catch (InterruptedException | IOException e) {
            exec.exitStatus += "Failed to read stdout/stderr.\n";
        }

        //Show the user the error message if it comes up
        if (!exec.exitStatus.isEmpty()) {
            outputs.append("\n====ERROR(S):");
            outputs.append(exec.exitStatus);
            throw new RuntimeException("Failure: " + exec.exitStatus);
        }

        //Save the final output
        exec.output = outputs.toString();
        exec.error = errors.toString();
    }
}
