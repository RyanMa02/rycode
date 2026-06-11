package com.rycode.core.agent;

import com.rycode.core.tool.Tool;
import com.rycode.core.tool.ToolCall;
import com.rycode.core.tool.ToolDefinition;
import com.rycode.core.tool.ToolResult;

import java.util.Map;

/**
 * @author Ryan
 */
final class TestTools {

    private TestTools() {
    }

    static Tool echo(String name) {
        return new EchoTool(name);
    }

    static Tool failing() {
        return new FailingTool();
    }

    static TrackingTool tracking() {
        return new TrackingTool();
    }

    static final class TrackingTool implements Tool {

        private boolean executed;

        @Override
        public ToolDefinition definition() {
            return new ToolDefinition("tracking", "Track execution", Map.of());
        }

        @Override
        public ToolResult execute(ToolCall toolCall) {
            executed = true;
            return new ToolResult(toolCall.id(), true, "executed", null);
        }

        boolean executed() {
            return executed;
        }
    }

    private record FailingTool() implements Tool {

        @Override
        public ToolDefinition definition() {
            return new ToolDefinition("failing", "Fail execution", Map.of());
        }

        @Override
        public ToolResult execute(ToolCall toolCall) {
            throw new IllegalStateException("boom secret=" + toolCall.input().get("secret"));
        }
    }

    private record EchoTool(String name) implements Tool {

        @Override
        public ToolDefinition definition() {
            return new ToolDefinition(name, "Echo text", Map.of());
        }

        @Override
        public ToolResult execute(ToolCall toolCall) {
            return new ToolResult(
                    toolCall.id(),
                    true,
                    toolCall.input().get("text").toString(),
                    null
            );
        }
    }
}
