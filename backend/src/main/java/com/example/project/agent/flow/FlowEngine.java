package com.example.project.agent.flow;

import com.example.project.agent.flow.dto.FlowNodeExecutionResult;
import com.example.project.agent.flow.dto.FlowResult;
import com.example.project.agent.flow.enums.FlowMode;
import com.example.project.agent.flow.enums.FlowStatus;
import com.example.project.agent.flow.enums.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 流程执行引擎
 * 
 * ============================================================
 *                    引擎核心职责
 * ============================================================
 * 
 * FlowEngine 负责调度和执行 Agent Flow：
 * 
 * 1. 根据 FlowMode 选择执行策略
 * 2. 管理节点执行顺序
 * 3. 处理条件分支
 * 4. 记录执行状态
 * 5. 错误处理和恢复
 * 
 * 执行策略：
 * - Planning 模式：拓扑排序后顺序执行
 * - ReAct 模式：动态决定下一步
 * - Hybrid 模式：先规划，执行中支持动态调整
 * 
 * ============================================================
 */
public class FlowEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(FlowEngine.class);
    
    private final AgentFlow flow;
    private final FlowContext context;
    private final ExecutionStrategy strategy;
    
    public FlowEngine(AgentFlow flow, FlowContext context) {
        this.flow = flow;
        this.context = context;
        this.strategy = createStrategy(flow.getMode());
    }
    
    /**
     * 执行流程
     */
    public FlowResult execute() {
        logger.info("Starting flow execution: flowId={}, mode={}, executionId={}", 
                flow.getFlowId(), flow.getMode(), context.getExecutionId());
        
        try {
            return strategy.execute(flow, context);
        } catch (Exception e) {
            logger.error("Flow execution failed: flowId={}, error={}", flow.getFlowId(), e.getMessage(), e);
            context.complete(FlowStatus.FAILED);
            return FlowResult.failure(
                    flow.getFlowId(),
                    context.getExecutionId(),
                    e.getMessage(),
                    context
            );
        }
    }
    
    /**
     * 创建执行策略
     */
    private ExecutionStrategy createStrategy(FlowMode mode) {
        return switch (mode) {
            case PLANNING -> new PlanningExecutionStrategy();
            case REACT -> new ReActExecutionStrategy();
            case HYBRID -> new HybridExecutionStrategy();
        };
    }
    
    /**
     * 执行策略接口
     */
    private interface ExecutionStrategy {
        FlowResult execute(AgentFlow flow, FlowContext context);
    }
    
    /**
     * 规划执行策略
     * 
     * 特点：
     * 1. 先进行拓扑排序确定执行顺序
     * 2. 按顺序依次执行节点
     * 3. 支持条件分支
     * 4. 执行路径在开始时就确定
     */
    private static class PlanningExecutionStrategy implements ExecutionStrategy {
        
        @Override
        public FlowResult execute(AgentFlow flow, FlowContext context) {
            logger.debug("Using Planning execution strategy");
            
            // 1. 获取起始节点
            List<FlowNode> startNodes = flow.getStartNodes();
            if (startNodes.isEmpty()) {
                throw new IllegalStateException("No start nodes found in flow");
            }
            
            // 2. 执行流程
            FlowNode currentNode = startNodes.get(0);
            Object lastOutput = null;
            
            while (currentNode != null && currentNode.getType() != NodeType.END) {
                logger.debug("Executing node: {} ({})", currentNode.getName(), currentNode.getNodeId());
                
                // 执行当前节点
                FlowNodeExecutionResult result = currentNode.execute(context);
                context.recordNodeResult(result);
                
                if (!result.isSuccess()) {
                    logger.error("Node execution failed: {} - {}", currentNode.getName(), result.getError());
                    context.complete(FlowStatus.FAILED);
                    return FlowResult.failure(
                            flow.getFlowId(),
                            context.getExecutionId(),
                            "Node " + currentNode.getName() + " failed: " + result.getError(),
                            context
                    );
                }
                
                lastOutput = result.getOutput();
                logger.debug("Node executed successfully: {} in {}ms", 
                        currentNode.getName(), result.getExecutionTimeMs());
                
                // 确定下一个节点
                currentNode = determineNextNode(flow, context, currentNode);
            }
            
            // 执行结束节点（如果有）
            if (currentNode != null && currentNode.getType() == NodeType.END) {
                FlowNodeExecutionResult endResult = currentNode.execute(context);
                context.recordNodeResult(endResult);
            }
            
            context.complete(FlowStatus.COMPLETED);
            logger.info("Flow execution completed: flowId={}, nodesExecuted={}", 
                    flow.getFlowId(), context.getExecutionHistory().size());
            
            return FlowResult.success(
                    flow.getFlowId(),
                    context.getExecutionId(),
                    lastOutput,
                    context
            );
        }
        
        /**
         * 确定下一个要执行的节点
         */
        private FlowNode determineNextNode(AgentFlow flow, FlowContext context, FlowNode currentNode) {
            List<FlowEdge> outgoingEdges = flow.getOutgoingEdges(currentNode.getNodeId());
            
            if (outgoingEdges.isEmpty()) {
                return null;
            }
            
            // 按优先级选择边：条件边 > 成功边 > 顺序边
            for (FlowEdge edge : outgoingEdges) {
                if (edge.canTraverse(context)) {
                    return flow.getNode(edge.getToNodeId());
                }
            }
            
            // 如果没有边可以走，返回第一个（默认路径）
            return flow.getNode(outgoingEdges.get(0).getToNodeId());
        }
    }
    
    /**
     * ReAct 执行策略
     * 
     * ReAct = Reasoning (推理) + Acting (行动)
     * 
     * 特点：
     * 1. 边推理边执行
     * 2. 每个步骤都有 Thought (思考)
     * 3. 根据观察结果动态调整
     * 4. 支持循环直到找到答案
     * 
     * 执行循环：
     * Thought → Action → Observation → [循环] → Answer
     */
    private static class ReActExecutionStrategy implements ExecutionStrategy {
        
        private static final int MAX_ITERATIONS = 10;
        
        @Override
        public FlowResult execute(AgentFlow flow, FlowContext context) {
            logger.debug("Using ReAct execution strategy");
            
            int iteration = 0;
            ReActState state = new ReActState();
            
            while (iteration < MAX_ITERATIONS) {
                iteration++;
                logger.debug("ReAct iteration: {}", iteration);
                
                // 1. Thought: 思考当前状态，决定下一步
                FlowNode thoughtNode = findNodeByType(flow, NodeType.LLM);
                if (thoughtNode != null) {
                    FlowNodeExecutionResult thoughtResult = thoughtNode.execute(context);
                    context.recordNodeResult(thoughtResult);
                    state.setLastThought(thoughtResult.getOutputAs());
                }
                
                // 2. Action: 执行行动
                FlowNode actionNode = determineNextAction(flow, context, state);
                if (actionNode == null) {
                    logger.debug("No more actions needed, finishing ReAct loop");
                    break;
                }
                
                FlowNodeExecutionResult actionResult = actionNode.execute(context);
                context.recordNodeResult(actionResult);
                state.setLastAction(actionNode.getName());
                state.setLastObservation(actionResult.getOutput());
                
                // 3. 检查是否达到结束条件
                if (shouldTerminate(context, state)) {
                    logger.debug("ReAct termination condition met");
                    break;
                }
                
                // 4. 更新上下文供下一轮使用
                context.setVariable("iteration", iteration);
                context.setVariable("lastThought", state.getLastThought());
                context.setVariable("lastAction", state.getLastAction());
                context.setVariable("lastObservation", state.getLastObservation());
            }
            
            // 生成最终答案
            Object finalOutput = generateFinalAnswer(flow, context, state);
            
            context.complete(FlowStatus.COMPLETED);
            logger.info("ReAct flow execution completed: flowId={}, iterations={}", 
                    flow.getFlowId(), iteration);
            
            return FlowResult.success(
                    flow.getFlowId(),
                    context.getExecutionId(),
                    finalOutput,
                    context
            );
        }
        
        /**
         * 确定下一个行动
         */
        private FlowNode determineNextAction(AgentFlow flow, FlowContext context, ReActState state) {
            // 根据当前状态和目标，选择最合适的工具节点
            List<FlowNode> toolNodes = findNodesByType(flow, NodeType.TOOL);
            
            // 简单策略：按顺序选择工具
            // 实际应用中可以使用 LLM 来决定
            for (FlowNode node : toolNodes) {
                if (!hasBeenExecuted(context, node.getNodeId())) {
                    return node;
                }
            }
            
            return null;
        }
        
        /**
         * 检查是否应该终止
         */
        private boolean shouldTerminate(FlowContext context, ReActState state) {
            // 检查是否有明确的答案
            String lastThought = state.getLastThought();
            if (lastThought != null && lastThought.contains("FINAL ANSWER")) {
                return true;
            }
            
            // 检查是否找到了所需信息
            Object goal = context.getInput("goal");
            if (goal != null && state.getLastObservation() != null) {
                // 简单的终止条件：观察结果包含目标关键词
                String observation = state.getLastObservation().toString();
                return observation.toLowerCase().contains(goal.toString().toLowerCase());
            }
            
            return false;
        }
        
        /**
         * 生成最终答案
         */
        private Object generateFinalAnswer(AgentFlow flow, FlowContext context, ReActState state) {
            // 使用所有收集到的信息生成最终答案
            StringBuilder answer = new StringBuilder();
            answer.append("基于以下推理过程：\n\n");
            
            for (FlowContext.ExecutionStep step : context.getExecutionHistory()) {
                FlowNodeExecutionResult result = context.getNodeResults().get(step.getNodeId());
                if (result != null && result.isSuccess()) {
                    answer.append("- ").append(step.getNodeName())
                          .append(": ").append(result.getOutput()).append("\n");
                }
            }
            
            return answer.toString();
        }
        
        private FlowNode findNodeByType(AgentFlow flow, NodeType type) {
            return flow.getNodes().values().stream()
                    .filter(n -> n.getType() == type)
                    .findFirst()
                    .orElse(null);
        }
        
        private List<FlowNode> findNodesByType(AgentFlow flow, NodeType type) {
            return flow.getNodes().values().stream()
                    .filter(n -> n.getType() == type)
                    .toList();
        }
        
        private boolean hasBeenExecuted(FlowContext context, String nodeId) {
            return context.getNodeResults().containsKey(nodeId);
        }
    }
    
    /**
     * ReAct 状态
     */
    private static class ReActState {
        private String lastThought;
        private String lastAction;
        private Object lastObservation;
        
        public String getLastThought() { return lastThought; }
        public void setLastThought(String lastThought) { this.lastThought = lastThought; }
        
        public String getLastAction() { return lastAction; }
        public void setLastAction(String lastAction) { this.lastAction = lastAction; }
        
        public Object getLastObservation() { return lastObservation; }
        public void setLastObservation(Object lastObservation) { this.lastObservation = lastObservation; }
    }
    
    /**
     * 混合执行策略
     * 
     * 结合规划和 ReAct 的优点：
     * 1. 先进行整体规划
     * 2. 执行过程中支持动态调整
     * 3. 关键节点使用 ReAct 循环
     */
    private static class HybridExecutionStrategy implements ExecutionStrategy {
        
        @Override
        public FlowResult execute(AgentFlow flow, FlowContext context) {
            logger.debug("Using Hybrid execution strategy");
            
            // 先用规划策略执行
            PlanningExecutionStrategy planningStrategy = new PlanningExecutionStrategy();
            FlowResult result = planningStrategy.execute(flow, context);
            
            // 如果规划执行失败，切换到 ReAct 模式重试
            if (!result.isSuccess()) {
                logger.debug("Planning execution failed, switching to ReAct mode");
                context.setVariable("retryWithReAct", true);
                ReActExecutionStrategy reactStrategy = new ReActExecutionStrategy();
                return reactStrategy.execute(flow, context);
            }
            
            return result;
        }
    }
}
