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
 * 基于节点驱动的 ReAct 执行策略
 * <p>
 * ReAct = Reasoning (推理) + Acting (行动)
 * <p>
 * 该策略<b>按节点拓扑顺序执行</b>，每个 LLM 节点内部支持自主工具调用：
 * <ol>
 *   <li>按流程图的边流转到下一个节点</li>
 *   <li>LLM 节点：调用模型，模型可自主决定是否需要工具</li>
 *   <li>如果 LLM 请求工具，在当前节点内完成工具调用并再次调用 LLM</li>
 *   <li>继续流转到下一个节点，直到 END</li>
 * </ol>
 * <p>
 * <b>工具简化设计</b>：不需要在流程中定义 TOOL 节点，
 * 所有工具统一注册到 ToolNodeExecutor，由 LLM 自主选择和调用。
 * </p>
 *
 * @author system
 * @since 1.0
 */
public class LLMReActStrategy implements ExecutionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(LLMReActStrategy.class);
    private static final int MAX_ITERATIONS = 10;

    private final ChatLanguageModel chatModel;
    private final ToolNodeExecutor toolExecutor;

    public LLMReActStrategy(ChatLanguageModel chatModel, ToolNodeExecutor toolExecutor) {
        this.chatModel = chatModel;
        this.toolExecutor = toolExecutor;
    }

    @Override
    public FlowResult execute(AgentFlow flow, FlowContext context) {
        logger.debug("Using node-driven ReAct execution strategy");

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
                case LOOP -> {
                    // LOOP 节点：判断是否继续循环
                    result = handleLoopNode(currentNode, context);
                    if (result != null && Boolean.TRUE.equals(result.getOutput())) {
                        // 需要循环，跳转到目标节点
                        String loopTarget = currentNode.getConfig("loopTarget");
                        if (loopTarget != null) {
                            currentNode = flow.getNode(loopTarget);
                            continue;
                        }
                    }
                    // 不需要循环，继续向下执行
                    result = FlowNodeExecutionResult.builder()
                            .nodeId(currentNode.getNodeId())
                            .nodeName(currentNode.getName())
                            .success(true)
                            .output("loop_exit")
                            .build();
                }
                default -> {
                    // 其他类型节点（如 CONDITION）直接执行
                    result = currentNode.execute(context);
                }
            }

            context.recordNodeResult(result);

            if (!result.isSuccess()) {
                context.complete(FlowStatus.FAILED);
                return FlowResult.failure(flow.getFlowId(), context.getExecutionId(),
                        "Node execution failed: " + currentNode.getNodeId() + " - " + result.getError(), context);
            }

            // 流转到下一个节点
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
     * 处理 LOOP 节点，判断是否继续循环
     */
    private FlowNodeExecutionResult handleLoopNode(FlowNode node, FlowContext context) {
        String loopCondition = node.getConfig("loopCondition", "检查是否需要继续优化");
        String loopTarget = node.getConfig("loopTarget");
        int maxIterations = node.getConfig("maxIterations", 3);

        // 获取当前循环次数
        Integer iteration = context.getVariable("loop_" + node.getNodeId() + "_count");
        if (iteration == null) {
            iteration = 0;
        }
        iteration++;
        context.setVariable("loop_" + node.getNodeId() + "_count", iteration);

        // 超过最大迭代次数，退出循环
        if (iteration >= maxIterations) {
            logger.debug("Loop {} reached max iterations: {}", node.getNodeId(), maxIterations);
            return FlowNodeExecutionResult.builder()
                    .nodeId(node.getNodeId())
                    .nodeName(node.getName())
                    .success(true)
                    .output(false)
                    .build();
        }

        // 使用 LLM 判断是否需要继续循环
        String lastOutput = context.getLastOutput() != null ? context.getLastOutput().toString() : "";
        String prompt = loopCondition + "\n\n当前结果：\n" + lastOutput + "\n\n是否需要继续优化？请回答：是 或 否";

        Response<AiMessage> response = chatModel.generate(UserMessage.from(prompt));
        String answer = response.content().text().trim().toLowerCase();

        boolean shouldContinue = answer.contains("是") || answer.contains("yes") || answer.contains("true");

        logger.debug("Loop {} iteration {}/{}, shouldContinue={}",
                node.getNodeId(), iteration, maxIterations, shouldContinue);

        return FlowNodeExecutionResult.builder()
                .nodeId(node.getNodeId())
                .nodeName(node.getName())
                .success(true)
                .output(shouldContinue)
                .build();
    }

    /**
     * 执行 LLM 节点，支持内部工具调用循环
     */
    private FlowNodeExecutionResult executeLLMNode(FlowNode node, FlowContext context,
                                                    List<ChatMessage> messages,
                                                    List<ToolSpecification> toolSpecs) {
        String systemPrompt = node.getConfig("systemPrompt", "You are a helpful AI assistant.");
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
        while (iteration < MAX_ITERATIONS) {
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

        // 简单场景：只有一条出边，直接返回目标节点
        if (outgoingEdges.size() == 1) {
            return flow.getNode(outgoingEdges.get(0).getToNodeId());
        }

        // 多条出边：找条件匹配的第一条
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

        // 替换 {{node.xxx.output}} - 获取其他节点的输出
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
     * 构建工具规范列表
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
