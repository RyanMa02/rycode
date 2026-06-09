package com.rycode.core.model;

import com.rycode.core.message.Message;
import com.rycode.core.tool.ToolDefinition;

import java.util.List;
import java.util.Objects;

public record ModelRequest(List<Message> messages, List<ToolDefinition> tools) {

    public ModelRequest {
        messages = List.copyOf(Objects.requireNonNull(messages, "messages must not be null"));
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("messages must not be empty");
        }
        tools = List.copyOf(Objects.requireNonNull(tools, "tools must not be null"));
    }
}
