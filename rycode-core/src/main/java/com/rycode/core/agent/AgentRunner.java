package com.rycode.core.agent;

import com.rycode.core.message.Message;
import com.rycode.core.message.TextBlock;
import com.rycode.core.message.ToolResultBlock;
import com.rycode.core.message.ToolUseBlock;
import com.rycode.core.model.ModelClient;
import com.rycode.core.model.ModelRequest;
import com.rycode.core.model.ModelResponse;
import com.rycode.core.permission.PermissionDecision;
import com.rycode.core.permission.PermissionDecisionType;
import com.rycode.core.permission.PermissionService;
import com.rycode.core.permission.UserConfirmationProvider;
import com.rycode.core.tool.ToolCall;
import com.rycode.core.tool.ToolRegistry;
import com.rycode.core.tool.ToolResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Ryan
 */
public final class AgentRunner {

    private final ModelClient modelClient;
    private final ToolRegistry toolRegistry;
    private final PermissionService permissionService;
    private final UserConfirmationProvider confirmationProvider;
    private final int maxLoopIterations;
    private final int maxToolCallsPerTurn;

    public AgentRunner(
            ModelClient modelClient,
            ToolRegistry toolRegistry,
            PermissionService permissionService,
            UserConfirmationProvider confirmationProvider,
            int maxLoopIterations,
            int maxToolCallsPerTurn
    ) {
        this.modelClient = Objects.requireNonNull(modelClient, "modelClient must not be null");
        this.toolRegistry = Objects.requireNonNull(toolRegistry, "toolRegistry must not be null");
        this.permissionService = Objects.requireNonNull(permissionService, "permissionService must not be null");
        this.confirmationProvider = Objects.requireNonNull(confirmationProvider, "confirmationProvider must not be null");

        if (maxLoopIterations < 1) {
            throw new IllegalArgumentException("maxLoopIterations must be positive");
        }
        this.maxLoopIterations = maxLoopIterations;

        if (maxToolCallsPerTurn < 1) {
            throw new IllegalArgumentException("maxToolCallsPerTurn must be positive");
        }
        this.maxToolCallsPerTurn = maxToolCallsPerTurn;
    }

    public AgentRunResult run(AgentRunRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        List<Message> messages = new ArrayList<>();
        messages.add(Message.userText(request.userInput()));

        for (int iteration = 0; iteration < maxLoopIterations; iteration++) {
            ModelResponse response = modelClient.complete(new ModelRequest(messages, toolRegistry.definitions()));
            messages.add(Message.assistant(response.content()));

            List<ToolUseBlock> toolUses = extractToolUseBlocks(response);
            if (toolUses.isEmpty()) {
                return new AgentRunResult(extractFinalText(response), messages);
            }
            if (toolUses.size() > maxToolCallsPerTurn) {
                throw new IllegalStateException("Tool calls per turn exceeded max: " + maxToolCallsPerTurn);
            }

            List<ToolResultBlock> toolResultBlocks = new ArrayList<>();
            for (ToolUseBlock toolUse : toolUses) {
                ToolResult toolResult = executeToolCall(toolUse.toolCall());
                toolResultBlocks.add(new ToolResultBlock(toolResult));
            }
            messages.add(Message.toolResults(toolResultBlocks));
        }

        throw new IllegalStateException("Agent loop exceeded max iterations: " + maxLoopIterations);
    }

    private List<ToolUseBlock> extractToolUseBlocks(ModelResponse response) {
        return response.content().stream()
                .filter(ToolUseBlock.class::isInstance)
                .map(ToolUseBlock.class::cast)
                .toList();
    }

    private ToolResult executeToolCall(ToolCall toolCall) {
        PermissionDecision decision = permissionService.evaluate(toolCall);

        if (decision.type() == PermissionDecisionType.DENY) {
            return deniedToolResult(toolCall, decision.reason());
        }

        if (decision.type() == PermissionDecisionType.ASK && !confirmationProvider.confirm(toolCall)) {
            return deniedToolResult(toolCall, decision.reason());
        }

        return toolRegistry.findByName(toolCall.name())
                .map(tool -> tool.execute(toolCall))
                .orElseGet(() -> new ToolResult(toolCall.id(), false, null, "Tool not found: " + toolCall.name()));
    }

    private ToolResult deniedToolResult(ToolCall toolCall, String reason) {
        return new ToolResult(toolCall.id(), false, null, "Permission denied: " + reason);
    }

    private String extractFinalText(ModelResponse response) {
        return response.content().stream()
                .filter(TextBlock.class::isInstance)
                .map(TextBlock.class::cast)
                .map(TextBlock::text)
                .collect(Collectors.joining());
    }
}
