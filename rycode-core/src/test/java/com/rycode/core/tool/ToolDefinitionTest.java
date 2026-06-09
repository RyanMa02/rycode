package com.rycode.core.tool;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToolDefinitionTest {

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> new ToolDefinition(" ", "Echo text", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name must not be blank");
    }

    @Test
    void rejectsBlankDescription() {
        assertThatThrownBy(() -> new ToolDefinition("echo", " ", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("description must not be blank");
    }
}
