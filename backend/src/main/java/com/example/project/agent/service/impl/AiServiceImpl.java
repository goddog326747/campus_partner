package com.example.project.agent.service.impl;

import com.example.project.agent.dto.*;
import com.example.project.agent.service.AiService;
import com.example.project.agent.PostTools;
import com.example.project.agent.flow.AgentFlow;
import com.example.project.agent.flow.FlowContext;
import com.example.project.agent.flow.FlowEdge;
import com.example.project.agent.flow.FlowNode;
import com.example.project.agent.flow.dto.FlowResult;
import com.example.project.agent.flow.enums.FlowMode;
import com.example.project.agent.flow.enums.NodeType;
import com.example.project.agent.flow.executor.LLMNodeExecutor;
import com.example.project.agent.flow.executor.ToolNodeExecutor;
import com.example.project.entity.Post;
import com.example.project.service.PostService;
import com.example.project.shiro.util.UserContext;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一 AI 服务实现
 * 
 * 基于 Agent Flow 框架实现所有 AI 能力
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {
    
    private final ChatLanguageModel chatModel;
    private final PostTools postTools;
    private final PostService postService;
    
    // 执行历史缓存
    private final Map<String, FlowContext> executionHistory = new ConcurrentHashMap<>();
    
    @Override
    public ChatResponse chat(ChatRequest request) {
        log.info("AI chat: mode={}, message={}", request.getMode(), 
                request.getMessage().substring(0, Math.min(50, request.getMessage().length())));
        
        Long userId = getCurrentUserId();
        
        // 根据模式选择不同的 Flow
        AgentFlow flow;
        if ("REACT".equalsIgnoreCase(request.getMode())) {
            flow = createReActChatFlow();
        } else {
            flow = createSimpleChatFlow();
        }
        
        // 准备输入
        Map<String, Object> input = new HashMap<>();
        input.put("message", request.getMessage());
        input.put("conversationId", request.getConversationId());
        input.put("userId", userId);
        
        // 执行
        FlowResult result = flow.execute(input);
        
        // 保存历史
        executionHistory.put(result.getExecutionId(), result.getContext());
        
        return ChatResponse.builder()
                .reply(result.getOutputAs())
                .executionId(result.getExecutionId())
                .executionTime(result.getExecutionTimeMs())
                .nodesExecuted(result.getExecutedNodeCount())
                .build();
    }
    
    @Override
    public CompletableFuture<ChatResponse> chatAsync(ChatRequest request) {
        return CompletableFuture.supplyAsync(() -> chat(request));
    }
    
    @Override
    public PostGenerateResponse generatePost(PostGenerateRequest request) {
        log.info("Generating post: topic={}, category={}", request.getTopic(), request.getCategory());
        
        Long userId = getCurrentUserId();
        
        // 创建帖子生成流程
        AgentFlow flow = createPostGenerationFlow();
        
        // 准备输入
        Map<String, Object> input = new HashMap<>();
        input.put("topic", request.getTopic());
        input.put("category", request.getCategory());
        input.put("style", request.getStyle());
        input.put("requirements", request.getRequirements());
        input.put("userId", userId);
        
        // 执行
        FlowResult result = flow.execute(input);
        
        // 保存历史
        executionHistory.put(result.getExecutionId(), result.getContext());
        
        // 解析生成的内容
        PostContent content = parseGeneratedContent(result.getOutputAs());
        
        return PostGenerateResponse.builder()
                .title(content.getTitle())
                .content(content.getContent())
                .category(request.getCategory())
                .executionId(result.getExecutionId())
                .executionTime(result.getExecutionTimeMs())
                .nodesExecuted(result.getExecutedNodeCount())
                .published(false)
                .build();
    }
    
    @Override
    public PostGenerateResponse generateAndPublishPost(PostGenerateRequest request) {
        // 先生成
        PostGenerateResponse response = generatePost(request);
        
        if (response.getTitle() == null || response.getContent() == null) {
            return response;
        }
        
        // 发布帖子
        try {
            Long userId = getCurrentUserId();
            
            Post post = new Post();
            post.setTitle(response.getTitle());
            post.setContent(response.getContent());
            post.setCategory(request.getCategory());
            post.setUserId(userId);
            
            boolean created = postService.createPost(post);
            
            if (created && post.getId() != null) {
                response.setPublished(true);
                response.setPostId(post.getId());
                log.info("Post published: postId={}", post.getId());
            }
        } catch (Exception e) {
            log.error("Failed to publish post", e);
        }
        
        return response;
    }
    
    @Override
    public FlowResult executeFlow(String flowName, Map<String, Object> input) {
        log.info("Executing custom flow: flowName={}", flowName);
        
        AgentFlow flow;
        switch (flowName.toLowerCase()) {
            case "post-generation":
                flow = createPostGenerationFlow();
                break;
            case "react-qa":
                flow = createReActChatFlow();
                break;
            default:
                throw new IllegalArgumentException("Unknown flow: " + flowName);
        }
        
        FlowResult result = flow.execute(input);
        executionHistory.put(result.getExecutionId(), result.getContext());
        
        return result;
    }
    
    @Override
    public FlowContext getExecutionHistory(String executionId) {
        return executionHistory.get(executionId);
    }
    
    // ==================== 私有方法 ====================
    
    private Long getCurrentUserId() {
        return UserContext.get() != null ? UserContext.get().getId() : null;
    }
    
    /**
     * 创建简单对话流程
     */
    private AgentFlow createSimpleChatFlow() {
        LLMNodeExecutor llmExecutor = new LLMNodeExecutor(chatModel);
        
        Map<String, FlowNode> nodes = new HashMap<>();
        List<FlowEdge> edges = new ArrayList<>();
        
        nodes.put("start", FlowNode.startNode("start"));
        
        nodes.put("chat", FlowNode.builder()
                .nodeId("chat")
                .name("Chat")
                .type(NodeType.LLM)
                .config("systemPrompt", "你是一个友好的 AI 助手，请帮助用户解决问题。")
                .config("userPromptTemplate", "{{input.message}}")
                .executor(ctx -> llmExecutor.execute(
                        FlowNode.builder()
                                .nodeId("chat")
                                .name("Chat")
                                .type(NodeType.LLM)
                                .config("systemPrompt", "你是一个友好的 AI 助手，请帮助用户解决问题。")
                                .config("userPromptTemplate", "{{input.message}}")
                                .build(), 
                        ctx))
                .build());
        
        nodes.put("end", FlowNode.endNode("end"));
        
        edges.add(FlowEdge.sequential("start", "chat"));
        edges.add(FlowEdge.sequential("chat", "end"));
        
        return AgentFlow.builder()
                .name("SimpleChatFlow")
                .mode(FlowMode.PLANNING)
                .nodes(nodes)
                .edges(edges)
                .build();
    }
    
    /**
     * 创建 ReAct 对话流程
     */
    private AgentFlow createReActChatFlow() {
        LLMNodeExecutor llmExecutor = new LLMNodeExecutor(chatModel);
        ToolNodeExecutor toolExecutor = new ToolNodeExecutor();
        
        // 注册工具
        toolExecutor.registerTool("searchPosts", ctx -> {
            String keyword = ctx.getInput("message");
            return postTools.searchRelatedPosts(keyword);
        });
        
        toolExecutor.registerTool("getHotTopics", ctx -> {
            return postTools.getHotTopics();
        });
        
        Map<String, FlowNode> nodes = new HashMap<>();
        List<FlowEdge> edges = new ArrayList<>();
        
        nodes.put("start", FlowNode.startNode("start"));
        
        nodes.put("thought", FlowNode.builder()
                .nodeId("thought")
                .name("Thought")
                .type(NodeType.LLM)
                .config("systemPrompt", "你是一个智能助手，使用 ReAct 模式。决定是否需要使用工具。")
                .config("userPromptTemplate", "用户问题：{{input.message}}。请分析是否需要搜索相关信息。")
                .executor(ctx -> llmExecutor.execute(
                        FlowNode.builder()
                                .nodeId("thought")
                                .name("Thought")
                                .type(NodeType.LLM)
                                .config("systemPrompt", "你是一个智能助手，使用 ReAct 模式。决定是否需要使用工具。")
                                .config("userPromptTemplate", "用户问题：{{input.message}}。请分析是否需要搜索相关信息。")
                                .build(), 
                        ctx))
                .build());
        
        nodes.put("answer", FlowNode.builder()
                .nodeId("answer")
                .name("Answer")
                .type(NodeType.LLM)
                .config("systemPrompt", "根据收集的信息回答用户问题。")
                .config("userPromptTemplate", "用户问题：{{input.message}}\n相关信息：{{lastOutput}}\n请回答：")
                .executor(ctx -> llmExecutor.execute(
                        FlowNode.builder()
                                .nodeId("answer")
                                .name("Answer")
                                .type(NodeType.LLM)
                                .config("systemPrompt", "根据收集的信息回答用户问题。")
                                .config("userPromptTemplate", "用户问题：{{input.message}}\n相关信息：{{lastOutput}}\n请回答：")
                                .build(), 
                        ctx))
                .build());
        
        nodes.put("end", FlowNode.endNode("end"));
        
        edges.add(FlowEdge.sequential("start", "thought"));
        edges.add(FlowEdge.sequential("thought", "answer"));
        edges.add(FlowEdge.sequential("answer", "end"));
        
        return AgentFlow.builder()
                .name("ReActChatFlow")
                .mode(FlowMode.REACT)
                .nodes(nodes)
                .edges(edges)
                .build();
    }
    
    /**
     * 创建帖子生成流程
     */
    private AgentFlow createPostGenerationFlow() {
        LLMNodeExecutor llmExecutor = new LLMNodeExecutor(chatModel);
        ToolNodeExecutor toolExecutor = new ToolNodeExecutor();
        
        // 注册工具
        toolExecutor.registerTool("getUserStyle", ctx -> {
            Long userId = ctx.getInput("userId");
            return postTools.getUserPostStyle(userId);
        });
        
        toolExecutor.registerTool("getHotTopics", ctx -> {
            return postTools.getHotTopics();
        });
        
        Map<String, FlowNode> nodes = new HashMap<>();
        List<FlowEdge> edges = new ArrayList<>();
        
        nodes.put("start", FlowNode.startNode("start"));
        
        nodes.put("analyze", FlowNode.builder()
                .nodeId("analyze")
                .name("AnalyzeRequest")
                .type(NodeType.LLM)
                .config("systemPrompt", "你是一个需求分析专家。分析用户的帖子生成需求。")
                .config("userPromptTemplate", "用户想要生成一篇帖子：{{input.topic}}。请分析需求。")
                .executor(ctx -> llmExecutor.execute(
                        FlowNode.builder()
                                .nodeId("analyze")
                                .name("AnalyzeRequest")
                                .type(NodeType.LLM)
                                .config("systemPrompt", "你是一个需求分析专家。分析用户的帖子生成需求。")
                                .config("userPromptTemplate", "用户想要生成一篇帖子：{{input.topic}}。请分析需求。")
                                .build(), 
                        ctx))
                .build());
        
        nodes.put("queryStyle", createToolNode("queryStyle", "QueryUserStyle", "getUserStyle", toolExecutor));
        
        nodes.put("getTopics", createToolNode("getTopics", "GetHotTopics", "getHotTopics", toolExecutor));
        
        nodes.put("generate", FlowNode.builder()
                .nodeId("generate")
                .name("GenerateContent")
                .type(NodeType.LLM)
                .config("systemPrompt", "你是一个专业的帖子生成助手。生成吸引人的帖子。")
                .config("userPromptTemplate", """
                    请生成一篇帖子：
                    主题：{{input.topic}}
                    风格：{{node.queryStyle.output}}
                    热门话题：{{node.getTopics.output}}
                    """)
                .executor(ctx -> {
                    FlowNode node = FlowNode.builder()
                            .nodeId("generate")
                            .name("GenerateContent")
                            .type(NodeType.LLM)
                            .config("systemPrompt", "你是一个专业的帖子生成助手。生成吸引人的帖子。")
                            .config("userPromptTemplate", """
                                请生成一篇帖子：
                                主题：{{input.topic}}
                                风格：{{node.queryStyle.output}}
                                热门话题：{{node.getTopics.output}}
                                """)
                            .build();
                    return llmExecutor.execute(node, ctx);
                })
                .build());
        
        nodes.put("end", FlowNode.endNode("end"));
        
        edges.add(FlowEdge.sequential("start", "analyze"));
        edges.add(FlowEdge.sequential("analyze", "queryStyle"));
        edges.add(FlowEdge.sequential("queryStyle", "getTopics"));
        edges.add(FlowEdge.sequential("getTopics", "generate"));
        edges.add(FlowEdge.sequential("generate", "end"));
        
        return AgentFlow.builder()
                .name("PostGenerationFlow")
                .mode(FlowMode.PLANNING)
                .nodes(nodes)
                .edges(edges)
                .build();
    }
    
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
    
    private PostContent parseGeneratedContent(String content) {
        PostContent result = new PostContent();
        
        if (content == null) {
            return result;
        }
        
        String[] lines = content.split("\n");
        if (lines.length > 0) {
            String firstLine = lines[0].trim();
            if (firstLine.startsWith("标题：") || firstLine.startsWith("标题:")) {
                result.setTitle(firstLine.substring(3).trim());
                result.setContent(content.substring(firstLine.length()).trim());
            } else if (firstLine.length() < 30) {
                result.setTitle(firstLine);
                result.setContent(content.substring(firstLine.length()).trim());
            } else {
                result.setTitle("生成的帖子");
                result.setContent(content);
            }
        } else {
            result.setTitle("生成的帖子");
            result.setContent(content);
        }
        
        return result;
    }
    
    @lombok.Data
    private static class PostContent {
        private String title;
        private String content;
    }
}
