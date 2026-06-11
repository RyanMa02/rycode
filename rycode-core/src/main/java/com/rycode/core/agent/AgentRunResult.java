package com.rycode.core.agent;

import com.rycode.core.message.Message;

import java.util.List;
import java.util.Objects;

public record AgentRunResult(String finalText, List<Message> messages) {

    public AgentRunResult {
        Objects.requireNonNull(finalText, "finalText must not be null");

        messages = List.copyOf(Objects.requireNonNull(messages, "messages must not be null"));
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("messages must not be empty");
        }
    }
}
