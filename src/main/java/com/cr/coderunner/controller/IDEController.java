package com.cr.coderunner.controller;

import com.cr.coderunner.dto.RunResult;
import com.cr.coderunner.model.CodeExecution;
import com.cr.coderunner.model.CodeSubmission;
import com.cr.coderunner.model.Problem;
import com.cr.coderunner.model.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
public class IDEController {
    //TODO: Switch from use of userData class to SQL-Based dataset
    private final UserData userData;

    public IDEController(UserData userData) {
        this.userData = userData;
    }

    @GetMapping("/")
    public String index() {
        return "Welcome to CodeRunner! Go to Route /submit to upload code and /check to view it.";
    }

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

    @PostMapping("/run")
    public RunResult runSubmission(@RequestBody String input) throws IOException, InterruptedException {
        CodeSubmission latestSubmission = userData.getLastSubmission();
        CodeExecution execution = new CodeExecution(latestSubmission, input);

        execution.start();
        execution.join();

        return new RunResult(execution);
    }
    
}
