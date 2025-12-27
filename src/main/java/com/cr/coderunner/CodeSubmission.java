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
    public String code;
    public String language;
    public boolean success;
    public double runtime;
    public String latestOutput;
    public String latestError;

    public static final int TIME_LIMIT_SECS = 10;

    //TODO: Add string to represent code language

    public CodeSubmission(String code, String language) {
        this.code = code;
        this.language = language;
        this.success = false;
        this.runtime = -1;
        this.latestOutput = null;
    }

    public String getExtensionByLang(String language) {
        if (language.equals("Java")) {
            return ".java";
        } else {
            return "none found";
        }
    }

    public String buildAndRun() throws IOException, InterruptedException {

        //Get the current directory
        File userDir = new File(System.getProperty("user.dir"));
        File execDir = new File(userDir, ".test");

        //Check if the extension is valid and save it
        String extension = getExtensionByLang(this.language);
        if (extension.equals("none found")) {
            throw new InputMismatchException(this.language + " is not a supported language in CodeRunner.");
        }

        //Make a new directory if needed
        if (execDir.mkdir()) {
            System.out.println("testing directory not detected; new directory created.");
        }

        //Create a temporary code file with an appropriate filename
        File codeFile = new File(execDir, "file" + extension);
        if (!codeFile.createNewFile()) {
            System.out.println("Code file already exists; overwriting.");
        }

        //Overwrite existing text file
        Files.writeString(codeFile.toPath(),this.code, StandardOpenOption.WRITE);

        //TODO: Bug fix needed--overwrites file, but does not remove prior text. Find a way to empty before writing

        //Prepare ProcessBuilder to run code file accordingly (e.g. java xxx.java)
        ProcessBuilder p = new ProcessBuilder();
        p.directory(execDir);

        //Use different execution methods for different languages
        if (language.equals("Java")) {
            p.command("java", codeFile.getAbsolutePath());
        }
        //Run the process, wait until complete
        String status = runProcess(p.start());

        //TODO: use Docker to ensure dev env has all needed build tools

        //TODO: funnel ProcessBuilder outputs into a variable somehow

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
     * @param process Process to be ran
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
        int count = 0;

        //Thread to read out the buffer values
        Thread readBuf = new Thread() {
            public void run() {
                while (true) {
                    try {
                        String outLine = outputBuffer.readLine();
                        String errLine = errorBuffer.readLine();
                        if (outLine != null) {
                            outputs.append(outLine); outputs.append("\n");
                        }
                        if (errLine != null) {
                            outputs.append(errLine); outputs.append("\n");
                        }
                    } catch (OutOfMemoryError | IOException e) {
                        latestError = "Output Limit Exceeded";
                        break;
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };

        //Begin reading stdout
        readBuf.start();

        //wait for the process to finish
        process.waitFor(TIME_LIMIT_SECS, TimeUnit.SECONDS);

        //Time limit exceeded
        if (process.isAlive()) {
            latestError = "Time Limit Exceeded.";
        }

        //TODO: Add checkers for if program exited with error, read the error buffer in that case.

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

        readBuf.interrupt();

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
