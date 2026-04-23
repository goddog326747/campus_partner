package com.example.project.agent.flow;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Agent Flow 链路执行引擎
 * 
 * ============================================================
 *                    Agent Flow 核心设计
 * ============================================================
 * 
 * Agent Flow 是一个灵活的 AI 工作流编排框架，支持两种核心模式：
 * 
 * 1. 规划执行模式 (Planning & Execution)
 *    - 先规划所有步骤，再按顺序执行
 *    - 适合确定性任务
 *    - 可预见的执行路径
 * 
 * 2. ReAct 模式 (Reasoning + Acting)
 *    - 边推理边执行
 *    - 根据中间结果动态调整下一步
 *    - 适合探索性任务
 * 
 * 核心概念：
 * - FlowNode: 流程节点，代表一个执行单元
 * - FlowEdge: 流程边，代表节点间的流转关系
 * - FlowContext: 流程上下文，存储执行状态和中间结果
 * - FlowEngine: 流程引擎，负责调度和执行
 * 
 * ============================================================
 */
public class AgentFlow {
    
    private final String flowId;
    private final String name;
    private final Map<String, FlowNode> nodes;
    private final List<FlowEdge> edges;
    private final FlowMode mode;
    
    private AgentFlow(Builder builder) {
        this.flowId = builder.flowId;
        this.name = builder.name;
        this.nodes = new HashMap<>(builder.nodes);
        this.edges = new ArrayList<>(builder.edges);
        this.mode = builder.mode;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * 执行流程
     * 
     * @param initialInput 初始输入
     * @return 执行结果
     */
    public FlowResult execute(Map<String, Object> initialInput) {
        FlowContext context = new FlowContext(flowId, initialInput);
        FlowEngine engine = new FlowEngine(this, context);
        return engine.execute();
    }
    
    /**
     * 异步执行流程
     */
    public CompletableFuture<FlowResult> executeAsync(Map<String, Object> initialInput) {
        return CompletableFuture.supplyAsync(() -> execute(initialInput));
    }
    
    // ============ Getters ============
    
    public String getFlowId() { return flowId; }
    public String getName() { return name; }
    public Map<String, FlowNode> getNodes() { return Collections.unmodifiableMap(nodes); }
    public List<FlowEdge> getEdges() { return Collections.unmodifiableList(edges); }
    public FlowMode getMode() { return mode; }
    
    public FlowNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }
    
    public List<FlowEdge> getOutgoingEdges(String nodeId) {
        return edges.stream()
                .filter(e -> e.getFromNodeId().equals(nodeId))
                .toList();
    }
    
    public List<FlowNode> getStartNodes() {
        Set<String> targetNodes = edges.stream()
                .map(FlowEdge::getToNodeId)
                .collect(java.util.HashSet::new, java.util.HashSet::add, java.util.HashSet::addAll);
        
        return nodes.values().stream()
                .filter(n -> !targetNodes.contains(n.getNodeId()))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 流程构建器
     */
    public static class Builder {
        private String flowId = UUID.randomUUID().toString();
        private String name = "UnnamedFlow";
        private Map<String, FlowNode> nodes = new HashMap<>();
        private List<FlowEdge> edges = new ArrayList<>();
        private FlowMode mode = FlowMode.PLANNING;
        
        public Builder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder mode(FlowMode mode) {
            this.mode = mode;
            return this;
        }
        
        public Builder addNode(FlowNode node) {
            this.nodes.put(node.getNodeId(), node);
            return this;
        }
        
        public Builder addEdge(String fromNodeId, String toNodeId) {
            return addEdge(fromNodeId, toNodeId, null);
        }
        
        public Builder addEdge(String fromNodeId, String toNodeId, Function<FlowContext, Boolean> condition) {
            this.edges.add(new FlowEdge(fromNodeId, toNodeId, condition));
            return this;
        }
        
        public Builder addSequentialEdge(String fromNodeId, String toNodeId) {
            return addEdge(fromNodeId, toNodeId, ctx -> {
                FlowNodeExecutionResult lastResult = ctx.getLastResult();
                return lastResult != null && lastResult.isSuccess();
            });
        }
        
        public AgentFlow build() {
            if (nodes.isEmpty()) {
                throw new IllegalStateException("Flow must have at least one node");
            }
            return new AgentFlow(this);
        }
    }
}
