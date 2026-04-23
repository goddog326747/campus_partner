package com.example.project.service.impl;

import com.example.project.agent.*;
import com.example.project.agent.flow.*;
import com.example.project.agent.flow.executor.*;
import com.example.project.service.AgentFlowService;
import com.example.project.service.PostService;
import com.example.project.mapper.UserMapper;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent Flow 服务实现
 * 
 * ============================================================
 *                    服务实现说明
 * ============================================================
 * 
 * 这个服务实现了两种核心流程模式：
 * 
 * 【规划执行模式 - createPostGenerationFlow】
 * 
 * 适用场景：帖子生成等确定性任务
 * 
 * 流程设计：
 * ┌─────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────┐
 * │  Start  │───→│ AnalyzeReq  │───→│ QueryStyle  │───→│ GetHotTopics│───→│  Generate   │───→│   End   │
 * └─────────┘    └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘    └─────────┘
 *                                                                           │
 *                                                                           ↓
 *                                                                    ┌─────────────┐
 *                                                                    │CheckSensitive│
 *                                                                    └─────────────┘
 * 
 * 【ReAct 模式 - createReActQaFlow】
 * 
 * 适用场景：智能问答等探索性任务
 * 
 * 执行循环：
 * ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
 * │ Thought  │────→│  Action  │────→│Observation│────→│  Judge   │
 * └──────────┘     └──────────┘     └──────────┘     └────┬─────┘
 *      ↑                                                  │
 *      └────────────────── 需要继续 ───────────────────────┘
 *                           找到答案 → Answer
 * 
 * ============================================================
 */
@Service
public class AgentFlowServiceImpl implements AgentFlowService {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentFlowServiceImpl.class);
    
    private final ChatLanguageModel chatModel;
    private final PostTools postTools;
    private final PostService postService;
    private final UserMapper userMapper;
    
    // 执行历史缓存
    private final Map<String, FlowContext> executionHistory = new ConcurrentHashMap<>();
    
    @Autowired
    public AgentFlowServiceImpl(
            ChatLanguageModel chatModel,
            PostTools postTools,
            PostService postService,
            UserMapper userMapper) {
        this.chatModel = chatModel;
        this.postTools = postTools;
        this.postService = postService;
        this.userMapper = userMapper;
    }
    
    @Override
    public AgentFlow.Builder createFlow(String name) {
        return AgentFlow.builder().name(name).mode(FlowMode.PLANNING);
    }
    
    @Override
    public AgentFlow.Builder createFlow(String name, FlowMode mode) {
        return AgentFlow.builder().name(name).mode(mode);
    }
    
    @Override
    public FlowResult executeFlow(AgentFlow flow, Map<String, Object> input) {
        logger.info("Executing flow: flowId={}, name={}, mode={}", 
                flow.getFlowId(), flow.getName(), flow.getMode());
        
        FlowResult result = flow.execute(input);
        
        // 保存执行历史
        executionHistory.put(result.getExecutionId(), result.getContext());
        
        return result;
    }
    
    @Override
    public CompletableFuture<FlowResult> executeFlowAsync(AgentFlow flow, Map<String, Object> input) {
        return CompletableFuture.supplyAsync(() -> executeFlow(flow, input));
    }
    
    @Override
    public FlowContext getExecutionHistory(String executionId) {
        return executionHistory.get(executionId);
    }
    
    @Override
    public AgentFlow createPostGenerationFlow() {
        logger.debug("Creating post generation flow with PLANNING mode");
        
        // 创建工具执行器
        ToolNodeExecutor toolExecutor = new ToolNodeExecutor();
        
        // 注册工具
        toolExecutor.registerTool("getUserStyle", ctx -> {
            Long userId = ctx.getInput("userId");
            return postTools.getUserPostStyle(userId);
        });
        
        toolExecutor.registerTool("getHotTopics", ctx -> {
            return postTools.getHotTopics();
        });
        
        toolExecutor.registerTool("checkSensitive", ctx -> {
            String content = ctx.getVariable("generatedContent");
            return postTools.checkSensitiveContent(content);
        });
        
        // 创建 LLM 执行器
        LLMNodeExecutor llmExecutor = new LLMNodeExecutor(chatModel);
        
        return AgentFlow.builder()
                .name("PostGenerationFlow")
                .mode(FlowMode.PLANNING)
                
                // 1. 起始节点
                .addNode(FlowNode.builder()
                        .nodeId("start")
                        .name("Start")
                        .type(NodeType.START)
                        .executor(ctx -> FlowNodeExecutionResult.success("start", "Start", "Started"))
                        .build())
                
                // 2. 分析需求节点（LLM）
                .addNode(createLLMNode("analyze", "AnalyzeRequest", 
                        "你是一个需求分析专家。分析用户的帖子生成需求，提取关键信息。",
                        "用户想要生成一篇帖子：{{input.topic}}。请分析：1.帖子类型 2.目标受众 3.风格要求 4.关键要素",
                        llmExecutor))
                
                // 3. 查询用户风格节点（Tool）
                .addNode(createToolNode("queryStyle", "QueryUserStyle", "getUserStyle", toolExecutor))
                
                // 4. 获取热门话题节点（Tool）
                .addNode(createToolNode("getTopics", "GetHotTopics", "getHotTopics", toolExecutor))
                
                // 5. 生成内容节点（LLM）
                .addNode(FlowNode.builder()
                        .nodeId("generate")
                        .name("GenerateContent")
                        .type(NodeType.LLM)
                        .config("systemPrompt", "你是一个专业的帖子生成助手。根据用户需求、用户风格和热门话题，生成一篇吸引人的帖子。")
                        .config("userPromptTemplate", """
                            请生成一篇帖子：
                            主题：{{input.topic}}
                            用户风格：{{node.queryStyle.output}}
                            热门话题参考：{{node.getTopics.output}}
                            需求分析：{{node.analyze.output}}
                            
                            要求：
                            1. 标题吸引人，不超过20字
                            2. 内容生动有趣，符合用户风格
                            3. 可以适当结合热门话题
                            4. 结尾要有互动引导
                            """)
                        .executor(ctx -> {
                            FlowNode node = FlowNode.builder()
                                    .nodeId("generate")
                                    .name("GenerateContent")
                                    .type(NodeType.LLM)
                                    .config("systemPrompt", "你是一个专业的帖子生成助手。根据用户需求、用户风格和热门话题，生成一篇吸引人的帖子。")
                                    .config("userPromptTemplate", """
                                        请生成一篇帖子：
                                        主题：{{input.topic}}
                                        用户风格：{{node.queryStyle.output}}
                                        热门话题参考：{{node.getTopics.output}}
                                        需求分析：{{node.analyze.output}}
                                        
                                        要求：
                                        1. 标题吸引人，不超过20字
                                        2. 内容生动有趣，符合用户风格
                                        3. 可以适当结合热门话题
                                        4. 结尾要有互动引导
                                        """)
                                    .build();
                            FlowNodeExecutionResult result = llmExecutor.execute(node, ctx);
                            if (result.isSuccess()) {
                                ctx.setVariable("generatedContent", result.getOutput());
                            }
                            return result;
                        })
                        .build())
                
                // 6. 检查敏感词节点（Tool）
                .addNode(createToolNode("checkSensitive", "CheckSensitive", "checkSensitive", toolExecutor))
                
                // 7. 结束节点
                .addNode(FlowNode.builder()
                        .nodeId("end")
                        .name("End")
                        .type(NodeType.END)
                        .executor(ctx -> FlowNodeExecutionResult.success("end", "End", 
                                ctx.getNodeOutput("generate")))
                        .build())
                
                // 定义流程边
                .addEdge("start", "analyze")
                .addEdge("analyze", "queryStyle")
                .addEdge("queryStyle", "getTopics")
                .addEdge("getTopics", "generate")
                .addEdge("generate", "checkSensitive")
                .addEdge("checkSensitive", "end")
                
                .build();
    }
    
    @Override
    public AgentFlow createReActQaFlow() {
        logger.debug("Creating ReAct QA flow");
        
        // 创建工具执行器
        ToolNodeExecutor toolExecutor = new ToolNodeExecutor();
        
        // 注册 ReAct 工具
        toolExecutor.registerTool("searchPosts", ctx -> {
            String keyword = ctx.getInput("question");
            return postTools.searchRelatedPosts(keyword);
        });
        
        toolExecutor.registerTool("getUserInfo", ctx -> {
            Long userId = ctx.getInput("userId");
            return postTools.getUserInfo(userId);
        });
        
        toolExecutor.registerTool("getHotTopics", ctx -> {
            return postTools.getHotTopics();
        });
        
        // 创建 LLM 执行器
        LLMNodeExecutor llmExecutor = new LLMNodeExecutor(chatModel);
        
        return AgentFlow.builder()
                .name("ReActQAFlow")
                .mode(FlowMode.REACT)
                
                // 起始节点
                .addNode(FlowNode.builder()
                        .nodeId("start")
                        .name("Start")
                        .type(NodeType.START)
                        .executor(ctx -> FlowNodeExecutionResult.success("start", "Start", "Started"))
                        .build())
                
                // Thought 节点 - 分析问题
                .addNode(createLLMNode("thought", "Thought", """
                            你是一个智能问答助手，使用 ReAct 模式解决问题。
                            
                            ReAct 模式说明：
                            - Thought: 分析当前情况，决定下一步行动
                            - Action: 执行行动（使用工具）
                            - Observation: 观察行动结果
                            - Answer: 当找到答案时，输出 "FINAL ANSWER: " + 答案
                            
                            可用工具：
                            - searchPosts: 搜索相关帖子
                            - getUserInfo: 获取用户信息
                            - getHotTopics: 获取热门话题
                            """,
                            """
                            用户问题：{{input.question}}
                            
                            当前迭代：{{variable.iteration}}
                            上一步思考：{{variable.lastThought}}
                            上一步行动：{{variable.lastAction}}
                            上一步观察：{{variable.lastObservation}}
                            
                            请分析：
                            1. 当前已知信息
                            2. 还需要什么信息
                            3. 下一步应该采取什么行动
                            
                            格式：
                            Thought: [你的分析]
                            Action: [searchPosts/getUserInfo/getHotTopics/none]
                            """,
                            llmExecutor))
                
                // Action 节点 - 执行工具
                .addNode(FlowNode.builder()
                        .nodeId("action")
                        .name("Action")
                        .type(NodeType.TOOL)
                        .config("toolName", "dynamic")
                        .executor(ctx -> {
                            // 根据 Thought 结果决定调用哪个工具
                            String lastThought = ctx.getVariable("lastThought");
                            String toolName = extractToolName(lastThought);
                            
                            if (toolName != null && toolExecutor.getToolRegistry().hasTool(toolName)) {
                                ctx.setVariable("currentTool", toolName);
                                return toolExecutor.execute(
                                        FlowNode.builder()
                                                .nodeId("action")
                                                .name("Action")
                                                .type(NodeType.TOOL)
                                                .config("toolName", toolName)
                                                .build(), 
                                        ctx);
                            }
                            
                            return FlowNodeExecutionResult.success("action", "Action", "No action needed");
                        })
                        .build())
                
                // Answer 节点 - 生成最终答案
                .addNode(createLLMNode("answer", "GenerateAnswer", 
                        "根据收集到的信息，生成完整、准确的回答。",
                        """
                            用户问题：{{input.question}}
                            
                            收集到的信息：
                            {{lastOutput}}
                            
                            请生成最终答案：
                            """,
                        llmExecutor))
                
                // 结束节点
                .addNode(FlowNode.builder()
                        .nodeId("end")
                        .name("End")
                        .type(NodeType.END)
                        .executor(ctx -> FlowNodeExecutionResult.success("end", "End", 
                                ctx.getNodeOutput("answer")))
                        .build())
                
                // 定义流程边
                .addEdge("start", "thought")
                .addEdge("thought", "action")
                .addEdge("action", "thought")  // 循环回 Thought
                .addEdge("thought", "answer", ctx -> {
                    // 当 Thought 包含 FINAL ANSWER 时，转到 Answer
                    String thought = ctx.getVariable("lastThought");
                    return thought != null && thought.contains("FINAL ANSWER");
                })
                .addEdge("answer", "end")
                
                .build();
    }
    
    /**
     * 从 Thought 中提取工具名称
     */
    private String extractToolName(String thought) {
        if (thought == null) return null;
        
        if (thought.contains("searchPosts")) return "searchPosts";
        if (thought.contains("getUserInfo")) return "getUserInfo";
        if (thought.contains("getHotTopics")) return "getHotTopics";
        
        return null;
    }
    
    /**
     * 创建 LLM 节点
     */
    private FlowNode createLLMNode(String nodeId, String name, String systemPrompt, 
                                    String userPromptTemplate, LLMNodeExecutor executor) {
        return FlowNode.builder()
                .nodeId(nodeId)
                .name(name)
                .type(NodeType.LLM)
                .config("systemPrompt", systemPrompt)
                .config("userPromptTemplate", userPromptTemplate)
                .executor(ctx -> executor.execute(
                        FlowNode.builder()
                                .nodeId(nodeId)
                                .name(name)
                                .type(NodeType.LLM)
                                .config("systemPrompt", systemPrompt)
                                .config("userPromptTemplate", userPromptTemplate)
                                .build(), 
                        ctx))
                .build();
    }
    
    /**
     * 创建工具节点
     */
    private FlowNode createToolNode(String nodeId, String name, String toolName, 
                                     ToolNodeExecutor executor) {
        return FlowNode.builder()
                .nodeId(nodeId)
                .name(name)
                .type(NodeType.TOOL)
                .config("toolName", toolName)
                .executor(ctx -> executor.execute(
                        FlowNode.builder()
                                .nodeId(nodeId)
                                .name(name)
                                .type(NodeType.TOOL)
                                .config("toolName", toolName)
                                .build(), 
                        ctx))
                .build();
    }
}
