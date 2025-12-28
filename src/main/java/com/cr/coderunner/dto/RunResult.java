package com.cr.coderunner.dto;

import com.cr.coderunner.model.CodeExecution;

//TODO: Move to its own file when used by more than one function
public class RunResult {
    public boolean success;
    public double runtime;
    public String output;
    public String error;
    public String exitStatus;

    public RunResult(CodeExecution execution) {
        this.success = execution.success;
        this.runtime = execution.runtime;
        this.output = execution.output;
        this.error = execution.error;
        this.exitStatus = execution.exitStatus;
    }
}