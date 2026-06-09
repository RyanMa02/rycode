package com.rycode.core.message;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TextBlockTest {

    @Test
    void rejectsBlankText() {
        assertThatThrownBy(() -> new TextBlock(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("text must not be blank");
    }
}
