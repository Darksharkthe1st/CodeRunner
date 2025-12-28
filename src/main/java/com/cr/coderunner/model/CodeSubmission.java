package com.cr.coderunner.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.InputMismatchException;
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
            case "Python" -> ".py";
            case "C" -> ".c";
            case "CPP" -> ".cpp";
            default -> null;
        };
    }

    public void build(ProcessBuilder processBuilder, CodeExecution exec) {
        //Get the current directory
        File userDir = new File(System.getProperty("user.dir"));
        File execDir = new File(userDir, ".test");

        //Check if the extension is valid and save it
        String extension = getExtensionByLang(this.language);
        if (extension == null)
            throw new InputMismatchException(this.language + " is not a supported language in CodeRunner.");

        //Make a new directory if needed
        if (execDir.mkdir()) {
            System.out.println("testing directory not detected; new directory created.");
        }

        //Files for code/input to be pulled from
        File codeFile, inputFile;

        try {
            //Create a temporary code file with an appropriate filename
            codeFile = new File(execDir, "file" + extension);
            if (!codeFile.createNewFile()) {
                System.out.println("Code file already exists; overwriting.");
            }

            //Create a temporary input file with an appropriate filename
            inputFile = new File(execDir, "input.txt");
            if (!inputFile.createNewFile()) {
                System.out.println("Input file already exists; overwriting.");
            }

            //Overwrite existing text files
            Files.writeString(codeFile.toPath(),  this.code,  StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.writeString(inputFile.toPath(), exec.input, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            //Catch exception if files cannot be written to
        } catch (IOException e) {
            exec.exitStatus += "could not write to code file.";
            return;
        }

        processBuilder.redirectInput(inputFile);
        processBuilder.directory(execDir);

        //Use different execution methods for different languages
        if (language.equals("Java")) {
            processBuilder.command("java", codeFile.getAbsolutePath());
        }
    }

    /** runs the code provided, w/ input from exec and outputting into exec
     * @param exec contains input text and will be filled with status, output, runtime information and more.
     */
    public void run(CodeExecution exec) {
        //Prepare ProcessBuilder to run code file accordingly (e.g. java xxx.java)
        ProcessBuilder processBuilder = new ProcessBuilder();
        Process process;

        //Blank values for now:
        exec.success = true;
        exec.runtime = -1;
        exec.output = "";
        exec.error = "";
        exec.exitStatus = "";

        synchronized (buildLock) {
            //Build code (AKA write data to files);
            build(processBuilder, exec);

            //If build failed, stop running
            if (!exec.success) {
                return;
            }

            //Try to start the process
            try {
                process = processBuilder.start();
            } catch (IOException e) {
                exec.exitStatus += "could not start program.";
                return;
            }
        }

        //Run the process, wait until complete
        try {
            runProcess(process, exec);

            //Catch internal errors in runProcess code in case any buffers fail at closing/threads can't join
        } catch (IOException | InterruptedException e) {
            exec.exitStatus += "could not read stdout/stderr.";
            return;
        } catch (RuntimeException e) {
            //Assume that the exitStatus was already changed.
            return;
        }

        //TODO: use Docker to ensure dev env has all needed build tools

        //Print out latest output for now for testing purposes
        System.out.println("OUT:\n" + exec.output);
        //Print out latest error for testing purposes
        System.out.println("ERR:\n" + exec.error);

        //Set success to true/false depending on status
        exec.success = exec.exitStatus.equals("success");
    }

    /** Runs protected process with Time and Output Limits. Returns status depending on if those limits are hit
     * @param process Process to be run
     * @param exec Stores process outputs and status info
     * @throws IOException if program output cannot be accessed
     * @throws InterruptedException for thread.sleep calls on the main process (Spring Boot server)
     */
    public void runProcess(Process process, CodeExecution exec) throws InterruptedException, IOException, RuntimeException {
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
        process.waitFor(TIME_LIMIT_SECS, TimeUnit.SECONDS);

        //Time limit exceeded
        if (process.isAlive()) {
            exec.exitStatus += "Time Limit Exceeded.\n";
        }

        try {
            //Kill the process no matter what to avoid any rogue processes
            process.destroy();
        } catch (IllegalThreadStateException e) {
            //If the process cannot be destroyed normally, forcibly kill it
            process.destroyForcibly();
        }

        //If the process exited improperly and an error wasn't caught, note it
        if (process.exitValue() != 0 && exec.exitStatus.equals("success")) {
            exec.exitStatus += "Program exited with incorrect return value: " + process.exitValue() + "\n";
        }

        //Stop reading input/error data, close buffers
        readOut.join();
        readErr.join();
        outputBuffer.close();
        errorBuffer.close();

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
