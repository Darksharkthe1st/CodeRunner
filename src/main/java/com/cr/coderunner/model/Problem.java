package com.cr.coderunner.model;


import com.cr.coderunner.dto.RunResult;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.List;

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

    public CodeExecution[] runCases(CodeSubmission codeSubmission) throws InterruptedException {
        CodeExecution[] executions = new CodeExecution[testCases.length];

        for (int i = 0; i < executions.length; i++) {
            executions[i] = new CodeExecution(codeSubmission/*, testCases[i].input*/);
            executions[i].start();
        }

        for (int i = 0; i < executions.length; i++) {
            executions[i].join();
            if (!executions[i].output.equals(testCases[i].output)) {
                executions[i].success = false;

                //Empty the output if it indicates success
                if (executions[i].exitStatus.equals("success"))
                    executions[i].exitStatus = "";

                //Indicate failure to pass test case.
                executions[i].exitStatus += "Failure: Incorrect output.\n";
            }
        }

        return executions;
    }

    public static List<RunResult> simplifyCases(CodeExecution[] cases) {
        RunResult[] results = new RunResult[cases.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = new RunResult(cases[i], "FINISHED");
        }
        return List.of(results);
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
