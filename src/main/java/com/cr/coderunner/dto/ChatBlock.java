package com.cr.coderunner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatBlock {
    public final String role;
    public final String content;

    public ChatBlock(@JsonProperty("role") String role, @JsonProperty("content") String content) {
        this.role = role;
        this.content = content;
    }

    public String toString() {
        return String.format("{%s : %s}", role, content);
    }
}