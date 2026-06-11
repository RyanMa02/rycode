package com.rycode.core.permission;

import com.rycode.core.tool.ToolCall;

/**
 * @author Ryan
 */
@FunctionalInterface
public interface UserConfirmationProvider {

    boolean confirm(ToolCall toolCall);
}
