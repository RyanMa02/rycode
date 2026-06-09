package com.rycode.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelRequestTest {

    @Test
    void rejectsEmptyMessages() {
        assertThatThrownBy(() -> new ModelRequest(List.of(), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("messages must not be empty");
    }
}
