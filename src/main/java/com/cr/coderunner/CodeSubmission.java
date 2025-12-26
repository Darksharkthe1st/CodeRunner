package com.cr.coderunner;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.InputMismatchException;
import java.util.List;

public class CodeSubmission {
    public String code;
    public String language;
    public boolean success;
    public double runtime;
    public String latestOutput;

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
        File execDir = new File(userDir, "test");

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

        //Create an output file for the code to write into
        File outputFile = new File(execDir, "output.txt");
        if (!outputFile.createNewFile()) {
            System.out.println("Output file already exists; overwriting.");
        }

        //Prepare ProcessBuilder to run code file accordingly (e.g. java xxx.java)
        ProcessBuilder p = new ProcessBuilder();
        p.directory(execDir);
        p.redirectOutput(outputFile);

        //Use different execution methods for different languages
        if (language.equals("Java")) {
            p.command("java", codeFile.getAbsolutePath());
        }

        int count = 0;

        //Run the process, wait until complete
        String status = runProcess(p.start());

        //TODO: use Docker to ensure dev env has all needed build tools

        //TODO: funnel ProcessBuilder outputs into a variable somehow

        //TODO: Add evaluation of code outputs by checking variable equality (start with ints)
        System.out.println("Here is the output: ");
        //Print out latest output for now for testing purposes
        System.out.println(this.latestOutput);

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
        InputStream outputStream = process.getInputStream();
        InputStreamReader outputReader = new InputStreamReader(outputStream, StandardCharsets.UTF_8);
        BufferedReader outputBuffer = new BufferedReader(outputReader);
        StringBuilder outputs = new StringBuilder("");

        String status = "success";
        int count = 0;

        //Run the process until time's up
        while (process.isAlive() && count/10 < TIME_LIMIT_SECS) {
            System.out.println("Running " + count);
            Thread.sleep(100);
            count++;
        }

        //Get the output of the code
        while (true) {
            System.out.println("Reading " + (count++));
            try {
                String line = outputBuffer.readLine();
                if (line != null) {
                    outputs.append(outputBuffer.readLine());
                    outputs.append("\n");
                } else {
                    break;
                }
            } catch (OutOfMemoryError e) {
                status = "Output Limit Exceeded";
                break;
            }
        }

        //Time limit exceeded
        if (count/10 >= TIME_LIMIT_SECS) {
            process.destroyForcibly();
            status = "Time Limit Exceeded.";
        }

        //Show the user the error message if it comes up
        if (!status.equals("success")) {
            outputs.append("\n====");
            outputs.append(status);
        }

        //Save the final output
        this.latestOutput = outputs.toString();

        return status;
    }
}
