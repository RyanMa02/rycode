# rycode Project Instructions

## Project Goal

rycode is a Java backend learning project for rebuilding Claude Code-like agent harness concepts in Java.

Focus on understanding and implementing the core ideas:

- Model invocation
- Agent loop
- Tool use
- Permission checks
- Memory/context handling
- Task tracking
- Subagent/background-task patterns

Reference materials are stored under:

- `references/claude-code-source-code`
- `references/learn-claude-code-main`

Use references for architectural study only. Do not copy proprietary source code.

## Working Style

- Keep changes simple and incremental.
- Touch only files related to the current task.
- Prefer idiomatic Java backend design.
- Use clear package boundaries, interfaces, and tests.
- Validate external inputs and handle errors explicitly.
- Do not hardcode secrets or API keys.

## Testing

Use TDD for new behavior when practical:

1. Write a failing test.
2. Implement the minimum code.
3. Run the test and make it pass.
4. Refactor only when needed.

Core agent harness logic should have unit tests.

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
