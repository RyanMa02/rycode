package com.rycode.core.model;

import com.rycode.core.message.ContentBlock;

import java.util.List;
import java.util.Objects;

public record ModelResponse(List<ContentBlock> content) {

    public ModelResponse {
        content = List.copyOf(Objects.requireNonNull(content, "content must not be null"));
        if (content.isEmpty()) {
            throw new IllegalArgumentException("content must not be empty");
        }
    }
}
