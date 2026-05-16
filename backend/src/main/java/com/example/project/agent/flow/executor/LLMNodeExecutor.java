package com.example.project.agent.flow.executor;

import com.example.project.agent.flow.FlowContext;
import com.example.project.agent.flow.FlowNode;
import com.example.project.agent.flow.advisor.FlowAdvisor;
import com.example.project.agent.flow.dto.FlowNodeExecutionResult;
import com.example.project.agent.flow.enums.NodeType;
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
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LLMNodeExecutor implements NodeExecutor {

    private static final Logger logger = LoggerFactory.getLogger(LLMNodeExecutor.class);
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{([\\w.]+)\\}\\}");
    private static final int MAX_TOOL_ITERATIONS = 5;

    protected final ChatLanguageModel chatModel;
    protected final ToolNodeExecutor toolExecutor;
    protected final List<FlowAdvisor> advisors;

    public LLMNodeExecutor(ChatLanguageModel chatModel, ToolNodeExecutor toolExecutor,
                           List<FlowAdvisor> advisors) {
        this.chatModel = chatModel;
        this.toolExecutor = toolExecutor;
        this.advisors = advisors != null ? advisors.stream()
                .sorted(Comparator.comparingInt(FlowAdvisor::getOrder))
                .toList() : List.of();
    }

    @Override
    public String getSupportedNodeType() {
        return NodeType.LLM.name();
    }

    @Override
    public FlowNodeExecutionResult execute(FlowNode node, FlowContext context) {
        try {
            long startTime = System.currentTimeMillis();

            String systemPrompt = node.getConfig("systemPrompt",
                    "你是一个专业的 AI 助手，请根据用户输入提供有帮助的回答。");
            String userPromptTemplate = node.getConfig("userPromptTemplate", "{{input}}");
            String userPrompt = resolveTemplate(userPromptTemplate, context);

            boolean useTools = node.getConfig("useTools", false);

            logger.debug("LLM Node executing: node={}, useTools={}, prompt={}",
                    node.getName(), useTools,
                    userPrompt.substring(0, Math.min(100, userPrompt.length())));

            String response;
            boolean hasToolCall = false;

            if (useTools && toolExecutor != null && toolExecutor.hasTools()) {
                response = executeWithTools(systemPrompt, userPrompt, context);
                hasToolCall = true;
            } else {
                response = callModel(systemPrompt, userPrompt, context);
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

    private String executeWithTools(String systemPrompt, String userPrompt, FlowContext context) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(systemPrompt));
        messages.add(UserMessage.from(userPrompt));

        messages = applyAdvisorsBefore(messages, context);

        List<ToolSpecification> toolSpecs = toolExecutor.buildToolSpecifications();

        int iteration = 0;
        while (iteration < MAX_TOOL_ITERATIONS) {
            iteration++;

            Response<AiMessage> response = chatModel.generate(messages, toolSpecs);
            AiMessage aiMessage = response.content();
            messages.add(aiMessage);

            if (aiMessage.toolExecutionRequests() == null || aiMessage.toolExecutionRequests().isEmpty()) {
                applyAdvisorsAfter(messages, aiMessage, context);
                return aiMessage.text();
            }

            for (ToolExecutionRequest request : aiMessage.toolExecutionRequests()) {
                logger.debug("Tool execution request: name={}, args={}", request.name(), request.arguments());
                ToolExecutionResultMessage toolResult = toolExecutor.executeTool(request, context);
                messages.add(toolResult);
            }
        }

        ChatMessage lastMessage = messages.get(messages.size() - 1);
        return lastMessage instanceof AiMessage ? ((AiMessage) lastMessage).text() : lastMessage.toString();
    }

    private String callModel(String systemPrompt, String userPrompt, FlowContext context) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(systemPrompt));
        messages.add(UserMessage.from(userPrompt));

        messages = applyAdvisorsBefore(messages, context);

        Response<AiMessage> response = chatModel.generate(messages);
        AiMessage aiMessage = response.content();

        applyAdvisorsAfter(messages, aiMessage, context);

        return aiMessage.text();
    }

    protected List<ChatMessage> applyAdvisorsBefore(List<ChatMessage> messages, FlowContext context) {
        List<ChatMessage> result = messages;
        for (FlowAdvisor advisor : advisors) {
            result = advisor.before(result, context);
        }
        return result;
    }

    protected void applyAdvisorsAfter(List<ChatMessage> messages, AiMessage response, FlowContext context) {
        for (FlowAdvisor advisor : advisors) {
            advisor.after(messages, response, context);
        }
    }

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
