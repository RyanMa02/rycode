package com.rycode.core.message;

import java.util.Objects;

public record TextBlock(String text) implements ContentBlock {

    public TextBlock {
        Objects.requireNonNull(text, "text must not be null");
        if (text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
    }
}
