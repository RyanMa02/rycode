package com.rycode.core.tool;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToolResultTest {

    @Test
    void rejectsBlankToolCallId() {
        assertThatThrownBy(() -> new ToolResult(" ", true, "ok", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("toolCallId must not be blank");
    }

    @Test
    void rejectsSuccessfulResultWithError() {
        assertThatThrownBy(() -> new ToolResult("tool-call-1", true, "ok", "error"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("successful tool result must not contain error");
    }

    @Test
    void rejectsFailedResultWithoutError() {
        assertThatThrownBy(() -> new ToolResult("tool-call-1", false, null, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("failed tool result must contain error");
    }
}
