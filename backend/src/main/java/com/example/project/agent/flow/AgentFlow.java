package com.example.project.agent.flow;

import com.example.project.agent.flow.dto.FlowResult;
import com.example.project.agent.flow.enums.FlowMode;
import lombok.Builder;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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
@Getter
public class AgentFlow {
    
    private final String flowId;
    private final String name;
    private final Map<String, FlowNode> nodes;
    private final List<FlowEdge> edges;
    private final FlowMode mode;
    
    @Builder
    public AgentFlow(String flowId, String name, 
                     Map<String, FlowNode> nodes, 
                     List<FlowEdge> edges, 
                     FlowMode mode) {
        this.flowId = flowId != null ? flowId : UUID.randomUUID().toString();
        this.name = name != null ? name : "UnnamedFlow";
        this.nodes = new HashMap<>(nodes != null ? nodes : new HashMap<>());
        this.edges = new ArrayList<>(edges != null ? edges : new ArrayList<>());
        this.mode = mode != null ? mode : FlowMode.PLANNING;
        
        if (this.nodes.isEmpty()) {
            throw new IllegalStateException("Flow must have at least one node");
        }
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
}
