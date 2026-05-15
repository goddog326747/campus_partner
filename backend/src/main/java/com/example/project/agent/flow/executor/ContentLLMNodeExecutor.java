package com.example.project.agent.flow.executor;

import com.example.project.agent.flow.FlowContext;
import com.example.project.agent.flow.FlowNode;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentLLMNodeExecutor implements NodeExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ContentLLMNodeExecutor.class);
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{([\\w.]+)\\}\\}");
    private static final int MAX_TOOL_ITERATIONS = 5;

    private final ChatLanguageModel chatModel;
    private final ToolNodeExecutor toolExecutor;

    public ContentLLMNodeExecutor(ChatLanguageModel chatModel, ToolNodeExecutor toolExecutor) {
        this.chatModel = chatModel;
        this.toolExecutor = toolExecutor;
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
                    "你是一个专业的内容创作助手，擅长生成高质量、有吸引力的内容。");
            String userPromptTemplate = node.getConfig("userPromptTemplate", "{{input}}");
            String userPrompt = resolveTemplate(userPromptTemplate, context);

            boolean useTools = node.getConfig("useTools", false);

            logger.debug("Content LLM Node executing: node={}, useTools={}", node.getName(), useTools);

            String response;
            boolean hasToolCall = false;

            if (useTools && toolExecutor != null && toolExecutor.hasTools()) {
                response = executeWithTools(systemPrompt, userPrompt, context);
                hasToolCall = true;
            } else {
                response = callModel(systemPrompt, userPrompt);
            }

            long endTime = System.currentTimeMillis();

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
            logger.error("Content LLM Node execution failed: node={}, error={}", node.getName(), e.getMessage(), e);
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

        List<ToolSpecification> toolSpecs = toolExecutor.buildToolSpecifications();

        int iteration = 0;
        while (iteration < MAX_TOOL_ITERATIONS) {
            iteration++;

            Response<AiMessage> response = chatModel.generate(messages, toolSpecs);
            AiMessage aiMessage = response.content();
            messages.add(aiMessage);

            if (aiMessage.toolExecutionRequests() == null || aiMessage.toolExecutionRequests().isEmpty()) {
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

    private String callModel(String systemPrompt, String userPrompt) {
        Response<AiMessage> response = chatModel.generate(Arrays.asList(
                SystemMessage.from(systemPrompt),
                UserMessage.from(userPrompt)
        ));
        return response.content().text();
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
