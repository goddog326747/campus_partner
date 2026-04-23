package com.example.project.agent.flow;

import com.example.project.agent.flow.dto.FlowNodeExecutionResult;
import com.example.project.agent.flow.enums.NodeType;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.*;
import java.util.function.Function;

/**
 * 流程节点
 * 
 * ============================================================
 *                    节点类型详解
 * ============================================================
 * 
 * 【START】起始节点
 * - 流程的入口点
 * - 负责接收初始输入
 * - 每个流程必须有且只有一个 START 节点
 * 
 * 【LLM】AI 模型节点
 * - 调用大语言模型
 * - 支持系统提示词和用户输入
 * - 可配置 temperature、maxTokens 等参数
 * 
 * 【TOOL】工具调用节点
 * - 调用外部工具/函数
 * - 如：查询数据库、调用 API、搜索等
 * - 参数可从上下文中动态获取
 * 
 * 【CONDITION】条件节点
 * - 根据条件决定流程走向
 * - 支持复杂逻辑判断
 * - 多分支输出
 * 
 * 【LOOP】循环节点
 * - 支持重复执行子流程
 * - 可设置最大迭代次数
 * - 支持 break 条件
 * 
 * 【PARALLEL】并行节点
 * - 同时执行多个子节点
 * - 等待所有子节点完成
 * - 合并结果
 * 
 * 【END】结束节点
 * - 流程的出口点
 * - 返回最终结果
 * - 可进行结果格式化
 * 
 * ============================================================
 */
@Getter
public class FlowNode {
    
    private final String nodeId;
    private final String name;
    private final NodeType type;
    private final Function<FlowContext, FlowNodeExecutionResult> executor;
    private final Map<String, Object> config;
    private final List<String> dependencies;
    
    @Builder
    public FlowNode(String nodeId, String name, NodeType type, 
                    Function<FlowContext, FlowNodeExecutionResult> executor,
                    @Singular("config") Map<String, Object> config,
                    @Singular("dependency") List<String> dependencies) {
        this.nodeId = nodeId != null ? nodeId : UUID.randomUUID().toString();
        this.name = name != null ? name : "UnnamedNode";
        this.type = type != null ? type : NodeType.LLM;
        this.executor = executor != null ? executor : ctx -> 
            FlowNodeExecutionResult.builder().success(true).output("").build();
        this.config = new HashMap<>(config != null ? config : new HashMap<>());
        this.dependencies = new ArrayList<>(dependencies != null ? dependencies : new ArrayList<>());
    }
    
    /**
     * 执行节点
     */
    public FlowNodeExecutionResult execute(FlowContext context) {
        try {
            long startTime = System.currentTimeMillis();
            FlowNodeExecutionResult result = executor.apply(context);
            long endTime = System.currentTimeMillis();
            
            return FlowNodeExecutionResult.builder()
                    .nodeId(nodeId)
                    .nodeName(name)
                    .success(result.isSuccess())
                    .output(result.getOutput())
                    .metadata(result.getMetadata())
                    .executionTimeMs(endTime - startTime)
                    .build();
        } catch (Exception e) {
            return FlowNodeExecutionResult.builder()
                    .nodeId(nodeId)
                    .nodeName(name)
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String key) {
        return (T) config.get(key);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String key, T defaultValue) {
        return (T) config.getOrDefault(key, defaultValue);
    }
    
    /**
     * 创建 LLM 节点的便捷方法
     */
    public static FlowNodeBuilder llmBuilder(String nodeId, String name, 
                                              String systemPrompt, String userPromptTemplate) {
        return builder()
                .nodeId(nodeId)
                .name(name)
                .type(NodeType.LLM)
                .config("systemPrompt", systemPrompt)
                .config("userPromptTemplate", userPromptTemplate);
    }
    
    /**
     * 创建工具节点的便捷方法
     */
    public static FlowNodeBuilder toolBuilder(String nodeId, String name, String toolName) {
        return builder()
                .nodeId(nodeId)
                .name(name)
                .type(NodeType.TOOL)
                .config("toolName", toolName);
    }
    
    /**
     * 创建起始节点的便捷方法
     */
    public static FlowNode startNode(String nodeId) {
        return builder()
                .nodeId(nodeId)
                .name("Start")
                .type(NodeType.START)
                .executor(ctx -> FlowNodeExecutionResult.success(nodeId, "Start", "Started"))
                .build();
    }
    
    /**
     * 创建结束节点的便捷方法
     */
    public static FlowNode endNode(String nodeId) {
        return builder()
                .nodeId(nodeId)
                .name("End")
                .type(NodeType.END)
                .executor(ctx -> FlowNodeExecutionResult.success(nodeId, "End", ctx.getLastOutput()))
                .build();
    }
}
