package com.cr.coderunner;


import java.io.IOException;

public class Problem {
    public static class TestCase {
        public String input;
        public String output;

        public TestCase(String output, String input) {
            this.output = output;
            this.input = input;
        }
    }

    public TestCase[] testCases;
    public String description;

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
