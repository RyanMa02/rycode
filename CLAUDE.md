# rycode Project Instructions

## Project Goal

rycode is a Java backend learning project for rebuilding claude-code-like Agent Harness concepts in Java.

The project focuses on understanding and implementing the harness around the model, not training or recreating the model itself:

```text
Agent = LLM + Harness
Harness = Tools + Context + Permission + Runtime + Task/Memory
```

Core MVP focus:

- model invocation
- agent loop
- tool use
- permission checks
- context and memory handling
- task tracking
- hooks and error recovery
- extensible session/event/store abstractions

Future goals include production-grade CLI, REST service, multi-agent orchestration, MCP/plugin ecosystem, richer UI, background tasks, and worktree isolation.
These are not excluded goals; MVP implementation should keep extension points for them.

## Design Reference

Use `DESIGN.md` as the source of truth for current architecture and MVP scope.

Before implementing features, check the relevant sections in `DESIGN.md`, especially:

- design principles
- module boundaries
- core architecture
- Agent Loop design
- model invocation layer
- ToolRegistry / ToolProvider design
- permission system
- context and memory design
- AgentEvent and Store abstractions
- MVP boundaries and future extension points

## Documentation Maintenance

`CLAUDE.md` and `DESIGN.md` are living project documents.

During future discussions and implementation:

- Update `DESIGN.md` when architecture, module boundaries, MVP scope, future goals, or key decisions change.
- Update `CLAUDE.md` when project-level working instructions, design-reference rules, or long-term implementation constraints change.
- Keep `CLAUDE.md` concise; use it as the project instruction index, not as a full design document.
- Keep detailed design content in `DESIGN.md` to avoid unnecessary context growth.
- If conversation decisions contradict either file, confirm with the user, then update the relevant file before implementing based on the new decision.

## Working Style

- Keep changes simple and incremental.
- Touch only files related to the current task.
- Prefer idiomatic Java backend design.
- Use clear package boundaries, interfaces, and tests.
- Validate external inputs and handle errors explicitly.
- Do not hardcode secrets or API keys.
- Do not let `rycode-core` depend on Spring, CLI, REST, terminal I/O, or concrete storage details.
- Treat CLI, REST, MCP, and UI as adapters over core abstractions.

## References

Reference materials are stored under:

- `references/claude-code-source-code`
- `references/learn-claude-code-main`

Use references for architectural study only. Do not copy proprietary source code.

Prefer the step-by-step learning structure from `references/learn-claude-code-main` when planning implementation order.

## Testing

Use TDD for new behavior when practical:

1. Write a failing test.
2. Implement the minimum code.
3. Run the test and make it pass.
4. Refactor only when needed.

Core agent harness logic should have unit tests.

Use `FakeModelClient` or equivalent test doubles for stable automated tests. Do not make unit tests depend on real external model calls.

## Git Workflow

Use short-lived branches and keep `main` working.

Branch naming examples:

```text
feature/<specific-function>
fix/<specific-bug>
docs/<specific-topic>
test/<specific-scope>
refactor/<specific-area>
```

Do not push to remote unless explicitly approved.

## Definition of Done

A task is done only when:

- Relevant tests pass.
- Static/IDE diagnostics are checked when code changes.
- Git status is understood before committing.
- `DESIGN.md` and/or `CLAUDE.md` are updated if the task changes project decisions, architecture, scope, or long-term instructions.
