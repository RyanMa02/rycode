package com.rycode.core.tool;

/**
 * @author Ryan
 */
public interface Tool {

    ToolDefinition definition();

    ToolResult execute(ToolCall toolCall);
}
