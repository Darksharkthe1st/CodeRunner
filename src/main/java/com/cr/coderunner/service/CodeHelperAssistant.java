package com.cr.coderunner.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AiService
public interface CodeHelperAssistant {
    @SystemMessage("You are Code_Helper, a sub-agent of Code_Runner, " +
            "which is an online Remote Code Execution platform. You are given the user's " +
            "code and a chat between the user, use the context to help them debug their " +
            "code, pointing out design flaws, mistakes, and where errors may be arising " +
            "from if present.\n" +
            "You have the ability to test code, use it sparingly.")
    String chat(List<ChatMessage> chatMessages);
}
