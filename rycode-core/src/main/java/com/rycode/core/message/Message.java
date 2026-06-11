package com.rycode.core.message;

import java.util.List;
import java.util.Objects;

public record Message(MessageRole role, List<ContentBlock> content) {

    public Message {
        Objects.requireNonNull(role, "role must not be null");
        content = List.copyOf(Objects.requireNonNull(content, "content must not be null"));
        if (content.isEmpty()) {
            throw new IllegalArgumentException("content must not be empty");
        }
        validateRoleContent(role, content);
    }

    public static Message userText(String text) {
        return new Message(MessageRole.USER, List.of(new TextBlock(text)));
    }

    public static Message assistant(List<ContentBlock> content) {
        return new Message(MessageRole.ASSISTANT, content);
    }

    public static Message toolResult(ToolResultBlock toolResultBlock) {
        return new Message(MessageRole.TOOL, List.of(toolResultBlock));
    }

    public static Message toolResults(List<ToolResultBlock> toolResultBlocks) {
        return new Message(MessageRole.TOOL, List.copyOf(toolResultBlocks));
    }

    private static void validateRoleContent(MessageRole role, List<ContentBlock> content) {
        switch (role) {
            case USER -> validateUserContent(content);
            case ASSISTANT -> validateAssistantContent(content);
            case TOOL -> validateToolContent(content);
        }
    }

    private static void validateUserContent(List<ContentBlock> content) {
        if (content.stream().anyMatch(block -> !(block instanceof TextBlock))) {
            throw new IllegalArgumentException("USER message content must contain only TextBlock");
        }
    }

    private static void validateAssistantContent(List<ContentBlock> content) {
        if (content.stream().anyMatch(ToolResultBlock.class::isInstance)) {
            throw new IllegalArgumentException("ASSISTANT message content must not contain ToolResultBlock");
        }
    }

    private static void validateToolContent(List<ContentBlock> content) {
        if (content.stream().anyMatch(block -> !(block instanceof ToolResultBlock))) {
            throw new IllegalArgumentException("TOOL message content must contain only ToolResultBlock");
        }
    }
}
