package com.cr.coderunner;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IDEController {

    @GetMapping("/")
    public String index() {
        return "Welcome to CodeRunner! Go to Route /submit to upload code.";
    }

    @GetMapping("/check")
    public CodeSubmission getSubmission() {
        return new CodeSubmission("no code written");
    }
}
