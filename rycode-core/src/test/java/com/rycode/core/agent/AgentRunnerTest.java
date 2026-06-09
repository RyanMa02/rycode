package com.rycode.core.agent;

import com.rycode.core.message.TextBlock;
import com.rycode.core.message.ToolResultBlock;
import com.rycode.core.message.ToolUseBlock;
import com.rycode.core.model.ModelClient;
import com.rycode.core.model.ModelRequest;
import com.rycode.core.model.ModelResponse;
import com.rycode.core.permission.PermissionDecision;
import com.rycode.core.tool.Tool;
import com.rycode.core.tool.ToolCall;
import com.rycode.core.tool.ToolDefinition;
import com.rycode.core.tool.ToolRegistry;
import com.rycode.core.tool.ToolResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentRunnerTest {

    @Test
    void returnsFinalAnswerWithoutToolUse() {
        FakeModelClient modelClient = new FakeModelClient(List.of(
                new ModelResponse(List.of(new TextBlock("Done")))
        ));
        AgentRunner runner = new AgentRunner(
                modelClient,
                new ToolRegistry(List.of()),
                toolCall -> PermissionDecision.allow("test"),
                toolCall -> false,
                5,
                10
        );

        AgentRunResult result = runner.run(new AgentRunRequest("Say hello"));

        assertThat(result.finalText()).isEqualTo("Done");
        assertThat(modelClient.requests()).hasSize(1);
        assertThat(modelClient.requests().getFirst().messages()).hasSize(1);
    }

    @Test
    void executesToolAndContinuesUntilFinalAnswer() {
        ToolUseBlock toolUse = new ToolUseBlock(new ToolCall(
                "tool-call-1",
                "echo",
                Map.of("text", "hello")
        ));
        FakeModelClient modelClient = new FakeModelClient(List.of(
                new ModelResponse(List.of(toolUse)),
                new ModelResponse(List.of(new TextBlock("Tool said hello")))
        ));
        AgentRunner runner = new AgentRunner(
                modelClient,
                new ToolRegistry(List.of(new EchoTool("echo"))),
                toolCall -> PermissionDecision.allow("test"),
                toolCall -> false,
                5,
                10
        );

        AgentRunResult result = runner.run(new AgentRunRequest("Use echo"));

        assertThat(result.finalText()).isEqualTo("Tool said hello");
        assertThat(modelClient.requests()).hasSize(2);
        assertThat(modelClient.requests().get(1).messages())
                .anySatisfy(message -> assertThat(message.content())
                        .contains(new ToolResultBlock(new ToolResult(
                                "tool-call-1",
                                true,
                                "hello",
                                null
                        ))));
    }

    @Test
    void asksForConfirmationBeforeExecutingAskDecision() {
        ToolUseBlock toolUse = new ToolUseBlock(new ToolCall(
                "tool-call-1",
                "echo",
                Map.of("text", "approved")
        ));
        FakeModelClient modelClient = new FakeModelClient(List.of(
                new ModelResponse(List.of(toolUse)),
                new ModelResponse(List.of(new TextBlock("Approved")))
        ));
        AgentRunner runner = new AgentRunner(
                modelClient,
                new ToolRegistry(List.of(new EchoTool("echo"))),
                toolCall -> PermissionDecision.ask("needs confirmation"),
                toolCall -> true,
                5,
                10
        );

        AgentRunResult result = runner.run(new AgentRunRequest("Use echo"));

        assertThat(result.finalText()).isEqualTo("Approved");
        assertThat(modelClient.requests()).hasSize(2);
        assertThat(modelClient.requests().get(1).messages())
                .anySatisfy(message -> assertThat(message.content())
                        .contains(new ToolResultBlock(new ToolResult(
                                "tool-call-1",
                                true,
                                "approved",
                                null
                        ))));
    }

    @Test
    void recordsDeniedToolResultWhenConfirmationRejectsAskDecision() {
        ToolUseBlock toolUse = new ToolUseBlock(new ToolCall(
                "tool-call-1",
                "echo",
                Map.of("text", "rejected")
        ));
        FakeModelClient modelClient = new FakeModelClient(List.of(
                new ModelResponse(List.of(toolUse)),
                new ModelResponse(List.of(new TextBlock("Rejected")))
        ));
        AgentRunner runner = new AgentRunner(
                modelClient,
                new ToolRegistry(List.of(new EchoTool("echo"))),
                toolCall -> PermissionDecision.ask("needs confirmation"),
                toolCall -> false,
                5,
                10
        );

        AgentRunResult result = runner.run(new AgentRunRequest("Use echo"));

        assertThat(result.finalText()).isEqualTo("Rejected");
        assertThat(modelClient.requests().get(1).messages())
                .anySatisfy(message -> assertThat(message.content())
                        .contains(new ToolResultBlock(new ToolResult(
                                "tool-call-1",
                                false,
                                null,
                                "Permission denied: needs confirmation"
                        ))));
    }

    @Test
    void deniesToolWithoutAskingForConfirmationOrExecutingTool() {
        ToolUseBlock toolUse = new ToolUseBlock(new ToolCall(
                "tool-call-1",
                "tracking",
                Map.of("text", "blocked")
        ));
        FakeModelClient modelClient = new FakeModelClient(List.of(
                new ModelResponse(List.of(toolUse)),
                new ModelResponse(List.of(new TextBlock("Denied")))
        ));
        TrackingTool tool = new TrackingTool();
        AgentRunner runner = new AgentRunner(
                modelClient,
                new ToolRegistry(List.of(tool)),
                toolCall -> PermissionDecision.deny("blocked by rule"),
                toolCall -> {
                    throw new AssertionError("confirmation should not be requested for DENY");
                },
                5,
                10
        );

        AgentRunResult result = runner.run(new AgentRunRequest("Use tracking"));

        assertThat(result.finalText()).isEqualTo("Denied");
        assertThat(tool.executed()).isFalse();
        assertThat(modelClient.requests().get(1).messages())
                .anySatisfy(message -> assertThat(message.content())
                        .contains(new ToolResultBlock(new ToolResult(
                                "tool-call-1",
                                false,
                                null,
                                "Permission denied: blocked by rule"
                        ))));
    }

    @Test
    void allowDoesNotAskForConfirmation() {
        ToolUseBlock toolUse = new ToolUseBlock(new ToolCall(
                "tool-call-1",
                "echo",
                Map.of("text", "allowed")
        ));
        FakeModelClient modelClient = new FakeModelClient(List.of(
                new ModelResponse(List.of(toolUse)),
                new ModelResponse(List.of(new TextBlock("Allowed")))
        ));
        AgentRunner runner = new AgentRunner(
                modelClient,
                new ToolRegistry(List.of(new EchoTool("echo"))),
                toolCall -> PermissionDecision.allow("allowed by rule"),
                toolCall -> {
                    throw new AssertionError("confirmation should not be requested for ALLOW");
                },
                5,
                10
        );

        AgentRunResult result = runner.run(new AgentRunRequest("Use echo"));

        assertThat(result.finalText()).isEqualTo("Allowed");
    }

    @Test
    void exposesToolDefinitionsToModelInRegistrationOrder() {
        FakeModelClient modelClient = new FakeModelClient(List.of(
                new ModelResponse(List.of(new TextBlock("Done")))
        ));
        AgentRunner runner = new AgentRunner(
                modelClient,
                new ToolRegistry(List.of(new EchoTool("first"), new EchoTool("second"))),
                toolCall -> PermissionDecision.allow("test"),
                toolCall -> false,
                5,
                10
        );

        runner.run(new AgentRunRequest("List tools"));

        assertThat(modelClient.requests().getFirst().tools())
                .extracting(ToolDefinition::name)
                .containsExactly("first", "second");
    }

    @Test
    void appendsOneToolMessageForMultipleToolResultsInOneTurn() {
        FakeModelClient modelClient = new FakeModelClient(List.of(
                new ModelResponse(List.of(
                        new ToolUseBlock(new ToolCall("tool-call-1", "echo", Map.of("text", "one"))),
                        new ToolUseBlock(new ToolCall("tool-call-2", "echo", Map.of("text", "two")))
                )),
                new ModelResponse(List.of(new TextBlock("Done")))
        ));
        AgentRunner runner = new AgentRunner(
                modelClient,
                new ToolRegistry(List.of(new EchoTool("echo"))),
                toolCall -> PermissionDecision.allow("test"),
                toolCall -> false,
                5,
                10
        );

        AgentRunResult result = runner.run(new AgentRunRequest("Use echo twice"));

        assertThat(result.finalText()).isEqualTo("Done");
        assertThat(modelClient.requests()).hasSize(2);
        assertThat(modelClient.requests().get(1).messages()).hasSize(3);
        assertThat(modelClient.requests().get(1).messages().get(2).content())
                .containsExactly(
                        new ToolResultBlock(new ToolResult("tool-call-1", true, "one", null)),
                        new ToolResultBlock(new ToolResult("tool-call-2", true, "two", null))
                );
    }

    @Test
    void rejectsTooManyToolCallsInOneTurn() {
        FakeModelClient modelClient = new FakeModelClient(List.of(
                new ModelResponse(List.of(
                        new ToolUseBlock(new ToolCall("tool-call-1", "echo", Map.of("text", "one"))),
                        new ToolUseBlock(new ToolCall("tool-call-2", "echo", Map.of("text", "two")))
                ))
        ));
        AgentRunner runner = new AgentRunner(
                modelClient,
                new ToolRegistry(List.of(new EchoTool("echo"))),
                toolCall -> PermissionDecision.allow("test"),
                toolCall -> false,
                5,
                1
        );

        assertThatThrownBy(() -> runner.run(new AgentRunRequest("Use too many tools")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Tool calls per turn exceeded max: 1");
    }

    private static final class FakeModelClient implements ModelClient {

        private final Queue<ModelResponse> responses;
        private final List<ModelRequest> requests = new java.util.ArrayList<>();

        private FakeModelClient(List<ModelResponse> responses) {
            this.responses = new ArrayDeque<>(responses);
        }

        @Override
        public ModelResponse complete(ModelRequest request) {
            requests.add(request);
            return responses.remove();
        }

        private List<ModelRequest> requests() {
            return List.copyOf(requests);
        }
    }

    private static final class TrackingTool implements Tool {

        private boolean executed;

        @Override
        public ToolDefinition definition() {
            return new ToolDefinition("tracking", "Track execution", Map.of());
        }

        @Override
        public ToolResult execute(ToolCall toolCall) {
            executed = true;
            return new ToolResult(toolCall.id(), true, "executed", null);
        }

        private boolean executed() {
            return executed;
        }
    }

    private static final class EchoTool implements Tool {

        private final String name;

        private EchoTool(String name) {
            this.name = name;
        }

        @Override
        public ToolDefinition definition() {
            return new ToolDefinition(name, "Echo text", Map.of());
        }

        @Override
        public ToolResult execute(ToolCall toolCall) {
            return new ToolResult(
                    toolCall.id(),
                    true,
                    toolCall.input().get("text").toString(),
                    null
            );
        }
    }
}
