package com.example.project.agent.flow.strategy;

import com.example.project.agent.flow.AgentFlow;
import com.example.project.agent.flow.FlowContext;
import com.example.project.agent.flow.FlowEdge;
import com.example.project.agent.flow.FlowNode;
import com.example.project.agent.flow.dto.FlowNodeExecutionResult;
import com.example.project.agent.flow.dto.FlowResult;
import com.example.project.agent.flow.enums.FlowStatus;
import com.example.project.agent.flow.enums.NodeType;
import com.example.project.agent.flow.executor.ToolNodeExecutor;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 规划执行策略（节点驱动版）
 * <p>
 * 采用"计划-执行-综合"三阶段模式，<b>按节点拓扑顺序执行</b>：
 * <ol>
 *   <li><b>Plan（计划）</b>：第一个 LLM 节点制定执行计划</li>
 *   <li><b>Execute（执行）</b>：按顺序执行后续 LLM 节点，每个节点内部支持工具调用</li>
 *   <li><b>Synthesize（综合）</b>：最后一个 LLM 节点汇总结果生成最终输出</li>
 * </ol>
 * <p>
 * 与 REACT 模式的区别：PLANNING 模式强调<b>分阶段执行</b>，
 * 每个 LLM 节点有明确的职责划分（计划/执行/综合），
 * 而 REACT 模式更灵活，每个节点都可以自主决定工具调用。
 * </p>
 */
public class PlanningExecutionStrategy implements ExecutionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(PlanningExecutionStrategy.class);
    private static final int MAX_STEP_ITERATIONS = 5;

    private final ChatLanguageModel chatModel;
    private final ToolNodeExecutor toolExecutor;

    public PlanningExecutionStrategy(ChatLanguageModel chatModel, ToolNodeExecutor toolExecutor) {
        if (chatModel == null || toolExecutor == null) {
            throw new IllegalArgumentException("PlanningExecutionStrategy requires both chatModel and toolExecutor");
        }
        this.chatModel = chatModel;
        this.toolExecutor = toolExecutor;
    }

    @Override
    public FlowResult execute(AgentFlow flow, FlowContext context) {
        logger.debug("Using node-driven Plan-Execute-Synthesize strategy");

        List<ToolSpecification> toolSpecs = buildToolSpecifications();
        List<ChatMessage> messages = new ArrayList<>();

        // 从起始节点开始遍历
        FlowNode currentNode = findStartNode(flow);
        if (currentNode == null) {
            throw new IllegalStateException("No start node found in flow");
        }

        while (currentNode != null && currentNode.getType() != NodeType.END) {
            logger.debug("Executing node: id={}, type={}, name={}",
                    currentNode.getNodeId(), currentNode.getType(), currentNode.getName());

            FlowNodeExecutionResult result;

            switch (currentNode.getType()) {
                case START -> {
                    result = currentNode.execute(context);
                }
                case LLM -> {
                    result = executeLLMNode(currentNode, context, messages, toolSpecs);
                }
                default -> {
                    result = currentNode.execute(context);
                }
            }

            context.recordNodeResult(result);

            if (!result.isSuccess()) {
                context.complete(FlowStatus.FAILED);
                return FlowResult.failure(flow.getFlowId(), context.getExecutionId(),
                        "Node execution failed: " + currentNode.getNodeId() + " - " + result.getError(), context);
            }

            currentNode = findNextNode(flow, currentNode, context);
        }

        // 执行 END 节点
        if (currentNode != null) {
            FlowNodeExecutionResult endResult = currentNode.execute(context);
            context.recordNodeResult(endResult);
        }

        Object finalOutput = context.getLastOutput();
        context.complete(FlowStatus.COMPLETED);
        return FlowResult.success(flow.getFlowId(), context.getExecutionId(), finalOutput, context);
    }

    /**
     * 执行 LLM 节点，支持内部工具调用循环
     */
    private FlowNodeExecutionResult executeLLMNode(FlowNode node, FlowContext context,
                                                    List<ChatMessage> messages,
                                                    List<ToolSpecification> toolSpecs) {
        String systemPrompt = node.getConfig("systemPrompt",
                "You are a helpful AI assistant executing a plan step by step.");
        String userPromptTemplate = node.getConfig("userPromptTemplate", "{{input.message}}");

        // 如果是第一个 LLM 节点，初始化消息列表
        if (messages.isEmpty()) {
            messages.add(SystemMessage.from(systemPrompt));
        }

        // 构建用户消息（替换模板变量）
        String userPrompt = renderTemplate(userPromptTemplate, context);
        messages.add(UserMessage.from(userPrompt));

        // LLM 调用循环（支持工具调用）
        int iteration = 0;
        while (iteration < MAX_STEP_ITERATIONS) {
            iteration++;

            Response<AiMessage> response = chatModel.generate(messages, toolSpecs);
            AiMessage aiMessage = response.content();
            messages.add(aiMessage);

            // 如果没有工具请求，直接返回结果
            if (aiMessage.toolExecutionRequests() == null || aiMessage.toolExecutionRequests().isEmpty()) {
                return FlowNodeExecutionResult.builder()
                        .nodeId(node.getNodeId())
                        .nodeName(node.getName())
                        .success(true)
                        .output(aiMessage.text())
                        .build();
            }

            // 执行工具调用
            for (ToolExecutionRequest request : aiMessage.toolExecutionRequests()) {
                logger.debug("Node {} tool request: name={}, args={}",
                        node.getNodeId(), request.name(), request.arguments());

                ToolExecutionResultMessage toolResult = executeTool(request, context);
                messages.add(toolResult);
            }
        }

        // 达到最大迭代次数，返回最后一条消息
        ChatMessage lastMessage = messages.get(messages.size() - 1);
        String finalText = lastMessage instanceof AiMessage ? ((AiMessage) lastMessage).text() : lastMessage.toString();
        return FlowNodeExecutionResult.builder()
                .nodeId(node.getNodeId())
                .nodeName(node.getName())
                .success(true)
                .output(finalText)
                .build();
    }

    /**
     * 执行工具调用
     */
    private ToolExecutionResultMessage executeTool(ToolExecutionRequest request, FlowContext context) {
        String toolName = request.name();
        var tool = toolExecutor.getToolRegistry().getTool(toolName);

        if (tool == null) {
            logger.warn("Tool not found in registry: {}", toolName);
            return ToolExecutionResultMessage.from(request,
                    "Error: Tool '" + toolName + "' not found");
        }

        try {
            Object result = tool.apply(context);
            String output = result != null ? result.toString() : "";
            return ToolExecutionResultMessage.from(request, output);
        } catch (Exception e) {
            logger.error("Tool execution failed: tool={}", toolName, e);
            return ToolExecutionResultMessage.from(request, "Error: " + e.getMessage());
        }
    }

    /**
     * 查找起始节点
     */
    private FlowNode findStartNode(AgentFlow flow) {
        List<FlowNode> startNodes = flow.getStartNodes();
        return startNodes.isEmpty() ? null : startNodes.get(0);
    }

    /**
     * 查找下一个节点
     */
    private FlowNode findNextNode(AgentFlow flow, FlowNode currentNode, FlowContext context) {
        List<FlowEdge> outgoingEdges = flow.getOutgoingEdges(currentNode.getNodeId());

        if (outgoingEdges.isEmpty()) {
            return null;
        }

        if (outgoingEdges.size() == 1) {
            return flow.getNode(outgoingEdges.get(0).getToNodeId());
        }

        for (FlowEdge edge : outgoingEdges) {
            if (edge.getCondition() == null || edge.getCondition().apply(context)) {
                return flow.getNode(edge.getToNodeId());
            }
        }

        return null;
    }

    /**
     * 渲染模板，替换变量占位符
     */
    private String renderTemplate(String template, FlowContext context) {
        if (template == null) {
            return "";
        }

        String result = template;

        // 替换 {{input.xxx}}
        result = result.replace("{{input.message}}",
                context.getInput("message") != null ? context.getInput("message").toString() : "");
        result = result.replace("{{input.topic}}",
                context.getInput("topic") != null ? context.getInput("topic").toString() : "");

        // 替换 {{node.xxx.output}}
        for (String nodeId : context.getNodeResults().keySet()) {
            String placeholder = "{{node." + nodeId + ".output}}";
            Object output = context.getNodeOutput(nodeId);
            if (output != null) {
                result = result.replace(placeholder, output.toString());
            }
        }

        // 替换 {{lastOutput}}
        Object lastOutput = context.getLastOutput();
        if (lastOutput != null) {
            result = result.replace("{{lastOutput}}", lastOutput.toString());
        }

        return result;
    }

    /**
     * 从 ToolNodeExecutor 注册表构建工具规范列表
     */
    private List<ToolSpecification> buildToolSpecifications() {
        List<ToolSpecification> specs = new ArrayList<>();

        for (String toolName : toolExecutor.getToolRegistry().getToolNames()) {
            String description = toolExecutor.getToolDescription(toolName);
            specs.add(ToolSpecification.builder()
                    .name(toolName)
                    .description(description)
                    .build());
        }

        return specs;
    }
}
