package com.rycode.core.message;

/**
 * @author Ryan
 */
public sealed interface ContentBlock permits TextBlock, ToolUseBlock, ToolResultBlock {
}
