package com.rycode.core.message;

import com.rycode.core.tool.ToolCall;
import com.rycode.core.tool.ToolResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageTest {

    @Test
    void rejectsUserMessageWithToolUseContent() {
        ToolCall toolCall = new ToolCall("tool-call-1", "echo", Map.of());

        assertThatThrownBy(() -> new Message(MessageRole.USER, List.of(new ToolUseBlock(toolCall))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("USER message content must contain only TextBlock");
    }

    @Test
    void rejectsAssistantMessageWithToolResultContent() {
        ToolResult toolResult = new ToolResult("tool-call-1", true, "ok", null);

        assertThatThrownBy(() -> Message.assistant(List.of(new ToolResultBlock(toolResult))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ASSISTANT message content must not contain ToolResultBlock");
    }

    @Test
    void allowsToolMessageWithMultipleToolResults() {
        ToolResult first = new ToolResult("tool-call-1", true, "first", null);
        ToolResult second = new ToolResult("tool-call-2", true, "second", null);

        Message message = Message.toolResults(List.of(
                new ToolResultBlock(first),
                new ToolResultBlock(second)
        ));

        assertThat(message.role()).isEqualTo(MessageRole.TOOL);
        assertThat(message.content()).containsExactly(
                new ToolResultBlock(first),
                new ToolResultBlock(second)
        );
    }

    @Test
    void rejectsToolMessageWithMixedBlocks() {
        ToolResult toolResult = new ToolResult("tool-call-1", true, "ok", null);

        assertThatThrownBy(() -> new Message(MessageRole.TOOL, List.of(
                new ToolResultBlock(toolResult),
                new TextBlock("extra")
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("TOOL message content must contain only ToolResultBlock");
    }
}
