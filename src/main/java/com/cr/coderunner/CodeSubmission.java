package com.cr.coderunner;

import java.io.File;
import java.io.IOException;
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

    public void buildAndRun() throws IOException, InterruptedException {

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
        Process process = p.start();
        while (process.isAlive() && count/10 < TIME_LIMIT_SECS) {
            Thread.sleep(100);
            count++;
        }

        //Time limit exceeded
        if (count/10 >= TIME_LIMIT_SECS) {
            process.destroyForcibly();
            this.latestOutput = "Time Limit Exceeded.";
            this.success = false;
            return;
        }
        //TODO: use Docker to ensure dev env has all needed build tools

        //TODO: funnel ProcessBuilder outputs into a variable somehow

        //TODO: Add evaluation of code outputs by checking variable equality (start with ints)
        System.out.println("Here is the output: ");
        List<String> fileLines = Files.readAllLines(outputFile.toPath());

        //Use string builder to convert output list of strings into readable format
        StringBuilder fullOutput = new StringBuilder("");
        for (String line : fileLines) {
            fullOutput.append(line);
            fullOutput.append("\n");
        }

        //Print out full output for now for testing purposes
        System.out.println(fullOutput);

        this.latestOutput = fullOutput.toString();

        //Set success to true by default for now
        this.success = true;
    }
}
