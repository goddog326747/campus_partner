package com.example.project.agent.flow.factory;

import com.example.project.agent.flow.AgentFlow;
import com.example.project.agent.flow.FlowNode;
import com.example.project.agent.flow.enums.FlowMode;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentFlowFactory {

    private final Map<String, AgentFlow> flowRegistry = new ConcurrentHashMap<>();

    public AgentFlowFactory() {
        registerDefaultFlows();
    }

    public AgentFlow getFlow(String flowName) {
        AgentFlow flow = flowRegistry.get(flowName.toLowerCase());
        if (flow == null) {
            throw new IllegalArgumentException("Unknown flow: " + flowName);
        }
        return flow;
    }

    public void registerFlow(String name, AgentFlow flow) {
        flowRegistry.put(name.toLowerCase(), flow);
    }

    private void registerDefaultFlows() {
        registerFlow("simple-chat", createSimpleChatFlow());
        registerFlow("react-chat", createReActChatFlow());
        registerFlow("post-generation", createPostGenerationFlow());
        registerFlow("post-agent", createPostAgentFlow());
        registerFlow("quality-check", createQualityCheckFlow());
    }

    public AgentFlow createSimpleChatFlow() {
        return AgentFlow.builder()
                .name("SimpleChatFlow")
                .mode(FlowMode.PLANNING)
                .node("start", FlowNode.startNode("start"))
                .node("chat", FlowNode.llmBuilder("chat", "Chat",
                                "你是一个友好的 AI 助手，请帮助用户解决问题。",
                                "{{input.message}}")
                        .build())
                .node("end", FlowNode.endNode("end"))
                .edge("start", "chat")
                .edge("chat", "end")
                .build();
    }

    public AgentFlow createReActChatFlow() {
        return AgentFlow.builder()
                .name("ReActChatFlow")
                .mode(FlowMode.REACT)
                .node("start", FlowNode.startNode("start"))
                .node("analyze", FlowNode.llmBuilder("analyze", "Analyze",
                                "你是一个智能分析助手。分析用户问题，判断是否需要搜索相关信息。" +
                                "如果需要，请使用 searchPosts 或 getHotTopics 工具获取信息。",
                                "用户问题：{{input.message}}")
                        .config("useTools", true)
                        .build())
                .node("answer", FlowNode.llmBuilder("answer", "Answer",
                                "根据分析结果回答用户问题。",
                                "用户问题：{{input.message}}\n分析结果：{{node.analyze.output}}\n请给出完整回答：")
                        .build())
                .node("end", FlowNode.endNode("end"))
                .edge("start", "analyze")
                .edge("analyze", "answer")
                .edge("answer", "end")
                .build();
    }

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
                        .config("useTools", true)
                        .build())
                .node("generate", FlowNode.llmBuilder("generate", "Generate",
                                "你是一个专业的帖子生成助手。根据收集的信息生成吸引人的帖子。\n" +
                                "核心原则：只基于已知信息创作，绝不编造事实、数据或事件。\n" +
                                "要求：\n" +
                                "- 正文500字左右，内容充实，言之有物\n" +
                                "- 内容真实可信，不虚构具体人名、地点、数据\n" +
                                "- 风格自然，像真实用户发帖\n" +
                                "输出格式（严格遵守）：\n" +
                                "【标题】帖子标题\n" +
                                "【正文】帖子正文\n" +
                                "【标签】标签1, 标签2, 标签3",
                                "主题：{{input.topic}}\n风格：{{input.style}}\n要求：{{input.requirements}}\n" +
                                "收集的信息：{{node.research.output}}\n\n请生成一篇帖子：")
                        .executorType("contentLLMNodeExecutor")
                        .build())
                .node("end", FlowNode.endNode("end"))
                .edge("start", "research")
                .edge("research", "generate")
                .edge("generate", "end")
                .build();
    }

    public AgentFlow createPostAgentFlow() {
        return AgentFlow.builder()
                .name("PostAgentFlow")
                .mode(FlowMode.REACT)
                .node("start", FlowNode.startNode("start"))
                .node("research", FlowNode.llmBuilder("research", "Research",
                                "你是一个信息收集助手。请使用工具获取以下信息：\n" +
                                "1. 当前热门话题（getHotTopics）\n" +
                                "2. 相关帖子参考（searchPosts）\n" +
                                "3. 用户写作风格（getUserStyle）",
                                "主题：{{input.topic}}\n类别：{{input.category}}")
                        .config("useTools", true)
                        .build())
                .node("generate", FlowNode.llmBuilder("generate", "Generate",
                                "你是一个专业的社交媒体内容创作助手。根据收集的信息生成一篇高质量的帖子。\n" +
                                "核心原则：只基于已知信息创作，绝不编造事实、数据或事件。\n" +
                                "要求：\n" +
                                "- 正文500字左右，内容充实，言之有物\n" +
                                "- 内容真实可信，不虚构具体人名、地点、数据\n" +
                                "- 风格自然，像真实用户发帖\n" +
                                "- 标题吸引眼球，标签精准相关\n" +
                                "输出格式（严格遵守）：\n" +
                                "【标题】帖子标题\n" +
                                "【正文】帖子正文\n" +
                                "【标签】标签1, 标签2, 标签3",
                                "主题：{{input.topic}}\n风格：{{input.style}}\n要求：{{input.requirements}}\n" +
                                "收集的信息：{{node.research.output}}\n\n请根据以上信息生成帖子：")
                        .executorType("contentLLMNodeExecutor")
                        .build())
                .node("check", FlowNode.loopBuilder("check", "QualityCheck",
                                "generate",
                                "检查以下帖子内容的质量：标题是否吸引人、正文是否生动有趣、标签是否合理、整体是否有吸引力。")
                        .config("maxIterations", 3)
                        .build())
                .node("end", FlowNode.endNode("end"))
                .edge("start", "research")
                .edge("research", "generate")
                .edge("generate", "check")
                .edge("check", "end")
                .build();
    }

    public AgentFlow createQualityCheckFlow() {
        return AgentFlow.builder()
                .name("QualityCheckFlow")
                .mode(FlowMode.REACT)
                .node("start", FlowNode.startNode("start"))
                .node("generate", FlowNode.llmBuilder("generate", "Generate",
                                "你是一个内容生成助手。根据要求生成内容。",
                                "要求：{{input.requirements}}\n参考：{{input.reference}}")
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
