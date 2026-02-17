package com.cr.coderunner.controller;

import com.cr.coderunner.service.GeminiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/llm")
public class LLMController {
    private final GeminiService geminiService;

    public LLMController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/ask")
    public String askLLM(@RequestBody String prompt) {
        return geminiService.askGemini(prompt);
    }
}
