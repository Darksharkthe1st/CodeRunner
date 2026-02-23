package com.cr.coderunner.controller;

import com.cr.coderunner.dto.UserChat;
import com.cr.coderunner.service.GeminiService;
import org.apache.catalina.User;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/llm")
public class LLMController {
    private final GeminiService geminiService;

    public LLMController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/message")
    public String messagePrompt(@RequestBody UserChat chat) {
        System.out.println(Arrays.toString(chat.getMessages().toArray()));
        return geminiService.messageModel(chat.getMessages());
    }
}
