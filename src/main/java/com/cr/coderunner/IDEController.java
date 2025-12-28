package com.cr.coderunner;

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

    @GetMapping("/run")
    public Map runSubmission() throws IOException, InterruptedException {
        CodeSubmission latestSubmission = userData.getLastSubmission();
        CodeExecution execution = new CodeExecution(latestSubmission, latestSubmission.input);

        execution.start();
        execution.join();

        return Map.of(
                "result", execution.exitStatus,
                "output", execution.output,
                "error", execution.error
        );
    }
    
}
