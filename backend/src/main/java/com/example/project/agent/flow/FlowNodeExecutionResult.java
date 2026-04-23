package com.example.project.agent.flow;

import java.util.*;

/**
 * 节点执行结果
 */
public class FlowNodeExecutionResult {
    
    private final String nodeId;
    private final String nodeName;
    private final boolean success;
    private final Object output;
    private final String error;
    private final Map<String, Object> metadata;
    private final long executionTimeMs;
    
    private FlowNodeExecutionResult(Builder builder) {
        this.nodeId = builder.nodeId;
        this.nodeName = builder.nodeName;
        this.success = builder.success;
        this.output = builder.output;
        this.error = builder.error;
        this.metadata = new HashMap<>(builder.metadata);
        this.executionTimeMs = builder.executionTimeMs;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
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
    
    // ============ Getters ============
    
    public String getNodeId() { return nodeId; }
    public String getNodeName() { return nodeName; }
    public boolean isSuccess() { return success; }
    public Object getOutput() { return output; }
    public String getError() { return error; }
    public Map<String, Object> getMetadata() { return Collections.unmodifiableMap(metadata); }
    public long getExecutionTimeMs() { return executionTimeMs; }
    
    @SuppressWarnings("unchecked")
    public <T> T getOutputAs() {
        return (T) output;
    }
    
    /**
     * 结果构建器
     */
    public static class Builder {
        private String nodeId;
        private String nodeName;
        private boolean success;
        private Object output;
        private String error;
        private Map<String, Object> metadata = new HashMap<>();
        private long executionTimeMs;
        
        public Builder nodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }
        
        public Builder nodeName(String nodeName) {
            this.nodeName = nodeName;
            return this;
        }
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder output(Object output) {
            this.output = output;
            return this;
        }
        
        public Builder error(String error) {
            this.error = error;
            return this;
        }
        
        public Builder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata.putAll(metadata);
            return this;
        }
        
        public Builder executionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }
        
        public FlowNodeExecutionResult build() {
            return new FlowNodeExecutionResult(this);
        }
    }
}
