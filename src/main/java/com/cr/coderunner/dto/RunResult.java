package com.cr.coderunner.dto;

import com.cr.coderunner.model.CodeExecution;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

//TODO: Move to its own file when used by more than one function
public class RunResult {
    public String status; // "RUNNING", "NONEXISTENT", "FINISHED"
    public boolean success;
    public double runtime;
    public String output;
    public String error;
    public String exitStatus;


    @JsonCreator
    public RunResult(@JsonProperty("success") boolean success, @JsonProperty("runtime") double runtime, @JsonProperty("output") String output, @JsonProperty("error") String error, @JsonProperty("exitStatus") String exitStatus, @JsonProperty("status") String status) {
        this.success = success;
        this.runtime = runtime;
        this.output = output;
        this.error = error;
        this.exitStatus = exitStatus;
        this.status = status;
    }

    public RunResult(CodeExecution execution, String status) {
        this.success = execution.success;
        this.runtime = execution.runtime;
        this.output = execution.output;
        this.error = execution.error;
        this.exitStatus = execution.exitStatus;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RunResult runResult = (RunResult) o;
        return success == runResult.success && Integer.compare((int)runtime, (int)runResult.runtime) == 0 && Objects.equals(output, runResult.output) && Objects.equals(error, runResult.error) && Objects.equals(exitStatus, runResult.exitStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, (int)runtime, output, error, exitStatus);
    }

    public String toString() {
        return String.format("status: %s, success: %b, runtime: %f, output: %s, error: %s, exitStatus: %s}",
                status, success, runtime, output, error, exitStatus);
    }


    public String displayStr() {
        return String.format("===status: %s \n===output:\n%s\n===error:\n%s\n===exitStatus:\n%s\n",
                status, output, error, exitStatus);
    }
}