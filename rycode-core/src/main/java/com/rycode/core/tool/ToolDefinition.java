package com.rycode.core.tool;

import java.util.Map;
import java.util.Objects;

public record ToolDefinition(String name, String description, Map<String, Object> inputSchema) {

    public ToolDefinition {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        Objects.requireNonNull(description, "description must not be null");
        if (description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }

        inputSchema = Map.copyOf(Objects.requireNonNull(inputSchema, "inputSchema must not be null"));
    }
}
