# rycode 实现路线图

> 版本：v0.1
> 日期：2026-06-11
> 目的：按合理实现顺序规划 rycode MVP 开发任务

## 当前完成情况

### ✅ 已完成模块

#### 1. 核心数据模型（rycode-core/message）
- [x] `Message` - 消息抽象
- [x] `MessageRole` - 角色枚举
- [x] `ContentBlock` - 内容块接口
- [x] `TextBlock` - 文本块
- [x] `ToolUseBlock` - 工具使用块
- [x] `ToolResultBlock` - 工具结果块

**提交**：`40b127d`, `7b59df8`

#### 2. 工具系统抽象（rycode-core/tool）
- [x] `Tool` - 工具接口
- [x] `ToolDefinition` - 工具定义
- [x] `ToolCall` - 工具调用
- [x] `ToolResult` - 工具结果
- [x] `ToolRegistry` - 工具注册表

**提交**：`40b127d`, `7b59df8`

#### 3. 权限系统抽象（rycode-core/permission）
- [x] `PermissionService` - 权限服务接口
- [x] `PermissionDecision` - 权限决策
- [x] `PermissionDecisionType` - 决策类型（ALLOW/ASK/DENY）
- [x] `UserConfirmationProvider` - 用户确认提供者接口

**提交**：`40b127d`, `7b59df8`

#### 4. 模型调用抽象（rycode-core/model）
- [x] `ModelClient` - 模型客户端接口
- [x] `ModelRequest` - 模型请求
- [x] `ModelResponse` - 模型响应

**提交**：`40b127d`, `7b59df8`

#### 5. Agent Loop 核心（rycode-core/agent）
- [x] `AgentRunner` - agent 运行器
- [x] `AgentRunRequest` - 运行请求
- [x] `AgentRunResult` - 运行结果
- [x] 工具执行异常处理（fallback）
- [x] 权限检查集成
- [x] 最大循环次数和工具调用限制

**提交**：`40b127d`, `7b59df8`, `d7ab061`

#### 6. 测试覆盖
- [x] `MessageTest` - 消息测试
- [x] `AgentRunnerTest` - Agent 运行器测试（happy path + 边界）
- [x] `PermissionDecisionTest` - 权限决策测试
- [x] `ToolResultTest` - 工具结果测试
- [x] `TestTools` - 测试用假工具

**提交**：`2b1dc27`, `7b59df8`

---

## 🚧 下一步实现任务（按优先级排序）

### Phase 1：模型调用层（rycode-model）- 优先级 P0

#### 任务 1.1：模块基础设施
- [ ] 创建 `rycode-model` Maven 模块
- [ ] 配置 pom.xml 依赖（Anthropic SDK、HTTP Client、Jackson）
- [ ] 添加到父 pom.xml 的 modules

**验证标准**：`mvn clean compile` 成功

#### 任务 1.2：Anthropic SDK 模型客户端实现
- [ ] 实现 `AnthropicSdkModelClient implements ModelClient`
- [ ] 添加 API Key 配置和验证
- [ ] 实现 message/tool schema 转换
- [ ] 实现错误处理和超时控制
- [ ] 添加日志记录

**验证标准**：
- 能够真实调用 Anthropic API
- 错误信息清晰
- 工具定义正确转换

#### 任务 1.3：模型客户端单元测试
- [ ] 使用 WireMock 或 mock 测试 SDK 转换逻辑
- [ ] 测试错误处理路径
- [ ] 测试超时机制

**验证标准**：测试覆盖 > 80%

#### 任务 1.4：HTTP 模型客户端（可选）
- [ ] 实现 `AnthropicHttpModelClient implements ModelClient`
- [ ] 复用 SDK 客户端的转换逻辑
- [ ] 添加配置选项（application.yml 中 client-type: sdk|http）

**说明**：可延后到 Phase 2，优先用 SDK 实现完成端到端验证

---

### Phase 2：工具实现（rycode-tools）- 优先级 P0

#### 任务 2.1：模块基础设施
- [ ] 创建 `rycode-tools` Maven 模块
- [ ] 依赖 `rycode-core`
- [ ] 添加到父 pom.xml 的 modules


#### 任务 2.2：文件工具实现
- [ ] `FileReadTool` - 读取文件内容
- [ ] `FileWriteTool` - 写入文件
- [ ] `FileEditTool` - 精确字符串替换
- [ ] `FileListTool` - 列出目录文件
- [ ] `FileSearchTool` - 搜索文件名或文本内容
- [ ] 参数校验（路径、权限、大小限制）
- [ ] 错误处理（文件不存在、权限拒绝、编码问题）

**验证标准**：
- 每个工具有单元测试
- 错误场景有覆盖
- 参数校验完备

#### 任务 2.3：Shell 工具实现
- [ ] `ShellRunTool` - 执行 shell 命令
- [ ] 超时控制
- [ ] 工作目录设置
- [ ] 禁止交互式命令
- [ ] 输出限制（防止过大输出）
- [ ] 错误码处理

**验证标准**：
- 能执行简单命令（ls, git status, mvn test）
- 超时能正常中断
- 错误信息清晰

#### 任务 2.4：任务工具实现（简化版）
- [ ] `TaskCreateTool` - 创建任务
- [ ] `TaskUpdateTool` - 更新任务状态
- [ ] `TaskListTool` - 列出任务
- [ ] 内存态 TaskStore 实现

**说明**：MVP 阶段使用内存存储，后续扩展为持久化

#### 任务 2.5：上下文/记忆工具（占位）
- [ ] `ContextReadTool` - 读取项目上下文摘要
- [ ] `MemorySearchTool` - 搜索长期记忆
- [ ] 返回静态占位内容（真实实现在 Phase 4）

**说明**：先提供接口，避免阻塞 agent loop 测试

---

### Phase 3：权限系统实现（rycode-core）- 优先级 P0

#### 任务 3.1：规则配置模型
- [ ] `PermissionRule` - 权限规则数据类
- [ ] `PermissionConfig` - 权限配置加载
- [ ] 规则匹配逻辑（工具名、路径模式、命令模式）


#### 任务 3.2：PermissionService 实现
- [ ] `RuleBasedPermissionService implements PermissionService`
- [ ] 规则优先级处理
- [ ] 默认决策策略
- [ ] 匹配原因记录

**验证标准**：
- 规则匹配测试覆盖
- allow/ask/deny 决策正确
- 多规则优先级正确

#### 任务 3.3：配置文件定义
- [ ] 定义 application.yml 权限配置格式
- [ ] 添加默认权限规则示例


---

### Phase 4：CLI 模块（rycode-cli）- 优先级 P1

#### 任务 4.1：模块基础设施
- [ ] 创建 `rycode-cli` Maven 模块（Spring Boot）
- [ ] 依赖 `rycode-core`, `rycode-model`, `rycode-tools`
- [ ] 配置 picocli + Spring Boot 集成
- [ ] 添加到父 pom.xml 的 modules


#### 任务 4.2：配置管理
- [ ] 创建 `application.yml` 配置模板
- [ ] API Key 环境变量绑定
- [ ] 模型配置（model, max-tokens, timeout）
- [ ] Agent 配置（max-loop-iterations, max-tool-calls-per-turn）
- [ ] 工作空间配置（root, session-dir, memory-dir）
- [ ] 权限配置


#### 任务 4.3：Spring Boot 应用装配
- [ ] `RycodeCliApplication` - 主类
- [ ] `ModelClientConfiguration` - 模型客户端 Bean
- [ ] `ToolRegistryConfiguration` - 工具注册 Bean
- [ ] `PermissionServiceConfiguration` - 权限服务 Bean
- [ ] `AgentRunnerConfiguration` - AgentRunner Bean


#### 任务 4.4：CLI 命令实现
- [ ] `ChatCommand` - chat 命令
- [ ] 参数解析（--cwd, --model, --permission-mode）
- [ ] 简单 REPL 循环
- [ ] 用户输入读取
- [ ] 模型输出展示
- [ ] 工具调用展示
- [ ] 权限确认提示

**验证标准**：
- `rycode chat` 能启动交互
- 能读取用户输入并调用 AgentRunner
- 模型响应能正确展示
- 工具调用可视化
- 权限请求能正确提示

#### 任务 4.5：UserConfirmationProvider 实现
- [ ] `CliUserConfirmationProvider implements UserConfirmationProvider`
- [ ] 终端输入读取
- [ ] 确认提示格式化（工具名、参数、原因）
- [ ] 默认拒绝（防止误操作）


---

### Phase 5：上下文与记忆系统（rycode-core）- 优先级 P1

#### 任务 5.1：Session 抽象
- [ ] `SessionId` - 会话 ID
- [ ] `AgentSession` - 会话抽象
- [ ] `SessionStore` - 会话存储接口
- [ ] `FileSessionStore` - 文件存储实现


#### 任务 5.2：Transcript 管理
- [ ] `TranscriptStore` - transcript 存储接口
- [ ] `JsonlTranscriptStore` - JSONL 文件实现
- [ ] `.rycode/sessions/<session-id>/transcript.jsonl` 格式


#### 任务 5.3：Summary 管理（简化版）
- [ ] `SummaryStore` - summary 存储接口
- [ ] `MarkdownSummaryStore` - Markdown 文件实现
- [ ] `.rycode/sessions/<session-id>/summary.md` 格式
- [ ] 手动触发 summary 生成（自动化延后）


#### 任务 5.4：Memory 管理（简化版）
- [ ] `MemoryStore` - memory 存储接口
- [ ] `MarkdownMemoryStore` - Markdown 文件实现
- [ ] `.rycode/memory/*.md` 格式
- [ ] 基于文件名的简单检索（向量检索延后）


#### 任务 5.5：ContextManager 实现
- [ ] `ContextManager` - 上下文管理器
- [ ] 组装 system prompt sections
- [ ] 加载项目 CLAUDE.md
- [ ] 注入 memory 片段
- [ ] 注入 session summary
- [ ] 注入 recent transcript messages
- [ ] Token 估算（简单字符计数）


#### 任务 5.6：SystemPromptBuilder 实现
- [ ] `SystemPromptBuilder` - system prompt 构建器
- [ ] 内置 base identity prompt
- [ ] 项目指令 section
- [ ] 工具使用规则 section
- [ ] 权限规则摘要 section
- [ ] 当前环境信息 section


---

### Phase 6：Hook 系统（rycode-core）- 优先级 P2

#### 任务 6.1：Hook 抽象
- [ ] `Hook` - hook 接口
- [ ] `BeforeModelCallHook`
- [ ] `AfterModelCallHook`
- [ ] `BeforeToolCallHook`
- [ ] `AfterToolCallHook`
- [ ] `OnErrorHook`
- [ ] `HookChain` - hook 链执行器


#### 任务 6.2：内置 Hook 实现
- [ ] `LoggingHook` - 日志记录
- [ ] `UsageCollectionHook` - token/cost 统计
- [ ] `AuditHook` - 工具调用审计


---

### Phase 7：错误恢复与边界保护（rycode-core）- 优先级 P0

#### 任务 7.1：错误分类
- [ ] `AgentError` - agent 错误基类
- [ ] `ModelError` - 模型调用错误
- [ ] `ToolError` - 工具执行错误
- [ ] `PermissionError` - 权限错误
- [ ] `LoopError` - 循环边界错误


#### 任务 7.2：重试策略（ModelClient 层）
- [ ] 临时错误重试（网络超时、rate limit）
- [ ] 配置错误直接失败（API key 缺失）
- [ ] 指数退避
- [ ] 重试日志


#### 任务 7.3：Loop 保护增强
- [ ] 工具重复失败检测
- [ ] 未知工具请求统计
- [ ] 停止原因详细记录
- [ ] 优雅降级策略


---

### Phase 8：事件系统（rycode-core）- 优先级 P1

#### 任务 8.1：AgentEvent 模型
- [ ] `AgentEvent` - 事件基类
- [ ] `AssistantTextDelta`
- [ ] `AssistantMessageCompleted`
- [ ] `ToolCallStarted`
- [ ] `ToolCallCompleted`
- [ ] `PermissionRequested`
- [ ] `PermissionResolved`
- [ ] `TaskUpdated`
- [ ] `SessionSummaryUpdated`
- [ ] `ErrorOccurred`


#### 任务 8.2：AgentEventPublisher 实现
- [ ] `AgentEventPublisher` - 事件发布器接口
- [ ] `InMemoryEventPublisher` - 内存实现
- [ ] 在 AgentRunner 中集成事件发布


#### 任务 8.3：CLI 事件订阅
- [ ] CLI 订阅事件流
- [ ] 事件展示格式化
- [ ] 进度指示器


---

### Phase 9：集成测试与端到端验证（全模块）- 优先级 P0

#### 任务 9.1：FakeModelClient 测试工具
- [ ] `FakeModelClient implements ModelClient`
- [ ] 预设响应序列
- [ ] 工具调用记录
- [ ] 用于稳定的集成测试


#### 任务 9.2：端到端测试场景
- [ ] 读取 README 并总结
- [ ] 列出项目文件
- [ ] 执行 git status
- [ ] 创建任务清单
- [ ] 权限拒绝场景
- [ ] 工具失败恢复场景
- [ ] 多轮对话场景


#### 任务 9.3：手动测试清单
- [ ] 真实 Anthropic API 调用测试
- [ ] 文件工具测试
- [ ] Shell 工具测试
- [ ] 权限系统测试
- [ ] 上下文加载测试
- [ ] 错误处理测试


---

### Phase 10：文档与完善（全模块）- 优先级 P2

#### 任务 10.1：代码文档
- [ ] 核心接口 Javadoc
- [ ] 关键类注释
- [ ] 包级说明文档


#### 任务 10.2：用户文档
- [ ] README 更新（安装、配置、使用）
- [ ] 配置文件示例
- [ ] 常见问题 FAQ
- [ ] 开发指南


#### 任务 10.3：架构文档同步
- [ ] 更新 DESIGN.md（反映实际实现）
- [ ] 更新 CLAUDE.md（反映当前工作约束）
- [ ] 补充架构决策记录（ADR）


---

## 📊 估算总结

| Phase | 估算工时 | 优先级 |
|-------|----------|--------|
| Phase 1: 模型调用层 | 10-13h | P0 |
| Phase 2: 工具实现 | 15-19h | P0 |
| Phase 3: 权限系统 | 7-9h | P0 |
| Phase 4: CLI 模块 | 14-17h | P1 |
| Phase 5: 上下文与记忆 | 19-25h | P1 |
| Phase 6: Hook 系统 | 5-7h | P2 |
| Phase 7: 错误恢复 | 8-11h | P0 |
| Phase 8: 事件系统 | 8-11h | P1 |
| Phase 9: 集成测试 | 9-12h | P0 |
| Phase 10: 文档 | 9-12h | P2 |
| **总计** | **104-136h** | - |

## 🎯 建议实施顺序

### 第一迭代：跑通基本 Agent Loop（30-35h）
1. Phase 1: 模型调用层（任务 1.1-1.3）
2. Phase 2: 工具实现（任务 2.1-2.3，仅文件和 Shell）
3. Phase 3: 权限系统（任务 3.1-3.3）
4. Phase 4: CLI 模块（任务 4.1-4.5）

**验证里程碑**：能通过 CLI 与真实模型交互，执行文件和 Shell 工具

### 第二迭代：完善上下文与工具（25-30h）
5. Phase 2: 任务工具（任务 2.4-2.5）
6. Phase 5: 上下文与记忆（任务 5.1-5.6）
7. Phase 7: 错误恢复（任务 7.1-7.3）

**验证里程碑**：支持长会话、项目上下文、任务管理、错误恢复

### 第三迭代：生产级增强（20-25h）
8. Phase 6: Hook 系统（任务 6.1-6.2）
9. Phase 8: 事件系统（任务 8.1-8.3）
10. Phase 9: 集成测试（任务 9.1-9.3）

**验证里程碑**：稳定的测试覆盖、可扩展的事件流

### 第四迭代：文档与发布（10-15h）
11. Phase 10: 文档与完善（任务 10.1-10.3）

**验证里程碑**：MVP 可交付

---

## ✅ 下一步行动

**建议立即开始**：Phase 1 - 模型调用层（任务 1.1）

创建 `rycode-model` 模块，开始实现真实的 Anthropic API 集成。

---

*最后更新：2026-06-11*
