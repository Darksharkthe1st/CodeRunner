package com.cr.coderunner.service;

import com.cr.coderunner.dto.ChatBlock;
import com.cr.coderunner.dto.UserChat;
//import com.google.genai.Client;
//import com.google.genai.types.*;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GeminiService {
//    private final Client client;
//    private final GoogleAiGeminiChatModel geminiChatModel;
//    private final String modelName = "gemini-2.5-flash-lite";
    private final CodeHelperAssistant assistant;

    public GeminiService(CodeHelperAssistant assistant) {
        this.assistant = assistant;
    }

//    public GeminiService(@Value("${gemini.api.key}") String apiKey) {
//        geminiChatModel = GoogleAiGeminiChatModel.builder()
//                .modelName(modelName)
//                .apiKey(apiKey)
//                .build();
//    }

    public String messageModel(List<ChatMessage> messages) {
//        ChatResponse response = geminiChatModel.chat(messages);
        return assistant.chat(messages);
    }
//
//    public GeminiService() {
//        this.client = Client.builder()
//                .apiKey(apiKey)
//                .build();
//    }
//
//    public String askGemini(String prompt) {
//        GenerateContentResponse response = client.models
//                .generateContent("gemini-2.5-flash", prompt, null);
//        return  response.text();
//    }
//
//    public List<Content> buildChat(List<ChatBlock> chatBlocks) {
//        //Map the chatBlocks to List<Content> for the Gemini API
//        List<Content> history = chatBlocks.stream()
//                .map(block -> Content.builder()
//                        .role(block.role)
//                        .parts(List.of(Part.fromText(block.content)))
//                        .build()
//                ).toList();
//
//        return history;
//
//    }

//    public String messageGemini(UserChat chat) {
//        AiMessage message = AiMessage.from(
//
//        )
//    }

//    public String messageGemini(String[] messages) {
//        Content content = new Content() {
//            @Override
//            public Optional<List<Part>> parts() {
//                return Optional.empty();
//            }
//
//            @Override
//            public Optional<String> role() {
//                return Optional.empty();
//            }
//
//            @Override
//            public Builder toBuilder() {
//                return null;
//            }
//        }
////        GenerateContentResponse response = client.models
////                .generateContent("gemini-2.5-flash",);
//
//    }
}
