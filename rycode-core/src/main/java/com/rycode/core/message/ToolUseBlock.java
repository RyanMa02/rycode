package com.rycode.core.message;

import com.rycode.core.tool.ToolCall;

import java.util.Objects;

public record ToolUseBlock(ToolCall toolCall) implements ContentBlock {

    public ToolUseBlock {
        Objects.requireNonNull(toolCall, "toolCall must not be null");
    }
}
