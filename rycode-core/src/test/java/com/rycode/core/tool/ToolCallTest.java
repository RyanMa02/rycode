package com.rycode.core.tool;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToolCallTest {

    @Test
    void rejectsBlankId() {
        assertThatThrownBy(() -> new ToolCall(" ", "echo", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("id must not be blank");
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> new ToolCall("tool-call-1", " ", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name must not be blank");
    }
}
