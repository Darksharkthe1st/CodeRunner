package com.cr.coderunner.controller;

import com.cr.coderunner.dto.RunResult;
import com.cr.coderunner.model.CodeExecution;
import com.cr.coderunner.model.CodeSubmission;
import com.cr.coderunner.model.UserData;
import com.cr.coderunner.service.CodeExecutionService;
import io.micrometer.core.annotation.Timed;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class IDEController {
    //TODO: Switch from use of userData class to SQL-Based dataset
    private final UserData userData;
    private final CodeExecutionService executionService;

    private static final String[] supported_langs = new String[] {"Java", "C", "Python"};

    public IDEController(UserData userData, CodeExecutionService executionService) {
        this.userData = userData;
        this.executionService = executionService;
    }

    @GetMapping("/supported")
    public String[] getSupported() {
        return supported_langs;
    }

    @Timed(value = "greeting.time", description = "Time taken to return greeting")
    @GetMapping("/")
    public String index() {
        return "Welcome to CodeRunner! Go to Route /submit to upload code and /check to view it.";
    }

    //Submit to the executor, returns the ID used for tracking it
    @Timed(value = "code.submit.time", description = "Time to submit code")
    @PostMapping("/submit")
    public String postSubmission(@RequestBody CodeSubmission codeSubmission) throws InterruptedException {
        return executionService.execute(new CodeExecution(codeSubmission));
    }

    @Timed(value = "code.check.time")
    @PostMapping("/check")
    public RunResult checkSubmission(@RequestBody String execID) {
        return executionService.checkExecution(execID);
    }

    @GetMapping("/get_template")
    public String getTemplate(@RequestParam String language) {
        return switch (language) {
            case "Java" -> "class test {\n\tpublic static void main(String[] args) {\n\t\t\n\t}\n}";
            case "C", "C++" -> "#include <stdio.h>\n\nint main(int argc, char *argv[]) {\n\t\n\treturn 0;\n}";
            case "Python" -> "if __name__ == \"__main__\":\n\tpass\n";
            default -> throw new IllegalStateException("Unexpected value: " + language);
        };
    }

    @GetMapping("/pull")
    public String pullImages() throws IOException, InterruptedException {
        //Builder and images to be pulled
        ProcessBuilder builder = new ProcessBuilder();
        String[] images = new String[] {"alpine:latest", "python:3.12-alpine", "eclipse-temurin:21-alpine"};
        //Pull every image needed
        for (String image : images) {
            //Pull cmd
            builder.command("docker", "pull", image);
            //Start and wait for the process
            builder.inheritIO();
            Process pulling = builder.start();
            pulling.waitFor();
            //Fail if the process couldn't exit
            if (pulling.isAlive()) return "Failure";
        }

        //Success if all pulls exited.
        return "Success!";
    }

    @GetMapping("/check_queue")
    public String checkQueue() throws IOException, InterruptedException {
        return executionService.listExecutions();
    }


    //Logging setup (Via web requests)
    private static boolean loggerOn = false;
    private static boolean pauserOn = false;
    private static volatile boolean waiting = false;

    public static void logText(String text) {
        if (!loggerOn)
            return;

        System.out.println("LOG (Press enter to continue): " + text);
        waiting = true;
        if (pauserOn) {
            //Busy wait until unpaused by postman
            while (waiting) {
                Thread.onSpinWait();
            }
        }

    }

//    @PostMapping("/set_logging")
//    public void setLogging(@RequestParam boolean loggingOn) {
//        loggerOn = loggingOn;
//    }
//
//    @PostMapping("/set_pausing")
//    public void setPausingOn(@RequestParam boolean pausingOn) {
//        pauserOn = pausingOn;
//    }
//
//    @PostMapping("/resume")
//    public void resume() {
//        waiting = false;
//    }

}
