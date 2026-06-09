package com.rycode.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelResponseTest {

    @Test
    void rejectsEmptyContent() {
        assertThatThrownBy(() -> new ModelResponse(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("content must not be empty");
    }
}
