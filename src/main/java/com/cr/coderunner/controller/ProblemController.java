package com.cr.coderunner.controller;

import com.cr.coderunner.dto.ManyResults;
import com.cr.coderunner.dto.RunResult;
import com.cr.coderunner.model.CodeSubmission;
import com.cr.coderunner.model.Problem;
import com.cr.coderunner.model.UserData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class ProblemController {
    //TODO: Switch from use of userData class to SQL-Based dataset
    private final UserData userData;

    public ProblemController(UserData userData) {
        this.userData = userData;
    }

    @PostMapping("/add_problem")
    public void addProblem(@RequestBody Problem problem) {
        userData.problems.put(problem.name, problem);
    }

    @GetMapping("/get_problems")
    public Map<String, Problem> getProblems() {
        return userData.problems;
    }

    @PostMapping("/try_problem")
    public ManyResults tryProblem(@RequestBody CodeSubmission submission) throws InterruptedException {
        Problem problem = userData.problems.get(submission.problemName);
        return new ManyResults(Problem.simplifyCases(problem.runCases(submission)));
    }
}
