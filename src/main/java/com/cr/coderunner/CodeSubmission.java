package com.cr.coderunner;

public class CodeSubmission {
    public String code;
    public boolean success;
    public double runtime;

    public CodeSubmission(String code) {
        this.code = code;
        this.success = false;
        this.runtime = -1;
    }
}
