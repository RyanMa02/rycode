package com.rycode.core.message;

import com.rycode.core.tool.ToolResult;

import java.util.Objects;

public record ToolResultBlock(ToolResult toolResult) implements ContentBlock {

    public ToolResultBlock {
        Objects.requireNonNull(toolResult, "toolResult must not be null");
    }
}
