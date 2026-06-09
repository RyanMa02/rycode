package com.rycode.core.agent;

import java.util.Objects;

public record AgentRunRequest(String userInput) {

    public AgentRunRequest {
        Objects.requireNonNull(userInput, "userInput must not be null");
        if (userInput.isBlank()) {
            throw new IllegalArgumentException("userInput must not be blank");
        }
    }
}
