package com.cr.coderunner;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
public class IDEController {

    @GetMapping("/")
    public String index() {
        return "Welcome to CodeRunner! Go to Route /submit to upload code and /check to view it.";
    }

    //Using simple field injection to create bean, autowired
    @Autowired
    public UserData userData;

    //Created sample get mapping for testing purposes
    @GetMapping("/check")
    public CodeSubmission checkSubmission() {
        return userData.getLastSubmission();
    }

    //Created sample post mapping for testing purposes
    @PostMapping("/submit")
    public void postSubmission(@RequestBody CodeSubmission codeSubmission) {
        userData.addAttempt(codeSubmission);
    }

    @PostMapping("/add_problem")
    public void addProblem(@RequestBody Problem problem) {
        userData.problems.put(problem.name, problem);
    }

    @GetMapping("/get_problems")
    public Map<String, Problem> getProblems() {
        return userData.problems;
    }

    //TODO: Move to its own file when used by more than one function
    public static class RunResult {
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

    @PostMapping("/run")
    public RunResult runSubmission(@RequestBody String input) throws IOException, InterruptedException {
        CodeSubmission latestSubmission = userData.getLastSubmission();
        CodeExecution execution = new CodeExecution(latestSubmission, input);

        execution.start();
        execution.join();

        return new RunResult(execution);
    }
    
}
