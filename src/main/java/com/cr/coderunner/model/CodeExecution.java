package com.cr.coderunner.model;

public class CodeExecution extends Thread {
    public CodeSubmission codeSubmission;
//    public String input;

    public boolean success;
    public double runtime;
    public String output;
    public String error;
    public String exitStatus;
    public boolean done;

    public CodeExecution(CodeSubmission codeSubmission /*, String input*/) {
        this.codeSubmission = codeSubmission;
//        this.input = input;
        done = false;
    }

    @Override
    public void run() {
        codeSubmission.run(this);
        done = true;
    }
}
