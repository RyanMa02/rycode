# rycode 核心逻辑 MVP 设计文档

> 版本：v0.1  
> 日期：2026-06-02  
> 范围：Java 版 claude-code-like Agent Harness 的核心逻辑 MVP 设计  
> 重点：框架雏形、核心循环、工具调用、权限、上下文、任务系统  
> 后续目标：生产级 CLI、完整 REST 服务、多代理编排、MCP 插件生态、复杂 UI

## 1. 背景与目标

`rycode` 是一个 Java 后端学习项目，目标不是训练或重新实现一个模型，而是学习并重建 claude-code-like 产品背后的 **Agent Harness**。

在这个项目中：

```text
Agent = LLM + Harness
Harness = Tools + Context + Permission + Runtime + Task/Memory
```

模型负责理解、推理和决定下一步行动；Harness 负责提供工具、上下文、权限边界和执行环境。

本轮设计聚焦 **核心逻辑 MVP**：先做出一个可扩展的 Java Agent Harness 框架雏形，并用极简 CLI 证明它能真实调用模型、执行工具、维护上下文和完成多轮任务。

## 2. 设计原则

### 2.1 核心优先

MVP 的中心不是 CLI，也不是 REST API，而是可复用的 Agent Harness 核心能力：

- agent loop
- model invocation
- tool use
- permission check
- context management
- task tracking
- hook/error recovery

CLI 和 REST 都只是 adapter。

### 2.2 多模块分层

核心模块不依赖 Spring。Spring Boot 只用于应用装配、配置管理和 CLI 启动。

### 2.3 面向后续目标的扩展性设计

生产级 CLI、完整 REST 服务、多代理编排、MCP 插件生态和复杂 UI 都是后续目标，不是被排除的目标。MVP 不实现这些能力的完整产品形态，但核心抽象必须避免阻碍它们后续演进。

因此 MVP 阶段需要遵守以下约束：

- `rycode-core` 不依赖 CLI、Spring、REST、终端输入输出或本地文件系统细节。
- agent 输出优先表达为结构化事件，而不是只返回拼接后的字符串。
- session、task、context、memory 都使用明确 ID 和 store 抽象，避免全局状态。
- 用户确认、工具输出、事件展示通过接口回调，不绑定终端实现。
- 工具系统通过 `ToolProvider` 扩展，MVP 只实现内置工具，但保留 MCP/plugin 工具来源。
- 模型调用通过 `ModelClient` 隔离 provider 和具体 SDK。
- `rycode-server` 可以后置实现，但 core API 必须天然可被 REST、SSE 或 WebSocket adapter 调用。

### 2.4 参考 step-by-step 演进

实现路径参考 `references/learn-claude-code-main` 的 step-by-step 思路：

```text
s01 agent loop
s02 tool use
s03 permission
s04 hooks
s05 todo/task
s08 context compact
s09 memory
s10 system prompt
s11 error recovery
```

MVP 不需要完整复刻 Claude Code，而是保留最能解释 harness 的核心机制。

### 2.5 不复制专有源码

`references/claude-code-source-code` 仅用于架构学习和概念研究，不复制其中的专有实现。

## 3. 最终实现效果与使用方式

### 3.1 MVP 期望效果

MVP 完成后，用户可以在项目根目录运行一个极简 CLI：

```bash
rycode chat
```

或通过 Maven 启动：

```bash
mvn -pl rycode-cli spring-boot:run
```

进入交互后，用户输入自然语言任务：

```text
> 阅读当前项目结构，总结核心模块，并创建一个任务清单
```

系统执行流程：

1. CLI 接收用户输入。
2. `AgentRunner` 构造当前 session 的 message history。
3. `ContextManager` 注入项目说明、历史摘要、memory 和 transcript 片段。
4. `ModelClient` 调用 Anthropic API。
5. 模型返回文本或 tool use。
6. `ToolRegistry` 找到对应工具。
7. `PermissionService` 根据规则判断是否允许执行。
8. 工具执行后返回 observation。
9. observation 追加回 message history。
10. loop 继续，直到模型返回最终回答。

### 3.2 CLI MVP 使用方式

MVP CLI 保持简单：

```bash
rycode chat
rycode chat --cwd /path/to/project
rycode chat --model claude-opus-4-8
rycode chat --permission-mode ask
```

交互示例：

```text
rycode> 帮我查看 README 并总结项目目标
assistant> 我需要读取 README.md
[tool: file.read] README.md
assistant> 项目目标是...
```

当模型请求高风险工具时：

```text
assistant wants to run shell command:
  mvn test

Rule: shell commands require confirmation.
Allow? [y/N]
```

### 3.3 后续目标与 MVP 扩展点

以下能力不在核心 MVP 中实现完整产品形态，但属于明确的后续目标。MVP 在具体功能实现时必须为它们保留扩展点：

- slash commands，例如 `/help`、`/clear`、`/compact`
- IDE 集成
- rich terminal UI
- 断点恢复
- 后台任务通知
- 多会话管理
- shell sandbox
- worktree 隔离
- MCP 插件生态
- 自动压缩策略
- token/cost 可视化
- 远程配置和团队策略

这些后续目标对 MVP 的直接影响是：CLI 只能作为 adapter；core 不直接读写终端；工具、权限、session、事件输出都必须通过接口抽象。

## 4. 技术栈选择

### 4.1 基础技术栈

```text
Language: Java 21
Build: Maven
Application: Spring Boot
CLI: picocli
JSON: Jackson
Logging: SLF4J + Logback
Testing: JUnit 5 + Mockito + AssertJ
Model: Anthropic SDK + HTTP Client 双实现
```

### 4.2 选择理由

#### Java 21

适合使用 record、sealed interface、pattern matching 等现代 Java 特性表达不可变数据结构和清晰边界。

#### Maven

多模块结构清晰，生命周期直观，适合 Java 后端学习项目。

#### Spring Boot

用于 CLI 应用装配、配置读取、profile 管理、依赖注入和日志配置。核心模块不依赖 Spring。

#### picocli

成熟、轻量，适合构建 Java CLI。

#### Anthropic SDK + HTTP Client

模型调用层设计为统一接口：

```text
ModelClient
├── AnthropicSdkModelClient
└── AnthropicHttpModelClient
```

SDK 实现用于快速接入真实 API；HTTP Client 实现用于学习底层请求格式，并作为 SDK 外的备用路径。

## 5. Maven 多模块结构

项目采用以下结构：

```text
rycode
├── pom.xml
├── rycode-core
├── rycode-model
├── rycode-tools
├── rycode-cli
└── rycode-server
```

### 5.1 `rycode-core`

Agent Harness 核心，不依赖 Spring。

职责：

- agent loop
- session model
- message model
- tool call model
- hook chain
- permission abstraction
- context abstraction
- error recovery abstraction

核心接口：

```text
AgentRunner
AgentSession
SessionId
Message
ContentBlock
ToolCall
ToolResult
ToolRegistry
ToolProvider
PermissionService
ContextManager
Hook
AgentEvent
AgentEventPublisher
SessionStore
UserConfirmationProvider
```

### 5.2 `rycode-model`

模型调用抽象与实现。

职责：

- 定义 `ModelClient`
- 适配 Anthropic SDK
- 适配 Anthropic HTTP API
- 处理模型请求/响应转换
- 处理 tool schema 转换
- 处理模型调用错误和重试

核心接口：

```text
ModelClient
ModelRequest
ModelResponse
ModelMessage
ModelToolDefinition
ModelUsage
```

### 5.3 `rycode-tools`

工具实现模块。

职责：

- 文件工具
- Shell 工具
- 任务工具
- 上下文/记忆工具
- 工具参数校验
- 工具结果标准化

MVP 工具：

```text
file.read
file.write
file.edit
file.list
file.search
shell.run
task.create
task.update
task.list
context.read
memory.search
```

### 5.4 `rycode-cli`

CLI 启动和用户交互模块。

职责：

- picocli 命令定义
- Spring Boot 应用装配
- 读取 CLI 参数
- 加载配置
- 显示模型输出和工具执行结果
- 接收用户权限确认

MVP 命令：

```text
rycode chat
```

### 5.5 `rycode-server`

后续 REST 服务模块，MVP 暂不实现业务，只在设计中保留。

未来职责：

- 创建 session
- 发送 user message
- 流式返回 assistant output
- 查询任务状态
- 查询 transcript
- 管理多用户、多项目、多会话

为了让 `rycode-server` 后续自然接入，MVP 阶段的 `rycode-core` 不能依赖终端输入输出。用户确认、agent 输出、session 存取都必须通过接口暴露，由 CLI 或 REST adapter 分别实现。

### 5.6 后续扩展模块

以下模块不在 MVP 中落地，但需要在当前模块边界中预留扩展空间：

```text
rycode-mcp        # 后续：MCP 工具发现、注册和调用
rycode-agent-team # 后续：subagent、多代理协作、后台任务
rycode-ui         # 后续：复杂 UI 或 Web UI adapter
```

这些模块应依赖 `rycode-core` 的抽象接口，而不是反向修改核心 loop。

## 6. 核心架构

整体架构：

```text
                 ┌─────────────────────┐
                 │      rycode-cli      │
                 │  picocli + Spring    │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │    AgentRunner      │
                 │      loop core      │
                 └──────────┬──────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│ ContextManager│   │  ModelClient  │   │ ToolRegistry  │
└───────┬───────┘   └───────┬───────┘   └───────┬───────┘
        │                   │                   │
        ▼                   ▼                   ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│ transcript    │   │ Anthropic API │   │ file/shell/   │
│ summary       │   │ SDK / HTTP    │   │ task/context  │
│ memory        │   └───────────────┘   └───────┬───────┘
└───────────────┘                               │
                                                ▼
                                      ┌───────────────────┐
                                      │ PermissionService │
                                      └───────────────────┘
```

## 7. Agent Loop 设计

### 7.1 最小循环

核心循环：

```text
while session is active:
    build model request
    call model
    append assistant response

    if response has no tool call:
        return final text

    for each tool call:
        check permission
        execute tool
        append tool result

    continue loop
```

### 7.2 Loop 边界

MVP 需要设置明确边界：

- 最大 loop 次数
- 单轮最大工具调用数
- 模型调用超时
- 工具执行超时
- session 取消标记
- 错误恢复策略

默认建议：

```text
maxLoopIterations = 20
maxToolCallsPerTurn = 10
modelTimeout = 120s
toolTimeout = 60s
```

### 7.3 AgentRunner 职责

`AgentRunner` 只负责编排，不直接实现模型、工具、权限、上下文细节。

它依赖：

```text
ModelClient
ToolRegistry
PermissionService
ContextManager
HookChain
SessionStore
AgentEventPublisher
UserConfirmationProvider
```

其中 `AgentEventPublisher` 用于向 CLI、REST 或未来 UI 输出结构化事件；`UserConfirmationProvider` 用于抽象权限确认，不允许 `AgentRunner` 直接读写终端。

## 8. 模型调用层设计

### 8.1 统一接口

```text
ModelClient.complete(ModelRequest request) -> ModelResponse
```

`ModelRequest` 包含：

- model
- system prompt
- messages
- tool definitions
- max tokens
- temperature
- metadata

`ModelResponse` 包含：

- assistant message
- text blocks
- tool use blocks
- stop reason
- token usage
- raw response reference

### 8.2 SDK 与 HTTP 双实现

```text
ModelClient
├── AnthropicSdkModelClient
└── AnthropicHttpModelClient
```

MVP 默认优先使用 SDK 实现。HTTP 实现保留为可配置选项：

```yaml
rycode:
  model:
    provider: anthropic
    client-type: sdk # sdk | http
    model: claude-opus-4-8
```

### 8.3 API Key 管理

不在源码中硬编码任何密钥。

配置来源：

```text
ANTHROPIC_API_KEY
application.yml / environment variable binding
```

启动时校验：

- 如果缺少 API Key，CLI 给出明确错误信息。
- 不把 API Key 写入日志。

## 9. Tool Use 设计

### 9.1 工具抽象

每个工具都实现统一接口：

```text
Tool
├── name
├── description
├── inputSchema
└── execute(input, context) -> ToolResult
```

工具必须满足：

- 名称稳定
- 描述清晰
- 输入 schema 明确
- 输出结构化
- 错误显式返回

### 9.2 ToolRegistry 与 ToolProvider

`ToolRegistry` 负责：

- 注册工具
- 根据名称查找工具
- 输出模型可理解的 tool definitions
- 校验重复名称

工具来源通过 `ToolProvider` 扩展：

```text
ToolProvider
├── BuiltInToolProvider  # MVP：Java 内置工具
├── McpToolProvider      # 后续：MCP 工具
└── PluginToolProvider   # 后续：插件工具
```

MVP 只实现 `BuiltInToolProvider`，但 `ToolRegistry` 不应假设工具一定来自本地 Java class。

### 9.3 MVP 工具清单

#### 文件工具

```text
file.read   读取文件内容
file.write  写入文件
file.edit   精确字符串替换
file.list   列出目录文件
file.search 搜索文件名或文本
```

权限建议：

- `file.read`、`file.list`、`file.search` 可按规则默认允许。
- `file.write`、`file.edit` 默认需要确认。

#### Shell 工具

```text
shell.run 执行 shell 命令
```

权限建议：

- 默认需要确认。
- 支持 allow/deny 规则。
- 禁止交互式命令。
- 设置超时。

#### 任务工具

```text
task.create 创建任务
task.update 更新任务状态
task.list   查看任务列表
```

MVP 可先使用内存态 task store，后续扩展为文件态或数据库态。

#### 上下文/记忆工具

```text
context.read 读取当前项目上下文摘要
memory.search 搜索长期记忆
```

## 10. 权限系统设计

### 10.1 目标

权限系统是 Agent Harness 的安全边界。模型可以请求行动，但不能绕过 harness 执行行动。

### 10.2 规则配置策略

MVP 使用规则配置：

```yaml
rycode:
  permissions:
    rules:
      - tool: file.read
        paths: ["**"]
        decision: allow
      - tool: file.write
        paths: ["**"]
        decision: ask
      - tool: shell.run
        commands: ["mvn test", "git status", "ls *"]
        decision: allow
      - tool: shell.run
        commands: ["rm *", "git push *"]
        decision: deny
```

### 10.3 决策类型

```text
allow  直接允许
ask    请求用户确认
deny   拒绝执行
```

### 10.4 PermissionService 职责

```text
PermissionService.evaluate(ToolCall, SessionContext) -> PermissionDecision
```

决策结果包含：

- decision
- matched rule
- reason
- whether user confirmation is required

### 10.5 用户确认

CLI 通过 `PermissionPrompter` 请求确认。

`rycode-core` 不直接依赖终端输入，只依赖抽象：

```text
UserConfirmationProvider
```

## 11. 上下文与记忆设计

MVP 采用三层上下文：

```text
transcript: 当前会话完整记录
summary:    历史摘要，用于压缩长上下文
memory:     跨会话长期事实
```

### 11.1 Transcript

记录当前 session 的完整消息和工具结果。

用途：

- 构造模型请求
- 调试 agent 行为
- 后续做 summary

MVP 存储方式：

```text
.rycode/sessions/<session-id>/transcript.jsonl
```

### 11.2 Summary

当 transcript 过长时，生成摘要并继续使用摘要作为上下文。

MVP 可以先实现手动或阈值触发：

```text
当消息数量超过 N 或 token 估算超过阈值时，调用模型生成 summary
```

存储方式：

```text
.rycode/sessions/<session-id>/summary.md
```

### 11.3 Memory

保存跨会话长期事实，例如：

- 用户偏好
- 项目约束
- 常用命令
- 已确认的设计决策

MVP 存储方式：

```text
.rycode/memory/*.md
```

格式可参考：

```markdown
---
name: short-slug
type: project | user | feedback | reference
---

记忆内容
```

### 11.4 ContextManager

`ContextManager` 负责组装模型上下文：

```text
system prompt
+ project instructions
+ relevant memory
+ session summary
+ recent transcript messages
+ user message
```

## 12. System Prompt 设计

System prompt 不写成一个巨大字符串，而是按 section 组装：

```text
Base identity
Project instructions
Tool usage rules
Permission rules
Context policy
Output style
Current environment
```

MVP 中，`SystemPromptBuilder` 从以下来源组装：

- 内置 base prompt
- 项目 `CLAUDE.md`
- 当前工作目录信息
- 权限摘要
- 可用工具列表
- memory/context 摘要

## 13. Hook 设计

Hook 用于在不修改 agent loop 的情况下扩展行为。

MVP 支持：

```text
BeforeModelCallHook
AfterModelCallHook
BeforeToolCallHook
AfterToolCallHook
OnErrorHook
```

用途：

- 记录日志
- 收集 usage
- 做输入校验
- 做工具调用审计
- 触发 summary

Hook 不承担核心业务决策，避免把控制流变复杂。

## 14. Error Recovery 设计

MVP 需要处理以下错误：

### 14.1 模型调用错误

包括：

- API key 缺失
- 网络超时
- rate limit
- 模型响应解析失败

策略：

- 明确错误信息
- 对临时错误做有限重试
- 对配置错误直接失败

### 14.2 工具执行错误

包括：

- 文件不存在
- 权限拒绝
- shell 超时
- 参数不合法

策略：

- 工具返回结构化错误
- 错误作为 observation 交还模型
- 不直接中断整个 session，除非是系统级错误

### 14.3 Loop 保护

包括：

- 超过最大循环次数
- 工具调用重复失败
- 模型持续请求不存在的工具

策略：

- 返回明确停止原因
- 写入 transcript
- 提示用户下一步可调整输入或权限规则

## 15. 数据模型与事件模型草案

### 15.1 Message

```text
Message
├── role: system | user | assistant | tool
├── content: List<ContentBlock>
├── createdAt
└── metadata
```

### 15.2 ContentBlock

```text
ContentBlock
├── TextBlock
├── ToolUseBlock
└── ToolResultBlock
```

### 15.3 ToolCall

```text
ToolCall
├── id
├── name
├── inputJson
└── createdAt
```

### 15.4 ToolResult

```text
ToolResult
├── toolCallId
├── success
├── output
├── error
└── metadata
```

### 15.5 Task

```text
Task
├── id
├── title
├── status: pending | in_progress | completed | failed
├── notes
├── createdAt
└── updatedAt
```

### 15.6 AgentEvent

核心层输出结构化事件，CLI、REST 和未来 UI 分别决定如何展示。

```text
AgentEvent
├── AssistantTextDelta
├── AssistantMessageCompleted
├── ToolCallStarted
├── ToolCallCompleted
├── PermissionRequested
├── PermissionResolved
├── TaskUpdated
├── SessionSummaryUpdated
└── ErrorOccurred
```

MVP CLI 可以把事件打印为普通文本；REST 服务后续可以把同一批事件转换为 SSE、WebSocket 消息或 HTTP streaming response。

### 15.7 Store 抽象

session、task、transcript、summary、memory 都通过 store 接口管理，避免核心逻辑绑定具体存储方式。

```text
SessionStore
TaskStore
TranscriptStore
SummaryStore
MemoryStore
```

MVP 可以使用本地文件实现；后续 REST 服务可以替换为数据库实现。

数据对象优先使用不可变 record，更新时返回新对象。

## 16. 配置设计

MVP 配置示例：

```yaml
rycode:
  model:
    provider: anthropic
    client-type: sdk
    model: claude-opus-4-8
    max-tokens: 4096
    timeout: 120s

  agent:
    max-loop-iterations: 20
    max-tool-calls-per-turn: 10

  workspace:
    root: .
    session-dir: .rycode/sessions
    memory-dir: .rycode/memory

  permissions:
    default-decision: ask
    rules:
      - tool: file.read
        paths: ["**"]
        decision: allow
      - tool: file.list
        paths: ["**"]
        decision: allow
      - tool: file.search
        paths: ["**"]
        decision: allow
      - tool: file.write
        paths: ["**"]
        decision: ask
      - tool: file.edit
        paths: ["**"]
        decision: ask
      - tool: shell.run
        commands: ["git status", "mvn test", "mvn -q test"]
        decision: ask
```

## 17. 测试策略

MVP 测试按层覆盖。

### 17.1 Unit Tests

重点测试：

- Agent loop 在 tool use 和 final answer 下的行为
- ToolRegistry 注册和查找
- PermissionService 规则匹配
- ContextManager 上下文组装
- SystemPromptBuilder section 拼装
- 文件工具参数校验
- Shell 工具超时和错误返回

### 17.2 Integration Tests

重点测试：

- FakeModelClient 驱动完整 agent loop
- 模型返回 tool call 后工具执行并回填结果
- 权限 ask/allow/deny 三种路径
- session transcript 写入

即使 MVP 使用真实 Anthropic API，也应保留 `FakeModelClient` 用于稳定测试，避免单元测试依赖外部网络和 API 成本。

### 17.3 Manual Tests

通过 CLI 验证：

```text
读取 README
列出项目文件
执行 git status
创建任务清单
拒绝一个 shell 命令
```

## 18. MVP 实现边界

### 18.1 MVP 必须包含

- Maven 多模块结构
- `rycode-core` 核心 loop
- `rycode-model` 真实 Anthropic API 调用
- `AnthropicSdkModelClient`
- `AnthropicHttpModelClient` 的最小实现或清晰接口占位
- `rycode-tools` 文件、Shell、任务、上下文/记忆工具
- 规则配置权限系统
- transcript、summary、memory 三层上下文设计的 MVP 实现
- `rycode-cli` 极简交互入口
- 核心逻辑单元测试

### 18.2 MVP 暂不实现但必须预留扩展点

以下能力属于后续目标，MVP 不实现完整产品形态，但当前设计和代码边界必须为它们保留扩展路径：

- 完整 REST 服务
- 多用户管理
- 浏览器工具
- MCP 插件
- 子代理并行执行
- background jobs
- worktree 隔离
- rich terminal UI
- 自动 PR/推送/远程协作
- 生产级 sandbox

对应约束：

- REST 服务依赖 `AgentRunner`、`SessionStore`、`AgentEventPublisher` 等 core 抽象接入。
- MCP 插件通过 `ToolProvider` 接入，不改变已有工具执行流程。
- 多代理和后台任务通过独立 `AgentSession`、`TaskId`、`SessionId` 扩展，不使用全局状态。
- 复杂 UI 消费 `AgentEvent`，不解析 CLI 文本输出。

## 19. 后续路线图

### Phase 1：核心 MVP

目标：跑通真实模型 + 工具调用 + 权限 + 上下文。

包含：

- agent loop
- model client
- tool registry
- permission service
- file/shell/task/context tools
- transcript/summary/memory MVP
- CLI chat

### Phase 2：上下文增强

包含：

- 更准确的 token 估算
- 自动 summary
- memory 写入工具
- memory 检索评分
- project instruction 多文件加载

### Phase 3：CLI 产品化

包含：

- slash commands
- session resume
- terminal UI 优化
- 权限规则编辑
- 工具调用可视化
- cost/usage 展示

### Phase 4：REST 服务

包含：

- `rycode-server`
- session API
- streaming response
- task API
- transcript API
- Web UI 或外部系统集成

### Phase 5：高级 Agent Harness

包含：

- subagent
- background tasks
- cron/scheduler
- worktree isolation
- MCP/plugin integration
- multi-agent coordination

## 20. 关键设计决策汇总

| 决策点 | 结论 |
|---|---|
| MVP 目标 | Agent Harness 框架雏形 |
| 运行形态 | 核心库优先 + 极简 CLI |
| 工程结构 | Maven 多模块 |
| 核心模块是否依赖 Spring | 不依赖 |
| CLI 是否使用 Spring Boot | 使用 |
| 模型调用 | 真实 Anthropic API |
| 模型客户端 | SDK + HTTP Client 双实现 |
| 权限策略 | 规则配置，allow/ask/deny |
| 工具范围 | 文件、Shell、任务、上下文/记忆 |
| 上下文层 | transcript + summary + memory |
| REST 服务 | 后续目标，MVP 预留 adapter 和事件流扩展点 |
| 子代理/后台任务 | 后续目标，MVP 通过 SessionId/TaskId/Store 抽象预留扩展点 |

## 21. 开放问题

以下问题不阻塞 MVP 设计，可在实现计划阶段继续细化：

1. `AnthropicHttpModelClient` 是 MVP 同步完成，还是先实现接口和测试桩。
2. summary 是手动触发，还是达到阈值后自动触发。
3. memory 写入是否允许模型主动调用，还是先由用户确认后写入。
4. Shell 工具的安全策略是否需要在 MVP 中支持工作目录白名单。
5. `rycode-server` 模块是否先创建空模块，还是等 Phase 4 再添加。
6. `AgentEvent` 是否从 MVP 第一版就支持流式 text delta，还是先支持 message completed 事件。

## 22. 成功标准

MVP 完成时，应满足：

1. 用户可以通过 CLI 发起一次多轮 agent session。
2. 模型可以真实调用 Anthropic API。
3. 模型可以请求文件、Shell、任务、上下文工具。
4. 工具调用经过权限规则判断。
5. 工具结果会回填给模型，并驱动下一轮响应。
6. session transcript 被持久化。
7. summary 和 memory 有明确存储与读取路径。
8. 核心逻辑可通过 FakeModelClient 做稳定自动化测试。
9. `rycode-core` 不依赖 Spring、CLI、REST 或终端输入输出，可被 CLI 和未来 REST 复用。
10. MVP 代码边界保留生产级 CLI、REST、多代理、MCP 和复杂 UI 的扩展路径。

## 23. 自检结果

- 无未定义的 MVP 核心模块。
- REST、子代理、后台任务、MCP、复杂 UI 等能力已明确标记为后续目标，而不是非目标。
- 技术栈、模块边界、核心数据流和权限策略保持一致。
- 设计聚焦单一实现计划：Java Agent Harness 核心 MVP。
- 开放问题均不阻塞后续实现计划制定。
