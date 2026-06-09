package com.rycode.core.agent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentRunRequestTest {

    @Test
    void rejectsBlankUserInput() {
        assertThatThrownBy(() -> new AgentRunRequest("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userInput must not be blank");
    }
}
