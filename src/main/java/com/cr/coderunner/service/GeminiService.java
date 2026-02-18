package com.cr.coderunner.service;

import com.cr.coderunner.dto.ChatBlock;
import com.google.genai.Client;
import com.google.genai.types.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GeminiService {
    private final Client client;
    private final String model = "gemini-2.5-flash-lite";

    public GeminiService(@Value("${gemini.api.key}") String apiKey) {
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public String askGemini(String prompt) {
        GenerateContentResponse response = client.models
                .generateContent("gemini-2.5-flash", prompt, null);
        return  response.text();
    }

    public List<Content> buildChat(List<ChatBlock> chatBlocks) {
        //Map the chatBlocks to List<Content> for the Gemini API
        List<Content> history = chatBlocks.stream()
                .map(block -> Content.builder()
                        .role(block.role)
                        .parts(List.of(Part.fromText(block.content)))
                        .build()
                ).toList();

        return history;

    }

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
