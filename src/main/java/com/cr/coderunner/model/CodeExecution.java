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
    public long completedAt;

    public CodeExecution(CodeSubmission codeSubmission /*, String input*/) {
        this.codeSubmission = codeSubmission;
//        this.input = input;
        done = false;
        completedAt = 0;
    }

    @Override
    public void run() {
        codeSubmission.run(this);
        done = true;
        completedAt = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return completedAt != 0 && System.currentTimeMillis() - completedAt > 60.00;
    }
}
