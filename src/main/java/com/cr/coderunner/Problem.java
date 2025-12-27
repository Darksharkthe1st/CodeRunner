package com.cr.coderunner;


public class Problem {
    public class TestCase {
        public String input;
        public String output;

        public TestCase(String output, String input) {
            this.output = output;
            this.input = input;
        }
    }
    public TestCase[] testCases;
    public String description;

    public boolean validateSubmission() {
        return false;
    }
}
