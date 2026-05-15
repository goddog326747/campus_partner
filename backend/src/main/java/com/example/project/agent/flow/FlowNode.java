package com.example.project.agent.flow;

import com.example.project.agent.flow.dto.FlowNodeExecutionResult;
import com.example.project.agent.flow.enums.NodeType;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.*;
import java.util.function.BiFunction;

/**
 * 流程节点，表示 AI 工作流中的一个执行单元。
 * <p>
 * 每个节点具有唯一标识、名称、类型、执行器以及配置信息。
 * 在新架构中，<b>所有业务逻辑都由 LLM 节点处理</b>，工具调用内嵌在 LLM 节点中自主完成，
 * 不再定义独立的 TOOL 节点。
 *
 * ============================================================
 *                    节点类型详解
 * ============================================================
 *
 * 【START】起始节点
 * - 流程的入口点
 * - 负责接收初始输入
 *
 * 【LLM】AI 模型节点
 * - 调用大语言模型生成内容
 * - 支持系统提示词和用户输入模板
 * - <b>内部支持自主工具调用</b>（由 LLM 决定是否需要工具）
 * - 可配置 temperature、maxTokens 等参数
 *
 * 【CONDITION】条件节点
 * - 根据条件决定流程走向
 * - 支持复杂逻辑判断
 *
 * 【END】结束节点
 * - 流程的出口点
 * - 返回最终结果
 *
 * ============================================================
 *
 * @author example
 * @since 1.0.0
 */
@Getter
public class FlowNode {

    /** 节点唯一标识 */
    private final String nodeId;
    /** 节点名称 */
    private final String name;
    /** 节点类型 */
    private final NodeType type;
    /** 节点执行器，接收当前节点和上下文，返回执行结果 */
    private final BiFunction<FlowNode, FlowContext, FlowNodeExecutionResult> executor;
    /** 节点配置参数 */
    private final Map<String, Object> config;
    /** 节点依赖的其他节点 ID 列表 */
    private final List<String> dependencies;

    /**
     * 构造方法，通过 Builder 创建节点实例。
     *
     * @param nodeId       节点唯一标识
     * @param name         节点名称
     * @param type         节点类型
     * @param executor     节点执行器
     * @param config       节点配置参数
     * @param dependencies 依赖节点 ID 列表
     */
    @Builder
    public FlowNode(String nodeId, String name, NodeType type,
                    BiFunction<FlowNode, FlowContext, FlowNodeExecutionResult> executor,
                    @Singular("config") Map<String, Object> config,
                    @Singular("dependency") List<String> dependencies) {
        this.nodeId = nodeId != null ? nodeId : UUID.randomUUID().toString();
        this.name = name != null ? name : "UnnamedNode";
        this.type = type != null ? type : NodeType.LLM;
        this.executor = executor != null ? executor : (node, ctx) ->
            FlowNodeExecutionResult.builder().success(true).output("").build();
        this.config = new HashMap<>(config != null ? config : new HashMap<>());
        this.dependencies = new ArrayList<>(dependencies != null ? dependencies : new ArrayList<>());
    }

    /**
     * 执行当前节点。
     * <p>
     * 调用执行器并自动计算执行耗时，若执行过程中发生异常则返回失败结果。
     *
     * @param context 流程执行上下文
     * @return 节点执行结果
     */
    public FlowNodeExecutionResult execute(FlowContext context) {
        long startTime = System.currentTimeMillis();
        try {
            FlowNodeExecutionResult result = executor.apply(this, context);
            long endTime = System.currentTimeMillis();
            if (result.getExecutionTimeMs() == 0) {
                return result.withExecutionTime(endTime - startTime);
            }
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            return FlowNodeExecutionResult.builder()
                    .nodeId(nodeId)
                    .nodeName(name)
                    .success(false)
                    .error(e.getMessage())
                    .executionTimeMs(endTime - startTime)
                    .build();
        }
    }

    /**
     * 获取指定键的配置值。
     *
     * @param key 配置键
     * @param <T> 配置值类型
     * @return 配置值，若不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String key) {
        return (T) config.get(key);
    }

    /**
     * 获取指定键的配置值，若不存在则返回默认值。
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @param <T>          配置值类型
     * @return 配置值或默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String key, T defaultValue) {
        return (T) config.getOrDefault(key, defaultValue);
    }

    /**
     * 创建 LLM 节点的便捷构建器。
     *
     * @param nodeId            节点 ID
     * @param name              节点名称
     * @param systemPrompt      系统提示词
     * @param userPromptTemplate 用户提示词模板
     * @return LLM 节点构建器
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
     * 创建起始节点的便捷方法。
     *
     * @param nodeId 节点 ID
     * @return 起始节点实例
     */
    public static FlowNode startNode(String nodeId) {
        return builder()
                .nodeId(nodeId)
                .name("Start")
                .type(NodeType.START)
                .executor((node, ctx) -> FlowNodeExecutionResult.success(node.getNodeId(), node.getName(), "Started"))
                .build();
    }

    /**
     * 创建循环节点的便捷构建器。
     *
     * @param nodeId        节点 ID
     * @param name          节点名称
     * @param loopTarget    循环回跳的目标节点 ID
     * @param loopCondition 循环条件描述（供 LLM 判断是否继续）
     * @return 循环节点构建器
     */
    public static FlowNodeBuilder loopBuilder(String nodeId, String name,
                                               String loopTarget, String loopCondition) {
        return builder()
                .nodeId(nodeId)
                .name(name)
                .type(NodeType.LOOP)
                .config("loopTarget", loopTarget)
                .config("loopCondition", loopCondition)
                .config("maxIterations", 3);
    }

    /**
     * 创建结束节点的便捷方法。
     *
     * @param nodeId 节点 ID
     * @return 结束节点实例
     */
    public static FlowNode endNode(String nodeId) {
        return builder()
                .nodeId(nodeId)
                .name("End")
                .type(NodeType.END)
                .executor((node, ctx) -> FlowNodeExecutionResult.success(node.getNodeId(), node.getName(), ctx.getLastOutput()))
                .build();
    }
}
