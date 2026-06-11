package com.rycode.core.tool;

import java.util.Objects;

public record ToolResult(String toolCallId, boolean success, String output, String error) {

    public ToolResult {
        Objects.requireNonNull(toolCallId, "toolCallId must not be null");
        if (toolCallId.isBlank()) {
            throw new IllegalArgumentException("toolCallId must not be blank");
        }
        if (success && error != null) {
            throw new IllegalArgumentException("successful tool result must not contain error");
        }
        if (!success && (error == null || error.isBlank())) {
            throw new IllegalArgumentException("failed tool result must contain error");
        }
    }
}
