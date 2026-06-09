package com.rycode.core.agent;

import com.rycode.core.message.Message;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentRunResultTest {

    @Test
    void allowsBlankFinalText() {
        AgentRunResult result = new AgentRunResult("", List.of(Message.userText("hello")));

        assertThat(result.finalText()).isEmpty();
    }

    @Test
    void rejectsEmptyMessages() {
        assertThatThrownBy(() -> new AgentRunResult("ok", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("messages must not be empty");
    }
}
