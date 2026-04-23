package com.example.project.agent.flow.dto;

import com.example.project.agent.flow.FlowContext;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.*;

/**
 * 流程执行结果
 */
@Getter
@Builder
public class FlowResult {

    private final String flowId;
    private final String executionId;
    private final boolean success;
    private final Object output;
    private final String error;
    private final FlowContext context;
    @Singular("metadata")
    private final Map<String, Object> metadata;

    /**
     * 创建成功结果
     */
    public static FlowResult success(String flowId, String executionId, Object output, FlowContext context) {
        return builder()
                .flowId(flowId)
                .executionId(executionId)
                .success(true)
                .output(output)
                .context(context)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static FlowResult failure(String flowId, String executionId, String error, FlowContext context) {
        return builder()
                .flowId(flowId)
                .executionId(executionId)
                .success(false)
                .error(error)
                .context(context)
                .build();
    }

    @SuppressWarnings("unchecked")
    public <T> T getOutputAs() {
        return (T) output;
    }

    /**
     * 获取执行耗时（毫秒）
     */
    public long getExecutionTimeMs() {
        return context != null ? context.getExecutionTimeMs() : 0;
    }

    /**
     * 获取执行的节点数量
     */
    public int getExecutedNodeCount() {
        return context != null ? context.getExecutionHistory().size() : 0;
    }
}
