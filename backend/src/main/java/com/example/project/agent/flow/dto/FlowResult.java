package com.example.project.agent.flow.dto;

import com.example.project.agent.flow.FlowContext;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.*;

/**
 * 流程执行结果 DTO
 * <p>
 * 封装整个 AgentFlow 的执行结果，包含流程 ID、执行状态、输出数据、
 * 错误信息、流程上下文和元数据。
 * 使用 Lombok {@link Builder} 模式构建。
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Getter
@Builder
public class FlowResult {

    /** 流程 ID */
    private final String flowId;
    /** 执行 ID */
    private final String executionId;
    /** 是否执行成功 */
    private final boolean success;
    /** 执行输出 */
    private final Object output;
    /** 错误信息 */
    private final String error;
    /** 流程上下文 */
    private final FlowContext context;
    /** 元数据映射 */
    private final Map<String, Object> metadata;

    /**
     * 创建成功的流程结果
     *
     * @param flowId      流程 ID
     * @param executionId 执行 ID
     * @param output      执行输出
     * @param context     流程上下文
     * @return 成功的流程结果
     */
    public static FlowResult success(String flowId, String executionId, Object output, FlowContext context) {
        return builder()
                .flowId(flowId)
                .executionId(executionId)
                .success(true)
                .output(output)
                .context(context)
                .metadata(new HashMap<>())
                .build();
    }

    /**
     * 创建失败的流程结果
     *
     * @param flowId      流程 ID
     * @param executionId 执行 ID
     * @param error       错误信息
     * @param context     流程上下文
     * @return 失败的流程结果
     */
    public static FlowResult failure(String flowId, String executionId, String error, FlowContext context) {
        return builder()
                .flowId(flowId)
                .executionId(executionId)
                .success(false)
                .error(error)
                .context(context)
                .metadata(new HashMap<>())
                .build();
    }

    /**
     * 将输出转换为指定类型
     *
     * @param <T> 目标类型
     * @return 类型转换后的输出
     */
    @SuppressWarnings("unchecked")
    public <T> T getOutputAs() {
        return (T) output;
    }

    /**
     * 获取执行耗时（毫秒）
     *
     * @return 执行耗时，上下文为空时返回 0
     */
    public long getExecutionTimeMs() {
        return context != null ? context.getExecutionTimeMs() : 0;
    }

    /**
     * 获取已执行节点数量
     *
     * @return 已执行节点数，上下文为空时返回 0
     */
    public int getExecutedNodeCount() {
        return context != null ? context.getExecutionHistory().size() : 0;
    }

    /**
     * 获取元数据映射
     *
     * @return 元数据映射，为空时返回空 HashMap
     */
    public Map<String, Object> getMetadata() {
        return metadata != null ? metadata : new HashMap<>();
    }
}
