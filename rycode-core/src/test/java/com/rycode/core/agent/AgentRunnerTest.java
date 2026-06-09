package com.rycode.core.agent;

import com.rycode.core.message.TextBlock;
import com.rycode.core.message.ToolResultBlock;
import com.rycode.core.message.ToolUseBlock;
import com.rycode.core.model.ModelClient;
import com.rycode.core.model.ModelRequest;
import com.rycode.core.model.ModelResponse;
import com.rycode.core.permission.PermissionDecision;
import com.rycode.core.permission.PermissionService;
import com.rycode.core.permission.UserConfirmationProvider;
import com.rycode.core.tool.Tool;
import com.rycode.core.tool.ToolCall;
import com.rycode.core.tool.ToolDefinition;
import com.rycode.core.tool.ToolRegistry;
import com.rycode.core.tool.ToolResult;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentRunnerTest {

    @Test
    void returnsFinalAnswerWithoutToolUse() {
        FakeModelClient modelClient = new FakeModelClient(List.of(textResponse("Done")));
        AgentRunner runner = newRunner(modelClient, new ToolRegistry(List.of()));

        AgentRunResult result = runner.run(new AgentRunRequest("Say hello"));

        assertThat(result.finalText()).isEqualTo("Done");
        assertThat(modelClient.requests()).hasSize(1);
        assertThat(modelClient.requests().getFirst().messages()).hasSize(1);
    }

    @Test
    void executesToolAndContinuesUntilFinalAnswer() {
        FakeModelClient modelClient = new FakeModelClient(List.of(
                toolUseResponse("tool-call-1", "echo", Map.of("text", "hello")),
                textResponse("Tool said hello")
        ));
        AgentRunner runner = newRunner(modelClient, new ToolRegistry(List.of(new EchoTool("echo"))));

        AgentRunResult result = runner.run(new AgentRunRequest("Use echo"));

        assertThat(result.finalText()).isEqualTo("Tool said hello");
        assertThat(modelClient.requests()).hasSize(2);
        assertToolResult(modelClient, "tool-call-1", true, "hello", null);
    }

    @Test
    void asksForConfirmationBeforeExecutingAskDecision() {
        FakeModelClient modelClient = new FakeModelClient(List.of(
                toolUseResponse("tool-call-1", "echo", Map.of("text", "approved")),
                textResponse("Approved")
        ));
        AgentRunner runner = newRunner(
                modelClient,
                new ToolRegistry(List.of(new EchoTool("echo"))),
                toolCall -> PermissionDecision.ask("needs confirmation"),
                toolCall -> true
        );

        AgentRunResult result = runner.run(new AgentRunRequest("Use echo"));

        assertThat(result.finalText()).isEqualTo("Approved");
        assertThat(modelClient.requests()).hasSize(2);
        assertToolResult(modelClient, "tool-call-1", true, "approved", null);
    }

    @Test
    void recordsDeniedToolResultWhenConfirmationRejectsAskDecision() {
        FakeModelClient modelClient = new FakeModelClient(List.of(
                toolUseResponse("tool-call-1", "echo", Map.of("text", "rejected")),
                textResponse("Rejected")
        ));
        AgentRunner runner = newRunner(
                modelClient,
                new ToolRegistry(List.of(new EchoTool("echo"))),
                toolCall -> PermissionDecision.ask("user declined"),
                toolCall -> false
        );

        AgentRunResult result = runner.run(new AgentRunRequest("Use echo"));

        assertThat(result.finalText()).isEqualTo("Rejected");
        assertToolResult(modelClient, "tool-call-1", false, null, "Permission denied: user declined");
    }

    @Test
    void deniesToolWithoutAskingForConfirmationOrExecutingTool() {
        FakeModelClient modelClient = new FakeModelClient(List.of(
                toolUseResponse("tool-call-1", "tracking", Map.of("text", "blocked")),
                textResponse("Denied")
        ));
        TrackingTool tool = new TrackingTool();
        AgentRunner runner = newRunner(
                modelClient,
                new ToolRegistry(List.of(tool)),
                toolCall -> PermissionDecision.deny("blocked by rule"),
                toolCall -> {
                    throw new AssertionError("confirmation should not be requested for DENY");
                }
        );

        AgentRunResult result = runner.run(new AgentRunRequest("Use tracking"));

        assertThat(result.finalText()).isEqualTo("Denied");
        assertThat(tool.executed()).isFalse();
        assertToolResult(modelClient, "tool-call-1", false, null, "Permission denied: blocked by rule");
    }

    @Test
    void allowDoesNotAskForConfirmation() {
        FakeModelClient modelClient = new FakeModelClient(List.of(
                toolUseResponse("tool-call-1", "echo", Map.of("text", "allowed")),
                textResponse("Allowed")
        ));
        AgentRunner runner = newRunner(
                modelClient,
                new ToolRegistry(List.of(new EchoTool("echo"))),
                toolCall -> PermissionDecision.allow("allowed by rule"),
                toolCall -> {
                    throw new AssertionError("confirmation should not be requested for ALLOW");
                }
        );

        AgentRunResult result = runner.run(new AgentRunRequest("Use echo"));

        assertThat(result.finalText()).isEqualTo("Allowed");
    }

    @Test
    void exposesToolDefinitionsToModelInRegistrationOrder() {
        FakeModelClient modelClient = new FakeModelClient(List.of(textResponse("Done")));
        AgentRunner runner = newRunner(
                modelClient,
                new ToolRegistry(List.of(new EchoTool("first"), new EchoTool("second")))
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
                textResponse("Done")
        ));
        AgentRunner runner = newRunner(modelClient, new ToolRegistry(List.of(new EchoTool("echo"))));

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

    @Test
    void recordsFailedToolResultWhenToolIsUnknown() {
        FakeModelClient modelClient = new FakeModelClient(List.of(
                toolUseResponse("tool-call-1", "missing", Map.of()),
                textResponse("Done")
        ));
        AgentRunner runner = newRunner(modelClient, new ToolRegistry(List.of()));

        AgentRunResult result = runner.run(new AgentRunRequest("Use missing tool"));

        assertThat(result.finalText()).isEqualTo("Done");
        assertToolResult(modelClient, "tool-call-1", false, null, "Tool not found: missing");
    }

    @Test
    void stopsWhenAgentLoopExceedsMaxIterations() {
        FakeModelClient modelClient = new FakeModelClient(List.of(
                toolUseResponse("tool-call-1", "echo", Map.of("text", "one")),
                toolUseResponse("tool-call-2", "echo", Map.of("text", "two"))
        ));
        AgentRunner runner = new AgentRunner(
                modelClient,
                new ToolRegistry(List.of(new EchoTool("echo"))),
                toolCall -> PermissionDecision.allow("test"),
                toolCall -> false,
                2,
                10
        );

        assertThatThrownBy(() -> runner.run(new AgentRunRequest("Keep using tools")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Agent loop exceeded max iterations: 2");
    }

    private static AgentRunner newRunner(FakeModelClient modelClient, ToolRegistry toolRegistry) {
        return newRunner(
                modelClient,
                toolRegistry,
                toolCall -> PermissionDecision.allow("test"),
                toolCall -> false
        );
    }

    private static AgentRunner newRunner(
            FakeModelClient modelClient,
            ToolRegistry toolRegistry,
            PermissionService permissionService,
            UserConfirmationProvider confirmationProvider
    ) {
        return new AgentRunner(
                modelClient,
                toolRegistry,
                permissionService,
                confirmationProvider,
                5,
                10
        );
    }

    private static ModelResponse textResponse(String text) {
        return new ModelResponse(List.of(new TextBlock(text)));
    }

    private static ModelResponse toolUseResponse(String id, String name, Map<String, Object> input) {
        return new ModelResponse(List.of(new ToolUseBlock(new ToolCall(id, name, input))));
    }

    private static void assertToolResult(
            FakeModelClient modelClient,
            String toolCallId,
            boolean success,
            String output,
            String error
    ) {
        assertThat(modelClient.requests().get(1).messages())
                .anySatisfy(message -> assertThat(message.content())
                        .contains(new ToolResultBlock(new ToolResult(toolCallId, success, output, error))));
    }

    private static final class FakeModelClient implements ModelClient {

        private final Queue<ModelResponse> responses;
        private final List<ModelRequest> requests = new ArrayList<>();

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
