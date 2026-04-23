package com.example.project.agent.flow.executor;

import com.example.project.agent.flow.FlowContext;
import com.example.project.agent.flow.FlowNode;
import com.example.project.agent.flow.dto.FlowNodeExecutionResult;
import com.example.project.agent.flow.enums.NodeType;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LLM 节点执行器
 * 
 * ============================================================
 *                    LLM 节点执行原理
 * ============================================================
 * 
 * 这个执行器负责调用大语言模型：
 * 
 * 1. 解析提示词模板
 * 2. 从上下文中获取变量
 * 3. 调用 AI 模型
 * 4. 返回结果
 * 
 * 提示词模板语法：
 * - {{variableName}}: 从上下文中获取变量
 * - {{input.key}}: 从初始输入中获取
 * - {{node.nodeId.output}}: 从其他节点输出中获取
 * 
 * 示例：
 * "根据用户风格 {{node.userStyle.output}} 生成一篇关于 {{input.topic}} 的帖子"
 * 
 * ============================================================
 */
public class LLMNodeExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(LLMNodeExecutor.class);
    
    private final ChatLanguageModel chatModel;
    
    public LLMNodeExecutor(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }
    
    /**
     * 执行 LLM 节点
     */
    public FlowNodeExecutionResult execute(FlowNode node, FlowContext context) {
        try {
            long startTime = System.currentTimeMillis();
            
            // 1. 获取系统提示词
            String systemPrompt = node.getConfig("systemPrompt", 
                    "你是一个专业的 AI 助手，请根据用户输入提供有帮助的回答。");
            
            // 2. 获取用户提示词模板并解析
            String userPromptTemplate = node.getConfig("userPromptTemplate", "{{input}}");
            String userPrompt = resolveTemplate(userPromptTemplate, context);
            
            logger.debug("LLM Node executing: node={}, prompt={}", node.getName(), 
                    userPrompt.substring(0, Math.min(100, userPrompt.length())));
            
            // 3. 调用 AI 模型
            String response = callModel(systemPrompt, userPrompt);
            
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
     * 调用 AI 模型
     */
    private String callModel(String systemPrompt, String userPrompt) {
        // 构建完整提示词
        String fullPrompt = String.format("%s\n\n用户输入：%s", systemPrompt, userPrompt);
        
        // 调用 LangChain4j 模型
        return chatModel.generate(fullPrompt);
    }
    
    /**
     * 解析模板
     * 
     * 支持的变量格式：
     * - {{input.key}}: 从初始输入获取
     * - {{variable.key}}: 从变量获取
     * - {{node.nodeId.output}}: 从节点输出获取
     * - {{lastOutput}}: 上一个节点的输出
     */
    private String resolveTemplate(String template, FlowContext context) {
        if (template == null || template.isEmpty()) {
            return "";
        }
        
        String result = template;
        
        // 解析 {{input.key}}
        Pattern inputPattern = Pattern.compile("\\{\\{input\\.(\\w+)\\}\\}");
        Matcher inputMatcher = inputPattern.matcher(result);
        while (inputMatcher.find()) {
            String key = inputMatcher.group(1);
            Object value = context.getInput(key);
            result = result.replace(inputMatcher.group(0), value != null ? value.toString() : "");
        }
        
        // 解析 {{variable.key}}
        Pattern varPattern = Pattern.compile("\\{\\{variable\\.(\\w+)\\}\\}");
        Matcher varMatcher = varPattern.matcher(result);
        while (varMatcher.find()) {
            String key = varMatcher.group(1);
            Object value = context.getVariable(key);
            result = result.replace(varMatcher.group(0), value != null ? value.toString() : "");
        }
        
        // 解析 {{node.nodeId.output}}
        Pattern nodePattern = Pattern.compile("\\{\\{node\\.(\\w+)\\.output\\}\\}");
        Matcher nodeMatcher = nodePattern.matcher(result);
        while (nodeMatcher.find()) {
            String nodeId = nodeMatcher.group(1);
            Object value = context.getNodeOutput(nodeId);
            result = result.replace(nodeMatcher.group(0), value != null ? value.toString() : "");
        }
        
        // 解析 {{lastOutput}}
        result = result.replace("{{lastOutput}}", 
                context.getLastOutput() != null ? context.getLastOutput().toString() : "");
        
        return result;
    }
    
    /**
     * 创建 LLM 节点
     */
    public static FlowNode createNode(String nodeId, String name, String systemPrompt, String userPromptTemplate) {
        return FlowNode.builder()
                .nodeId(nodeId)
                .name(name)
                .type(NodeType.LLM)
                .config("systemPrompt", systemPrompt)
                .config("userPromptTemplate", userPromptTemplate)
                .build();
    }
}
