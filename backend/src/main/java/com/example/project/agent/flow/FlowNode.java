package com.example.project.agent.flow;

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
public class FlowNode {
    
    private final String nodeId;
    private final String name;
    private final NodeType type;
    private final Function<FlowContext, FlowNodeExecutionResult> executor;
    private final Map<String, Object> config;
    private final List<String> dependencies;
    
    private FlowNode(Builder builder) {
        this.nodeId = builder.nodeId;
        this.name = builder.name;
        this.type = builder.type;
        this.executor = builder.executor;
        this.config = new HashMap<>(builder.config);
        this.dependencies = new ArrayList<>(builder.dependencies);
    }
    
    public static Builder builder() {
        return new Builder();
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
    
    // ============ Getters ============
    
    public String getNodeId() { return nodeId; }
    public String getName() { return name; }
    public NodeType getType() { return type; }
    public Map<String, Object> getConfig() { return Collections.unmodifiableMap(config); }
    public List<String> getDependencies() { return Collections.unmodifiableList(dependencies); }
    
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String key) {
        return (T) config.get(key);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String key, T defaultValue) {
        return (T) config.getOrDefault(key, defaultValue);
    }
    
    /**
     * 节点构建器
     */
    public static class Builder {
        private String nodeId = UUID.randomUUID().toString();
        private String name = "UnnamedNode";
        private NodeType type = NodeType.LLM;
        private Function<FlowContext, FlowNodeExecutionResult> executor = ctx -> 
            FlowNodeExecutionResult.builder().success(true).output("").build();
        private Map<String, Object> config = new HashMap<>();
        private List<String> dependencies = new ArrayList<>();
        
        public Builder nodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder type(NodeType type) {
            this.type = type;
            return this;
        }
        
        public Builder executor(Function<FlowContext, FlowNodeExecutionResult> executor) {
            this.executor = executor;
            return this;
        }
        
        public Builder config(String key, Object value) {
            this.config.put(key, value);
            return this;
        }
        
        public Builder config(Map<String, Object> config) {
            this.config.putAll(config);
            return this;
        }
        
        public Builder dependsOn(String... nodeIds) {
            this.dependencies.addAll(Arrays.asList(nodeIds));
            return this;
        }
        
        /**
         * 创建 LLM 节点
         */
        public Builder llm(String systemPrompt, String userPromptTemplate) {
            this.type = NodeType.LLM;
            this.config.put("systemPrompt", systemPrompt);
            this.config.put("userPromptTemplate", userPromptTemplate);
            return this;
        }
        
        /**
         * 创建工具节点
         */
        public Builder tool(String toolName, String description) {
            this.type = NodeType.TOOL;
            this.config.put("toolName", toolName);
            this.config.put("description", description);
            return this;
        }
        
        /**
         * 创建条件节点
         */
        public Builder condition(String conditionName) {
            this.type = NodeType.CONDITION;
            this.config.put("conditionName", conditionName);
            return this;
        }
        
        /**
         * 创建起始节点
         */
        public Builder start() {
            this.type = NodeType.START;
            this.name = "Start";
            return this;
        }
        
        /**
         * 创建结束节点
         */
        public Builder end() {
            this.type = NodeType.END;
            this.name = "End";
            return this;
        }
        
        public FlowNode build() {
            return new FlowNode(this);
        }
    }
}
