package com.cr.coderunner.service;

import com.cr.coderunner.dto.RunResult;
import com.cr.coderunner.model.CodeExecution;
import com.cr.coderunner.model.CodeSubmission;
import dev.langchain4j.agent.tool.Tool;
import io.micrometer.core.annotation.Timed;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Objects;

@Component
public class CodeExecutionTools {
    private final CodeExecutionService executionService;

    public CodeExecutionTools(CodeExecutionService executionService) {
        this.executionService = executionService;
    }

    @Tool("Execute code and return the output. Use this to test or verify code, this does NOT modify the user's code (you need to tell them the new code so they can copy it themselves).")
    public String executeCode(String code, String language, String input) {
        System.out.println("LLM Executing code " + code + " with language " + language);
        language = language.toLowerCase();
        CodeSubmission submission = new CodeSubmission(code, language, "ONE", input);

        String UUID = executionService.execute(new CodeExecution(submission));
        RunResult result = executionService.checkExecution(UUID);
        int count = 0;

        while (result.status.equals("RUNNING") && count < 1200) {
            System.out.println(result.toString());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            count += 1;
            result = executionService.checkExecution(UUID);
        }
        System.out.println(result.displayStr());
        return result.displayStr();
    }

//    //Submit to the executor, returns the ID used for tracking it
//    @Timed(value = "code.submit.time", description = "Time to submit code")
//    @PostMapping("/submit")
//    public String postSubmission(@RequestBody CodeSubmission codeSubmission) throws InterruptedException {
//        return ;
//    }

//    @Timed(value = "code.check.time")
//    @PostMapping("/check")
//    public RunResult checkSubmission(@RequestBody String execID) {
//        return executionService.checkExecution(execID);
//    }
}
