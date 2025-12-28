package com.cr.coderunner;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.InputMismatchException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CodeSubmission {
    public static final int TIME_LIMIT_SECS = 10;

    public final String code;
    public final String language;
    public final String input;

    public CodeSubmission(String code, String language, String input) {
        this.code = code;
        this.language = language;
        this.input = input;
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

    public void run() {
        run(new CodeExecution(
                this,
                this.input
        ));
    }

    public void run(CodeExecution exec) {
        //TODO: Remove in the future; temporary
        if (exec.input == null) {
            exec.input = this.input;
        }

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
            exec.success = false;
            exec.runtime = -1;
            exec.output = "";
            exec.error = "";
            exec.exitStatus = "could not write to code file.";
            return;
        }

        //Prepare ProcessBuilder to run code file accordingly (e.g. java xxx.java)
        ProcessBuilder p = new ProcessBuilder();
        p.redirectInput(inputFile);
        p.directory(execDir);

        //Use different execution methods for different languages
        if (language.equals("Java")) {
            p.command("java", codeFile.getAbsolutePath());
        }

        //Run the process, wait until complete
        String status = null;
        try {
            runProcess(p.start(), exec);

            //Catch internal errors in runProcess code in case any buffers fail at closing/threads can't join
        } catch (IOException | InterruptedException e) {
            exec.success = false;
            exec.runtime = 0;
            exec.output = "";
            exec.error = "";
            exec.exitStatus += "could not read stdout/stderr.";
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
    public void runProcess(Process process, CodeExecution exec) throws InterruptedException, IOException {
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
        }

        //Save the final output
        exec.output = outputs.toString();
        exec.error = errors.toString();
    }
}
