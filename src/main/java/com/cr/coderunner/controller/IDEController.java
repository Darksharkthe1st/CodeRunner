package com.cr.coderunner.controller;

import com.cr.coderunner.dto.RunResult;
import com.cr.coderunner.model.CodeExecution;
import com.cr.coderunner.model.CodeSubmission;
import com.cr.coderunner.model.UserData;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class IDEController {
    //TODO: Switch from use of userData class to SQL-Based dataset
    private final UserData userData;

    public IDEController(UserData userData) {
        this.userData = userData;
    }

    @GetMapping("/supported")
    public String[] getSupported() {
        return new String[] {"Java", "C"};
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

    @PostMapping("/run")
    public RunResult runSubmission(@RequestBody String input) throws IOException, InterruptedException {
        CodeSubmission latestSubmission = userData.getLastSubmission();
        CodeExecution execution = new CodeExecution(latestSubmission, input);

        execution.start();
        execution.join();

        return new RunResult(execution);
    }

    @GetMapping("/get_template")
    public String getTemplate(@RequestParam String language) {
        return switch (language) {
            case "Java" -> "class test {\n\tpublic static void main(String[] args) {\n\t\t\n\t}\n}";
            case "C", "C++" -> "#include <stdio.h>\n\nint main(int argc, char *argv[]) {\n\t\n}";
            case "Python" -> "if __name__ == \"__main__\":\n\tpass\n";
            default -> throw new IllegalStateException("Unexpected value: " + language);
        };
    }


    //Logging setup (Via web requests)
    private static boolean loggerOn = false;
    private static volatile boolean waiting = false;

    public static void logText(String text) {
        if (!loggerOn)
            return;

        System.out.println("LOG (Press enter to continue): " + text);
        waiting = true;

        //Busy wait until unpaused by postman
        while (waiting) {
            Thread.onSpinWait();
        }

    }

    @PostMapping("/set_logging")
    public void setLogging(@RequestParam boolean loggingOn) {
        loggerOn = loggingOn;
    }

    @PostMapping("/resume")
    public void resume() {
        waiting = false;
    }

}
