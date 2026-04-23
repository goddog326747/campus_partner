package com.example.project.agent.flow;

import com.example.project.agent.flow.dto.FlowNodeExecutionResult;
import com.example.project.agent.flow.enums.FlowStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程上下文
 * 
 * ============================================================
 *                    上下文作用
 * ============================================================
 * 
 * FlowContext 是整个流程执行过程中的共享状态容器：
 * 
 * 1. 存储初始输入
 * 2. 存储每个节点的执行结果
 * 3. 存储中间变量
 * 4. 记录执行历史
 * 5. 支持节点间的数据传递
 * 
 * 数据访问方式：
 * - getInput(): 获取初始输入
 * - getNodeOutput(nodeId): 获取指定节点的输出
 * - getLastResult(): 获取上一个节点的执行结果
 * - getVariable(key): 获取变量
 * - setVariable(key, value): 设置变量
 * 
 * ============================================================
 */
@Getter
public class FlowContext {
    
    private final String flowId;
    private final String executionId;
    private final Map<String, Object> initialInput;
    private final Map<String, FlowNodeExecutionResult> nodeResults;
    private final Map<String, Object> variables;
    private final List<ExecutionStep> executionHistory;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private FlowStatus status;
    
    public FlowContext(String flowId, Map<String, Object> initialInput) {
        this.flowId = flowId;
        this.executionId = UUID.randomUUID().toString();
        this.initialInput = new ConcurrentHashMap<>(initialInput != null ? initialInput : new HashMap<>());
        this.nodeResults = new ConcurrentHashMap<>();
        this.variables = new ConcurrentHashMap<>();
        this.executionHistory = Collections.synchronizedList(new ArrayList<>());
        this.startTime = LocalDateTime.now();
        this.status = FlowStatus.RUNNING;
    }
    
    /**
     * 记录节点执行结果
     */
    public void recordNodeResult(FlowNodeExecutionResult result) {
        nodeResults.put(result.getNodeId(), result);
        executionHistory.add(new ExecutionStep(
            result.getNodeId(),
            result.getNodeName(),
            result.isSuccess(),
            LocalDateTime.now()
        ));
    }
    
    /**
     * 设置变量
     */
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }
    
    /**
     * 获取变量
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String key) {
        return (T) variables.get(key);
    }
    
    /**
     * 获取变量，带默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String key, T defaultValue) {
        return (T) variables.getOrDefault(key, defaultValue);
    }
    
    /**
     * 获取节点输出
     */
    @SuppressWarnings("unchecked")
    public <T> T getNodeOutput(String nodeId) {
        FlowNodeExecutionResult result = nodeResults.get(nodeId);
        if (result != null && result.isSuccess()) {
            return (T) result.getOutput();
        }
        return null;
    }
    
    /**
     * 获取上一个节点的执行结果
     */
    public FlowNodeExecutionResult getLastResult() {
        if (executionHistory.isEmpty()) {
            return null;
        }
        ExecutionStep lastStep = executionHistory.get(executionHistory.size() - 1);
        return nodeResults.get(lastStep.getNodeId());
    }
    
    /**
     * 获取上一个成功节点的输出
     */
    @SuppressWarnings("unchecked")
    public <T> T getLastOutput() {
        FlowNodeExecutionResult lastResult = getLastResult();
        if (lastResult != null && lastResult.isSuccess()) {
            return (T) lastResult.getOutput();
        }
        return null;
    }
    
    /**
     * 完成流程
     */
    public void complete(FlowStatus status) {
        this.status = status;
        this.endTime = LocalDateTime.now();
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getInput(String key) {
        return (T) initialInput.get(key);
    }
    
    /**
     * 获取执行耗时（毫秒）
     */
    public long getExecutionTimeMs() {
        if (endTime == null) {
            return java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
        }
        return java.time.Duration.between(startTime, endTime).toMillis();
    }
    
    /**
     * 执行步骤记录
     */
    @Getter
    @AllArgsConstructor
    public static class ExecutionStep {
        private final String nodeId;
        private final String nodeName;
        private final boolean success;
        private final LocalDateTime timestamp;
    }
}
