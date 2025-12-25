package com.cr.coderunner;

public class CodeSubmission {
    public String code;
    public boolean success;
    public double runtime;
    //TODO: Add string to represent code language

    public CodeSubmission(String code) {
        this.code = code;
        this.success = false;
        this.runtime = -1;
    }

    public void buildAndRun() {

        //Get the current directory
        String userDir = System.getProperty("user.dir");

        //TODO: create a temporary code file with an appropriate filename

        //TODO: use ProcessBuilder to run code file accordingly (javac xxx)
        ProcessBuilder p = new ProcessBuilder();

        //TODO: use Docker to ensure dev env has all needed build tools

        //TODO: funnel ProcessBuilder outputs into a variable somehow

        //TODO: Add evaluation of code outputs by checking variable equality (start with ints)

        //Set success to true by default for now
        this.success = true;
    }
}
