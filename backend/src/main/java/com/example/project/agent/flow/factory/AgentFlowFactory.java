package com.example.project.agent.flow.factory;

import com.example.project.agent.flow.AgentFlow;
import com.example.project.agent.flow.FlowNode;
import com.example.project.agent.flow.enums.FlowMode;
import com.example.project.agent.flow.executor.LLMNodeExecutor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AgentFlow 工厂类
 * <p>
 * 负责创建和管理预定义的流程模板，包括：
 * <ul>
 *   <li>简单聊天流程（simple-chat）</li>
 *   <li>ReAct 聊天流程（react-chat）</li>
 *   <li>帖子生成流程（post-generation）</li>
 * </ul>
 * 流程注册在内存中的 ConcurrentHashMap 中，支持大小写不敏感查找。
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Component
public class AgentFlowFactory {

    /** LLM 节点执行器 */
    private final LLMNodeExecutor llmExecutor;
    /** 流程注册表，key 为流程名称（小写） */
    private final Map<String, AgentFlow> flowRegistry = new ConcurrentHashMap<>();

    /**
     * 构造流程工厂，自动注册默认流程
     *
     * @param llmExecutor LLM 节点执行器
     */
    public AgentFlowFactory(LLMNodeExecutor llmExecutor) {
        this.llmExecutor = llmExecutor;
        registerDefaultFlows();
    }

    /**
     * 根据名称获取流程
     *
     * @param flowName 流程名称
     * @return 流程定义
     * @throws IllegalArgumentException 流程不存在时抛出
     */
    public AgentFlow getFlow(String flowName) {
        AgentFlow flow = flowRegistry.get(flowName.toLowerCase());
        if (flow == null) {
            throw new IllegalArgumentException("Unknown flow: " + flowName);
        }
        return flow;
    }

    /**
     * 注册流程
     *
     * @param name 流程名称
     * @param flow 流程定义
     */
    public void registerFlow(String name, AgentFlow flow) {
        flowRegistry.put(name.toLowerCase(), flow);
    }

    /**
     * 注册默认流程模板
     */
    private void registerDefaultFlows() {
        registerFlow("simple-chat", createSimpleChatFlow());
        registerFlow("react-chat", createReActChatFlow());
        registerFlow("post-generation", createPostGenerationFlow());
        registerFlow("quality-check", createQualityCheckFlow());
    }

    /**
     * 创建简单聊天流程
     * <p>
     * 流程：start → chat (LLM) → end
     * </p>
     *
     * @return 简单聊天流程
     */
    public AgentFlow createSimpleChatFlow() {
        return AgentFlow.builder()
                .name("SimpleChatFlow")
                .mode(FlowMode.PLANNING)
                .node("start", FlowNode.startNode("start"))
                .node("chat", FlowNode.llmBuilder("chat", "Chat",
                                "你是一个友好的 AI 助手，请帮助用户解决问题。",
                                "{{input.message}}")
                        .executor(llmExecutor::execute)
                        .build())
                .node("end", FlowNode.endNode("end"))
                .edge("start", "chat")
                .edge("chat", "end")
                .build();
    }

    /**
     * 创建 ReAct 聊天流程
     * <p>
     * 流程：start → analyze (LLM) → answer (LLM) → end
     * <p>
     * <b>ReAct 节点驱动设计</b>：
     * <ul>
     *   <li>不需要定义 TOOL 节点，工具由 LLM 自主选择和调用</li>
     *   <li>每个 LLM 节点内部可以完成多轮工具调用</li>
     *   <li>节点之间按拓扑顺序流转，保持流程可控性</li>
     * </ul>
     * </p>
     *
     * @return ReAct 聊天流程
     */
    public AgentFlow createReActChatFlow() {
        return AgentFlow.builder()
                .name("ReActChatFlow")
                .mode(FlowMode.REACT)
                .node("start", FlowNode.startNode("start"))
                .node("analyze", FlowNode.llmBuilder("analyze", "Analyze",
                                "你是一个智能分析助手。分析用户问题，判断是否需要搜索相关信息。" +
                                "如果需要，请使用 searchPosts 或 getHotTopics 工具获取信息。",
                                "用户问题：{{input.message}}")
                        .executor(llmExecutor::execute)
                        .build())
                .node("answer", FlowNode.llmBuilder("answer", "Answer",
                                "根据分析结果回答用户问题。",
                                "用户问题：{{input.message}}\n分析结果：{{node.analyze.output}}\n请给出完整回答：")
                        .executor(llmExecutor::execute)
                        .build())
                .node("end", FlowNode.endNode("end"))
                .edge("start", "analyze")
                .edge("analyze", "answer")
                .edge("answer", "end")
                .build();
    }

    /**
     * 创建帖子生成流程
     * <p>
     * 流程：start → research (LLM) → generate (LLM) → end
     * <p>
     * 节点职责：
     * <ul>
     *   <li>research：使用工具查询用户风格、热门话题和相关帖子</li>
     *   <li>generate：根据查询结果生成帖子内容</li>
     * </ul>
     * </p>
     *
     * @return 帖子生成流程
     */
    public AgentFlow createPostGenerationFlow() {
        return AgentFlow.builder()
                .name("PostGenerationFlow")
                .mode(FlowMode.PLANNING)
                .node("start", FlowNode.startNode("start"))
                .node("research", FlowNode.llmBuilder("research", "Research",
                                "你是一个信息收集助手。请使用工具获取以下信息：\n" +
                                "1. 用户写作风格（getUserStyle）\n" +
                                "2. 当前热门话题（getHotTopics）\n" +
                                "3. 相关帖子（searchPosts）",
                                "主题：{{input.topic}}\n类别：{{input.category}}")
                        .executor(llmExecutor::execute)
                        .build())
                .node("generate", FlowNode.llmBuilder("generate", "Generate",
                                "你是一个专业的帖子生成助手。根据收集的信息生成吸引人的帖子。",
                                "主题：{{input.topic}}\n风格：{{input.style}}\n要求：{{input.requirements}}\n" +
                                "收集的信息：{{node.research.output}}\n\n请生成一篇帖子：")
                        .executor(llmExecutor::execute)
                        .build())
                .node("end", FlowNode.endNode("end"))
                .edge("start", "research")
                .edge("research", "generate")
                .edge("generate", "end")
                .build();
    }

    /**
     * 创建质量检查流程（带 LOOP）
     * <p>
     * 流程：start → generate (LLM) → check (LOOP) → end
     * <p>
     * LOOP 节点逻辑：
     * <ul>
     *   <li>评估生成内容质量</li>
     *   <li>质量不达标 → 循环回 generate 重新生成</li>
     *   <li>质量达标 → 继续到 end</li>
     * </ul>
     * </p>
     *
     * @return 质量检查流程
     */
    public AgentFlow createQualityCheckFlow() {
        return AgentFlow.builder()
                .name("QualityCheckFlow")
                .mode(FlowMode.REACT)
                .node("start", FlowNode.startNode("start"))
                .node("generate", FlowNode.llmBuilder("generate", "Generate",
                                "你是一个内容生成助手。根据要求生成内容。",
                                "要求：{{input.requirements}}\n参考：{{input.reference}}")
                        .executor(llmExecutor::execute)
                        .build())
                .node("check", FlowNode.loopBuilder("check", "QualityCheck",
                                "generate",
                                "检查以下内容是否满足要求：内容完整性、准确性、可读性。")
                        .config("maxIterations", 3)
                        .build())
                .node("end", FlowNode.endNode("end"))
                .edge("start", "generate")
                .edge("generate", "check")
                .edge("check", "end")
                .build();
    }

}
