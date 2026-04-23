package com.example.project.agent.flow.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.*;

/**
 * 节点执行结果
 */
@Getter
@Builder
public class FlowNodeExecutionResult {
    
    private final String nodeId;
    private final String nodeName;
    private final boolean success;
    private final Object output;
    private final String error;
    @Singular("metadata")
    private final Map<String, Object> metadata;
    @Builder.Default
    private final long executionTimeMs = 0;
    
    /**
     * 创建成功结果
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
     */
    public static FlowNodeExecutionResult failure(String nodeId, String nodeName, String error) {
        return builder()
                .nodeId(nodeId)
                .nodeName(nodeName)
                .success(false)
                .error(error)
                .build();
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getOutputAs() {
        return (T) output;
    }
}
