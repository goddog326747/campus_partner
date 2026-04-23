package com.example.project.agent.flow;

import java.util.*;

/**
 * 流程执行结果
 */
public class FlowResult {
    
    private final String flowId;
    private final String executionId;
    private final boolean success;
    private final Object output;
    private final String error;
    private final FlowContext context;
    private final Map<String, Object> metadata;
    
    private FlowResult(Builder builder) {
        this.flowId = builder.flowId;
        this.executionId = builder.executionId;
        this.success = builder.success;
        this.output = builder.output;
        this.error = builder.error;
        this.context = builder.context;
        this.metadata = new HashMap<>(builder.metadata);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
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
    
    // ============ Getters ============
    
    public String getFlowId() { return flowId; }
    public String getExecutionId() { return executionId; }
    public boolean isSuccess() { return success; }
    public Object getOutput() { return output; }
    public String getError() { return error; }
    public FlowContext getContext() { return context; }
    public Map<String, Object> getMetadata() { return Collections.unmodifiableMap(metadata); }
    
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
    
    /**
     * 结果构建器
     */
    public static class Builder {
        private String flowId;
        private String executionId;
        private boolean success;
        private Object output;
        private String error;
        private FlowContext context;
        private Map<String, Object> metadata = new HashMap<>();
        
        public Builder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }
        
        public Builder executionId(String executionId) {
            this.executionId = executionId;
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
        
        public Builder context(FlowContext context) {
            this.context = context;
            return this;
        }
        
        public Builder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public FlowResult build() {
            return new FlowResult(this);
        }
    }
}
