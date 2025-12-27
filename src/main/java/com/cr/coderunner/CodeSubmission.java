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
    public boolean success;
    public double runtime;
    public String latestOutput;
    public String latestError;

    public CodeSubmission(String code, String language, String input) {
        this.code = code;
        this.language = language;
        this.input = input;
        this.success = false;
        this.runtime = -1;
        this.latestOutput = null;
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

    public String buildAndRun(String inputs) throws IOException, InterruptedException {

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

        //Create a temporary code file with an appropriate filename
        File codeFile = new File(execDir, "file" + extension);
        if (!codeFile.createNewFile()) {
            System.out.println("Code file already exists; overwriting.");
        }

        //Create a temporary input file with an appropriate filename
        File inputFile = new File(execDir, "input.txt");
        if (!inputFile.createNewFile()) {
            System.out.println("Input file already exists; overwriting.");
        }

        //Overwrite existing text files
        Files.writeString(codeFile.toPath(),this.code, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.writeString(inputFile.toPath(), inputs, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        //Prepare ProcessBuilder to run code file accordingly (e.g. java xxx.java)
        ProcessBuilder p = new ProcessBuilder();
        p.redirectInput(inputFile);
        p.directory(execDir);

        //Use different execution methods for different languages
        if (language.equals("Java")) {
            p.command("java", codeFile.getAbsolutePath());
        }

        //Run the process, wait until complete
        String status = runProcess(p.start());

        //TODO: use Docker to ensure dev env has all needed build tools

        //TODO: Add evaluation of code outputs by checking variable equality (start with ints)

        //Print out latest output for now for testing purposes
        System.out.println("OUT:\n" + this.latestOutput);
        //Print out latest error for testing purposes
        System.out.println("ERR:\n" + this.latestError);

        //Set success to true/false depending on status
        this.success = status.equals("success");

        return status;
    }

    /** Runs protected process with Time and Output Limits. Returns status depending on if those limits are hit
     * @param process Process to be run
     * @return "success" for completion, "Time Limit Exceeded" for surpassing CodeSubmission.TIME_LIMIT_SECS, "Output Limit Exceeded" for surpassing max String size in output
     * @throws IOException if program output cannot be accessed
     * @throws InterruptedException for thread.sleep calls on the main process (Spring Boot server)
     */
    public String runProcess(Process process) throws IOException, InterruptedException {
        //Use BufferedReader/StringBuilder to store outputs
        BufferedReader errorBuffer = process.errorReader();
        BufferedReader outputBuffer = process.inputReader();
        StringBuilder outputs = new StringBuilder();
        StringBuilder errors = new StringBuilder();

        latestError = "success";

        //Thread to read out the buffer values for output
        Thread readOut = new Thread() {
            public void run() {
                String outLine = "";
                while (outLine != null) {
                    try {
                        outLine = outputBuffer.readLine();
                        if (outLine != null) {
                            outputs.append(outLine); outputs.append("\n");
                        }
                    } catch (OutOfMemoryError | IOException e) {
                        latestError = "Output Limit Exceeded";
                        break;
                    }
                }
            }
        };

        //Thread to read out the buffer values for input
        Thread readErr = new Thread() {
            public void run() {
                String errLine = "";
                while (errLine != null) {
                    try {
                        errLine = errorBuffer.readLine();
                        if (errLine != null) {
                            errors.append(errLine); errors.append("\n");
                        }
                    } catch (OutOfMemoryError | IOException e) {
                        latestError = "Error Limit Exceeded";
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
            latestError = "Time Limit Exceeded.";
        }

        try {
            //Kill the process no matter what to avoid any rogue processes
            process.destroy();
        } catch (IllegalThreadStateException e) {
            //If the process cannot be destroyed normally, forcibly kill it
            process.destroyForcibly();
        }

        //If the process exited improperly and an error wasn't caught, note it
        if (process.exitValue() != 0 && latestError.equals("success")) {
            latestError = "Program exited with incorrect return value: " + process.exitValue();
        }

        //Stop reading input/error data, close buffers
        readOut.join();
        readErr.join();
        outputBuffer.close();
        errorBuffer.close();

        //Store the error/success values to status
        String status = latestError;

        //Show the user the error message if it comes up
        if (!status.equals("success")) {
            outputs.append("\n====");
            outputs.append(status);
        }

        //Save the final output
        this.latestOutput = outputs.toString();
        this.latestError = errors.toString();

        return status;
    }
}
