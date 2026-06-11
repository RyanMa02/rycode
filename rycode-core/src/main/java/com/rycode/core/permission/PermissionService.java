package com.rycode.core.permission;

import com.rycode.core.tool.ToolCall;

/**
 * @author Ryan
 */
@FunctionalInterface
public interface PermissionService {

    PermissionDecision evaluate(ToolCall toolCall);
}
