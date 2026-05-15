package com.example.project.agent.flow.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.*;

/**
 * 流程节点执行结果 DTO
 * <p>
 * 封装单个流程节点的执行结果，包括执行状态、输出数据、错误信息和元数据。
 * 使用 Lombok {@link Builder} 模式构建，支持不可变对象。
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Getter
@Builder
public class FlowNodeExecutionResult {

    /** 节点 ID */
    private final String nodeId;
    /** 节点名称 */
    private final String nodeName;
    /** 是否执行成功 */
    private final boolean success;
    /** 执行输出 */
    private final Object output;
    /** 错误信息 */
    private final String error;
    /** 元数据映射 */
    @Singular("metadata")
    private final Map<String, Object> metadata;
    /** 执行耗时（毫秒），默认为 0 */
    @Builder.Default
    private final long executionTimeMs = 0;

    /**
     * 创建成功结果
     *
     * @param nodeId   节点 ID
     * @param nodeName 节点名称
     * @param output   执行输出
     * @return 成功的节点执行结果
     */
    public static FlowNodeExecutionResult success(String nodeId, String nodeName, Object output) {
        return builder()
                .nodeId(nodeId)
                .nodeName(nodeName)
                .success(true)
                .output(output)
                .build();
    }
    
    /**
     * 创建失败结果
     *
     * @param nodeId   节点 ID
     * @param nodeName 节点名称
     * @param error    错误信息
     * @return 失败的节点执行结果
     */
    public static FlowNodeExecutionResult failure(String nodeId, String nodeName, String error) {
        return builder()
                .nodeId(nodeId)
                .nodeName(nodeName)
                .success(false)
                .error(error)
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
     * 返回带有执行耗时的新结果（如果原结果没有记录耗时）
     *
     * @param executionTimeMs 执行耗时（毫秒）
     * @return 包含执行耗时的新结果实例
     */
    public FlowNodeExecutionResult withExecutionTime(long executionTimeMs) {
        return builder()
                .nodeId(nodeId)
                .nodeName(nodeName)
                .success(success)
                .output(output)
                .error(error)
                .metadata(metadata != null ? new HashMap<>(metadata) : new HashMap<>())
                .executionTimeMs(executionTimeMs)
                .build();
    }
}
