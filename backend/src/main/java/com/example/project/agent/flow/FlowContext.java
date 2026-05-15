package com.example.project.agent.flow;

import com.example.project.agent.flow.dto.FlowNodeExecutionResult;
import com.example.project.agent.flow.enums.FlowStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程执行上下文，用于保存和管理流程执行过程中的状态、变量、结果等信息。
 * <p>
 * 每个流程执行实例都会创建独立的 FlowContext，包含执行历史、节点结果、事件监听等，
 * 支持变量存取、节点结果查询、执行时间统计以及重试上下文创建等功能。
 *
 * @author example
 * @since 1.0.0
 */
@Getter
public class FlowContext {

    /** 流程定义唯一标识 */
    private final String flowId;
    /** 本次执行唯一标识 */
    private final String executionId;
    /** 初始输入参数 */
    private final Map<String, Object> initialInput;
    /** 各节点执行结果，key 为节点 ID */
    private final Map<String, FlowNodeExecutionResult> nodeResults;
    /** 流程变量存储区 */
    private final Map<String, Object> variables;
    /** 执行步骤历史记录 */
    private final List<ExecutionStep> executionHistory;
    /** 流程开始时间 */
    private final LocalDateTime startTime;
    /** 流程结束时间 */
    private LocalDateTime endTime;
    /** 当前流程状态 */
    private FlowStatus status;

    /** 事件监听器列表 */
    private final List<FlowEventListener> eventListeners;

    /**
     * 构造方法，创建新的流程执行上下文。
     *
     * @param flowId       流程定义唯一标识
     * @param initialInput 初始输入参数
     */
    public FlowContext(String flowId, Map<String, Object> initialInput) {
        this.flowId = flowId;
        this.executionId = UUID.randomUUID().toString();
        this.initialInput = new ConcurrentHashMap<>();
        if (initialInput != null) {
            initialInput.forEach((key, value) -> {
                if (value != null) {
                    this.initialInput.put(key, value);
                }
            });
        }
        this.nodeResults = new ConcurrentHashMap<>();
        this.variables = new ConcurrentHashMap<>();
        this.executionHistory = Collections.synchronizedList(new ArrayList<>());
        this.startTime = LocalDateTime.now();
        this.status = FlowStatus.RUNNING;
        this.eventListeners = new ArrayList<>();
    }

    /**
     * 添加事件监听器。
     *
     * @param listener 事件监听器实例
     */
    public void addEventListener(FlowEventListener listener) {
        eventListeners.add(listener);
    }

    /**
     * 记录节点执行结果，并更新执行历史和触发事件。
     *
     * @param result 节点执行结果
     */
    public void recordNodeResult(FlowNodeExecutionResult result) {
        nodeResults.put(result.getNodeId(), result);
        executionHistory.add(new ExecutionStep(
                result.getNodeId(),
                result.getNodeName(),
                result.isSuccess(),
                LocalDateTime.now()
        ));
        emitEvent(new FlowEvent(FlowEvent.Type.NODE_COMPLETED, result.getNodeId(), result));
    }

    /**
     * 设置流程变量。
     *
     * @param key   变量名
     * @param value 变量值
     */
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }

    /**
     * 获取流程变量。
     *
     * @param key 变量名
     * @param <T> 变量类型
     * @return 变量值，若不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String key) {
        return (T) variables.get(key);
    }

    /**
     * 获取流程变量，若不存在则返回默认值。
     *
     * @param key          变量名
     * @param defaultValue 默认值
     * @param <T>          变量类型
     * @return 变量值或默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String key, T defaultValue) {
        return (T) variables.getOrDefault(key, defaultValue);
    }

    /**
     * 获取指定节点的执行结果。
     *
     * @param nodeId 节点 ID
     * @return 节点执行结果的可选对象
     */
    public Optional<FlowNodeExecutionResult> getNodeResult(String nodeId) {
        return Optional.ofNullable(nodeResults.get(nodeId));
    }

    /**
     * 获取指定节点的成功输出。
     *
     * @param nodeId 节点 ID
     * @param <T>    输出类型
     * @return 节点输出，若节点未成功执行则返回 null
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
     * 获取最近一次执行的节点结果。
     *
     * @return 最近节点执行结果，若无历史则返回 null
     */
    public FlowNodeExecutionResult getLastResult() {
        if (executionHistory.isEmpty()) {
            return null;
        }
        ExecutionStep lastStep = executionHistory.get(executionHistory.size() - 1);
        return nodeResults.get(lastStep.getNodeId());
    }

    /**
     * 获取最近一次成功执行的节点输出。
     *
     * @param <T> 输出类型
     * @return 最近成功节点的输出，若无则返回 null
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
     * 标记流程执行完成，设置最终状态和结束时间。
     *
     * @param status 最终状态
     */
    public void complete(FlowStatus status) {
        this.status = status;
        this.endTime = LocalDateTime.now();
        emitEvent(new FlowEvent(FlowEvent.Type.FLOW_COMPLETED, flowId, status));
    }

    /**
     * 获取初始输入参数。
     *
     * @param key 参数键
     * @param <T> 参数类型
     * @return 参数值
     */
    @SuppressWarnings("unchecked")
    public <T> T getInput(String key) {
        return (T) initialInput.get(key);
    }

    /**
     * 获取已执行时间（毫秒）。
     * <p>
     * 若流程已结束则返回总耗时，否则返回从开始到当前的耗时。
     *
     * @return 执行时间（毫秒）
     */
    public long getExecutionTimeMs() {
        if (endTime == null) {
            return java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
        }
        return java.time.Duration.between(startTime, endTime).toMillis();
    }

    /**
     * 创建用于重试的新上下文，继承初始输入并附加重试标记。
     *
     * @return 新的重试上下文
     */
    public FlowContext createRetryContext() {
        FlowContext retryContext = new FlowContext(flowId, new HashMap<>(initialInput));
        retryContext.setVariable("retryFrom", executionId);
        retryContext.setVariable("retryReason", "previous_execution_failed");
        return retryContext;
    }

    /**
     * 触发事件通知所有监听器，监听器异常不影响主流程。
     *
     * @param event 流程事件
     */
    private void emitEvent(FlowEvent event) {
        for (FlowEventListener listener : eventListeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                // 不让事件监听器异常影响主流程
            }
        }
    }

    /**
     * 执行步骤记录，保存单个节点的执行摘要信息。
     */
    @Getter
    @AllArgsConstructor
    public static class ExecutionStep {
        /** 节点 ID */
        private final String nodeId;
        /** 节点名称 */
        private final String nodeName;
        /** 是否执行成功 */
        private final boolean success;
        /** 执行时间戳 */
        private final LocalDateTime timestamp;
    }

    /**
     * 流程事件监听器接口，用于接收流程执行过程中的事件通知。
     */
    public interface FlowEventListener {
        /**
         * 当事件触发时的回调方法。
         *
         * @param event 流程事件
         */
        void onEvent(FlowEvent event);
    }

    /**
     * 流程事件，用于在流程执行过程中传递节点和流程级别的状态变更。
     */
    @Getter
    @AllArgsConstructor
    public static class FlowEvent {
        /** 事件类型 */
        private final Type type;
        /** 事件来源（节点 ID 或流程 ID） */
        private final String source;
        /** 事件附加数据 */
        private final Object data;

        /**
         * 事件类型枚举。
         */
        public enum Type {
            /** 节点开始执行 */
            NODE_STARTED,
            /** 节点执行完成 */
            NODE_COMPLETED,
            /** 流程执行完成 */
            FLOW_COMPLETED,
            /** 流程执行出错 */
            FLOW_ERROR
        }
    }
}
