package com.example.project.agent.flow.executor;

import com.example.project.agent.flow.FlowContext;
import com.example.project.agent.flow.FlowNode;
import com.example.project.agent.flow.dto.FlowNodeExecutionResult;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LLM 节点执行器
 * <p>
 * 负责执行 AI 模型节点，包括：
 * <ul>
 *   <li>解析提示词模板，从上下文中填充变量</li>
 *   <li>调用大语言模型生成回复</li>
 *   <li><b>支持内嵌工具调用</b>：LLM 可自主决定是否需要工具</li>
 *   <li>记录执行耗时和元数据</li>
 * </ul>
 * 支持模板变量：{@code {{input.xxx}}}、{@code {{variable.xxx}}}、{@code {{node.xxx}}}、{@code {{lastOutput}}}
 * </p>
 *
 * @author system
 * @since 1.0
 */
public class LLMNodeExecutor {

    private static final Logger logger = LoggerFactory.getLogger(LLMNodeExecutor.class);
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{([\\w.]+)\\}\\}");
    private static final int MAX_TOOL_ITERATIONS = 5;

    private final ChatLanguageModel chatModel;

    public LLMNodeExecutor(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 执行 LLM 节点（基础版，不调用工具）
     * <p>
     * 适用于不需要工具调用的场景。
     * </p>
     *
     * @param node    流程节点
     * @param context 流程上下文
     * @return 节点执行结果
     */
    public FlowNodeExecutionResult execute(FlowNode node, FlowContext context) {
        return execute(node, context, null);
    }

    /**
     * 执行 LLM 节点（带工具调用）
     * <p>
     * 适用于需要 LLM 自主调用工具的场景。
     * LLM 会根据上下文自主决定是否需要调用工具，并在当前节点内完成多轮交互。
     * </p>
     *
     * @param node         流程节点
     * @param context      流程上下文
     * @param toolExecutor 工具执行器（可选）
     * @return 节点执行结果
     */
    public FlowNodeExecutionResult execute(FlowNode node, FlowContext context, ToolNodeExecutor toolExecutor) {
        try {
            long startTime = System.currentTimeMillis();

            String systemPrompt = node.getConfig("systemPrompt",
                    "你是一个专业的 AI 助手，请根据用户输入提供有帮助的回答。");
            String userPromptTemplate = node.getConfig("userPromptTemplate", "{{input}}");
            String userPrompt = resolveTemplate(userPromptTemplate, context);

            logger.debug("LLM Node executing: node={}, prompt={}", node.getName(),
                    userPrompt.substring(0, Math.min(100, userPrompt.length())));

            String response;
            boolean hasToolCall = false;

            // 如果提供了 toolExecutor 且有工具，则使用带工具调用的版本
            if (toolExecutor != null && !toolExecutor.getToolRegistry().getToolNames().isEmpty()) {
                response = executeWithTools(systemPrompt, userPrompt, toolExecutor);
                hasToolCall = true;
            } else {
                response = callModel(systemPrompt, userPrompt);
            }

            long endTime = System.currentTimeMillis();

            logger.debug("LLM Node completed: node={}, time={}ms", node.getName(), endTime - startTime);

            return FlowNodeExecutionResult.builder()
                    .nodeId(node.getNodeId())
                    .nodeName(node.getName())
                    .success(true)
                    .output(response)
                    .executionTimeMs(endTime - startTime)
                    .metadata("systemPrompt", systemPrompt)
                    .metadata("userPrompt", userPrompt)
                    .metadata("hasToolCall", hasToolCall)
                    .build();

        } catch (Exception e) {
            logger.error("LLM Node execution failed: node={}, error={}", node.getName(), e.getMessage(), e);
            return FlowNodeExecutionResult.builder()
                    .nodeId(node.getNodeId())
                    .nodeName(node.getName())
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * 调用模型（带工具支持）
     */
    private String executeWithTools(String systemPrompt, String userPrompt, ToolNodeExecutor toolExecutor) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(systemPrompt));
        messages.add(UserMessage.from(userPrompt));

        List<ToolSpecification> toolSpecs = buildToolSpecifications(toolExecutor);

        int iteration = 0;
        while (iteration < MAX_TOOL_ITERATIONS) {
            iteration++;

            Response<AiMessage> response = chatModel.generate(messages, toolSpecs);
            AiMessage aiMessage = response.content();
            messages.add(aiMessage);

            // 如果没有工具请求，直接返回结果
            if (aiMessage.toolExecutionRequests() == null || aiMessage.toolExecutionRequests().isEmpty()) {
                return aiMessage.text();
            }

            // 执行工具调用
            for (ToolExecutionRequest request : aiMessage.toolExecutionRequests()) {
                logger.debug("Tool execution request: name={}, args={}", request.name(), request.arguments());
                ToolExecutionResultMessage toolResult = executeTool(request, toolExecutor);
                messages.add(toolResult);
            }
        }

        // 达到最大迭代次数，返回最后一条消息
        ChatMessage lastMessage = messages.get(messages.size() - 1);
        return lastMessage instanceof AiMessage ? ((AiMessage) lastMessage).text() : lastMessage.toString();
    }

    /**
     * 调用语言模型生成回复（基础版，无工具）
     */
    private String callModel(String systemPrompt, String userPrompt) {
        SystemMessage systemMessage = SystemMessage.from(systemPrompt);
        UserMessage userMessage = UserMessage.from(userPrompt);

        Response<AiMessage> response = chatModel.generate(Arrays.asList(systemMessage, userMessage));
        return response.content().text();
    }

    /**
     * 执行单个工具调用
     */
    private ToolExecutionResultMessage executeTool(ToolExecutionRequest request, ToolNodeExecutor toolExecutor) {
        String toolName = request.name();
        var tool = toolExecutor.getToolRegistry().getTool(toolName);

        if (tool == null) {
            logger.warn("Tool not found in registry: {}", toolName);
            return ToolExecutionResultMessage.from(request,
                    "Error: Tool '" + toolName + "' not found");
        }

        try {
            Object result = tool.apply(null);
            String output = result != null ? result.toString() : "";
            return ToolExecutionResultMessage.from(request, output);
        } catch (Exception e) {
            logger.error("Tool execution failed: tool={}", toolName, e);
            return ToolExecutionResultMessage.from(request, "Error: " + e.getMessage());
        }
    }

    /**
     * 构建工具规范列表
     */
    private List<ToolSpecification> buildToolSpecifications(ToolNodeExecutor toolExecutor) {
        List<ToolSpecification> specs = new ArrayList<>();

        if (toolExecutor == null) {
            return specs;
        }

        for (String toolName : toolExecutor.getToolRegistry().getToolNames()) {
            String description = toolExecutor.getToolDescription(toolName);
            specs.add(ToolSpecification.builder()
                    .name(toolName)
                    .description(description)
                    .build());
        }

        return specs;
    }

    /**
     * 解析模板字符串，将占位符替换为上下文中的实际值
     */
    private String resolveTemplate(String template, FlowContext context) {
        if (template == null || template.isEmpty()) {
            return "";
        }

        Matcher matcher = TEMPLATE_PATTERN.matcher(template);

        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            Object value = resolveVariable(placeholder, context);
            String replacement = value != null ? Matcher.quoteReplacement(value.toString()) : "";
            if (value == null) {
                logger.warn("Template variable not found: {{}}", placeholder);
            }
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 根据变量路径从上下文中解析变量值
     */
    private Object resolveVariable(String path, FlowContext context) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        if ("lastOutput".equals(path)) {
            return context.getLastOutput();
        }

        String[] parts = path.split("\\.");
        if (parts.length < 2) {
            return null;
        }

        String namespace = parts[0];

        return switch (namespace) {
            case "input" -> context.getInput(parts[1]);
            case "variable" -> context.getVariable(parts[1]);
            case "node" -> parts.length >= 3 ? context.getNodeOutput(parts[1]) : null;
            default -> null;
        };
    }
}
