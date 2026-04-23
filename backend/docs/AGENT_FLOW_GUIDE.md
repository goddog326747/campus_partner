# Agent Flow 链路设计指南

## 概述

Agent Flow 是一个灵活的 AI 工作流编排框架，基于 LangChain4j 构建，支持两种核心执行模式：

1. **规划执行模式 (Planning & Execution)** - 适合确定性任务
2. **ReAct 模式 (Reasoning + Acting)** - 适合探索性任务

## 架构设计

### 核心组件

```
┌─────────────────────────────────────────────────────────────────┐
│                        Agent Flow 架构                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐      │
│  │  FlowNode    │◄──►│   FlowEdge   │◄──►│ FlowContext  │      │
│  │   (节点)      │    │    (边)       │    │   (上下文)    │      │
│  └──────────────┘    └──────────────┘    └──────────────┘      │
│         │                   │                   │               │
│         ▼                   ▼                   ▼               │
│  ┌──────────────────────────────────────────────────────┐      │
│  │                    FlowEngine                         │      │
│  │              (Planning / ReAct / Hybrid)              │      │
│  └──────────────────────────────────────────────────────┘      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 节点类型 (NodeType)

| 类型 | 说明 | 使用场景 |
|------|------|----------|
| START | 起始节点 | 流程入口 |
| LLM | AI 模型节点 | 调用大语言模型 |
| TOOL | 工具节点 | 调用外部工具/函数 |
| CONDITION | 条件节点 | 分支判断 |
| LOOP | 循环节点 | 重复执行 |
| PARALLEL | 并行节点 | 并发执行 |
| END | 结束节点 | 流程出口 |

## 两种执行模式详解

### 1. 规划执行模式 (Planning Mode)

**特点：**
- 先规划所有步骤，再按顺序执行
- 执行路径在开始前就已确定
- 适合确定性任务

**执行流程：**
```
输入 → 分析需求 → 查询风格 → 获取话题 → 生成内容 → 检查敏感词 → 输出
```

**适用场景：**
- 帖子生成
- 数据报表生成
- 固定流程的自动化任务

**代码示例：**
```java
AgentFlow flow = AgentFlow.builder()
    .name("PostGenerationFlow")
    .mode(FlowMode.PLANNING)
    
    // 添加节点
    .addNode(FlowNode.builder()
        .nodeId("analyze")
        .name("AnalyzeRequest")
        .type(NodeType.LLM)
        .config("systemPrompt", "分析需求...")
        .config("userPromptTemplate", "用户想要：{{input.topic}}")
        .build())
    
    // 定义边
    .addEdge("start", "analyze")
    .addEdge("analyze", "generate")
    .addEdge("generate", "end")
    
    .build();

// 执行
FlowResult result = flow.execute(Map.of("topic", "周末爬山"));
```

### 2. ReAct 模式 (ReAct Mode)

**ReAct = Reasoning (推理) + Acting (行动)**

**特点：**
- 边推理边执行
- 根据中间结果动态调整下一步
- 支持循环直到找到答案

**执行循环：**
```
Thought (思考) → Action (行动) → Observation (观察) → [循环] → Answer (回答)
```

**适用场景：**
- 智能问答
- 复杂问题解答
- 需要多轮交互的任务

**代码示例：**
```java
AgentFlow flow = AgentFlow.builder()
    .name("ReActQAFlow")
    .mode(FlowMode.REACT)
    
    // Thought 节点
    .addNode(FlowNode.builder()
        .nodeId("thought")
        .name("Thought")
        .type(NodeType.LLM)
        .config("systemPrompt", "分析当前情况，决定下一步...")
        .build())
    
    // Action 节点
    .addNode(FlowNode.builder()
        .nodeId("action")
        .name("Action")
        .type(NodeType.TOOL)
        .build())
    
    // 定义边（支持循环）
    .addEdge("start", "thought")
    .addEdge("thought", "action")
    .addEdge("action", "thought")  // 循环回 Thought
    .addEdge("thought", "answer", ctx -> {
        // 条件：当找到答案时
        String thought = ctx.getVariable("lastThought");
        return thought != null && thought.contains("FINAL ANSWER");
    })
    
    .build();
```

## API 使用指南

### 1. 生成帖子（规划执行模式）

**请求：**
```http
POST /api/agent-flow/post/generate
Content-Type: application/json

{
    "topic": "周末爬山",
    "style": "活泼",
    "requirements": "吸引大学生参加"
}
```

**响应：**
```json
{
    "code": 200,
    "data": {
        "title": "周末爬山约起来！🏔️",
        "content": "小伙伴们，周末一起去爬山吧！...",
        "executionId": "550e8400-e29b-41d4-a716-446655440000",
        "executionTime": 3500,
        "nodesExecuted": 6
    }
}
```

### 2. 智能问答（ReAct 模式）

**请求：**
```http
POST /api/agent-flow/qa
Content-Type: application/json

{
    "question": "最近有什么热门的户外活动？"
}
```

**响应：**
```json
{
    "code": 200,
    "data": {
        "answer": "根据搜索结果，最近热门的户外活动有...",
        "executionId": "550e8400-e29b-41d4-a716-446655440001",
        "executionTime": 5200,
        "nodesExecuted": 8
    }
}
```

### 3. 查询执行历史

**请求：**
```http
GET /api/agent-flow/history/{executionId}
```

**响应：**
```json
{
    "code": 200,
    "data": {
        "executionId": "550e8400-e29b-41d4-a716-446655440000",
        "flowId": "post-generation-flow",
        "status": "COMPLETED",
        "startTime": "2024-01-15T10:30:00",
        "endTime": "2024-01-15T10:30:03",
        "executionTimeMs": 3500,
        "steps": [
            {
                "nodeId": "analyze",
                "nodeName": "AnalyzeRequest",
                "success": true,
                "executionTimeMs": 800,
                "timestamp": "2024-01-15T10:30:00"
            },
            {
                "nodeId": "queryStyle",
                "nodeName": "QueryUserStyle",
                "success": true,
                "executionTimeMs": 200,
                "timestamp": "2024-01-15T10:30:01"
            }
        ]
    }
}
```

## 自定义流程

### 创建自定义流程

```java
@Service
public class CustomFlowService {
    
    @Autowired
    private AgentFlowService agentFlowService;
    
    @Autowired
    private ChatLanguageModel chatModel;
    
    public AgentFlow createCustomFlow() {
        LLMNodeExecutor llmExecutor = new LLMNodeExecutor(chatModel);
        
        return agentFlowService.createFlow("CustomFlow", FlowMode.PLANNING)
            .addNode(FlowNode.builder()
                .nodeId("step1")
                .name("Step1")
                .type(NodeType.LLM)
                .config("systemPrompt", "系统提示词...")
                .config("userPromptTemplate", "用户提示词：{{input.data}}")
                .executor(ctx -> llmExecutor.execute(
                    FlowNode.builder()
                        .nodeId("step1")
                        .name("Step1")
                        .type(NodeType.LLM)
                        .config("systemPrompt", "系统提示词...")
                        .config("userPromptTemplate", "用户提示词：{{input.data}}")
                        .build(), 
                    ctx))
                .build())
            .addEdge("start", "step1")
            .addEdge("step1", "end")
            .build();
    }
}
```

### 提示词模板语法

| 语法 | 说明 | 示例 |
|------|------|------|
| `{{input.key}}` | 从初始输入获取 | `{{input.topic}}` |
| `{{variable.key}}` | 从变量获取 | `{{variable.iteration}}` |
| `{{node.nodeId.output}}` | 从节点输出获取 | `{{node.analyze.output}}` |
| `{{lastOutput}}` | 上一个节点的输出 | `{{lastOutput}}` |

## 流程对比

| 特性 | 规划执行模式 | ReAct 模式 |
|------|-------------|-----------|
| 执行路径 | 预定义 | 动态决定 |
| 适用场景 | 确定性任务 | 探索性任务 |
| 可预测性 | 高 | 中 |
| 灵活性 | 中 | 高 |
| Token 消耗 | 较少 | 较多 |
| 执行时间 | 较短 | 较长 |

## 最佳实践

1. **选择合适的模式**
   - 确定性任务 → 规划执行模式
   - 探索性任务 → ReAct 模式

2. **节点粒度设计**
   - 每个节点只做一件事
   - 保持节点职责单一

3. **错误处理**
   - 使用条件边处理失败情况
   - 设置超时机制

4. **性能优化**
   - 并行节点提高执行效率
   - 缓存常用工具结果

5. **调试技巧**
   - 使用执行历史追踪问题
   - 记录详细的执行日志
