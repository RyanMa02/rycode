package com.rycode.core.tool;

import java.util.Map;
import java.util.Objects;

public record ToolCall(String id, String name, Map<String, Object> input) {

    public ToolCall {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }

        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        input = Map.copyOf(Objects.requireNonNull(input, "input must not be null"));
    }
}
