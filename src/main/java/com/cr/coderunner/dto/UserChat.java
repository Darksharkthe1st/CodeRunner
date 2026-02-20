package com.cr.coderunner.dto;

import com.cr.coderunner.model.CodeSubmission;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.data.message.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserChat {
    final ChatBlock[] messages;
    final CodeSubmission code;
    final RunResult runResult;

    public UserChat(@JsonProperty("messages") ChatBlock[] messages, @JsonProperty("code") CodeSubmission code, @JsonProperty("result") RunResult runResult) {
        this.messages = messages;
        this.code = code;
        this.runResult = runResult;
    }

    @Override
    public String toString() {
        return String.format("MESSAGES: %s\nCODE: %s\nRESULT: %s", Arrays.toString(messages), code, runResult.toString());
    }

    public List<ChatMessage> getMessages() {
        //Collect all the existing chat messages:
        ArrayList<ChatMessage> chatMessages = Stream.of(messages).map(
                msg -> switch (msg.role) {
                    case "system" -> SystemMessage.from(msg.content);
                    case "user" -> UserMessage.from(msg.content);
                    case "agent" -> AiMessage.from(msg.content);
                    default -> new CustomMessage(Map.of(
                            "role", "unknown",
                            "content", msg.content
                    ));
                }
        ).collect(Collectors.toCollection(ArrayList::new));

        //Add a message containing the code for the LLM to access
        String codeContent = "Here's some information about my code: ";
        codeContent += "=======CODE=======\n";
        codeContent += code.displayStr() + "\n";
        codeContent += "======RESULTS=====\n";
        codeContent += runResult.displayStr() + "\n";

        chatMessages.add(chatMessages.size() - 1, new UserMessage(codeContent));

        return chatMessages;
    }
}
