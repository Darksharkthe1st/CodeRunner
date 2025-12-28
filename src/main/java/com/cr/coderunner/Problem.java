package com.cr.coderunner;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

public class Problem {
    public static class TestCase {
        public String input;
        public String output;
        @JsonCreator
        public TestCase(@JsonProperty("output") String output, @JsonProperty("input") String input) {
            this.output = output;
            this.input = input;
        }
    }

    public TestCase[] testCases;
    public String description;
    public String name;

    @JsonCreator
    public Problem(@JsonProperty("test cases") TestCase[] testCases, @JsonProperty("description") String description, @JsonProperty("name") String name) {
        this.testCases = testCases;
        this.description = description;
        this.name = name;
    }

    public CodeExecution[] runCases(CodeSubmission codeSubmission) throws IOException, InterruptedException {
        CodeExecution[] executions = new CodeExecution[testCases.length];

        for (int i = 0; i < executions.length; i++) {
            executions[i] = new CodeExecution(codeSubmission, testCases[i].input);
            executions[i].start();
        }

        for (CodeExecution execution : executions) {
            execution.join();
        }

        return executions;
    }

    public boolean validateCases(CodeExecution[] executions) throws IOException, InterruptedException {
        for (CodeExecution execution : executions) {
            if (!execution.success) {
                return false;
            }
        }
        return true;
    }


}
