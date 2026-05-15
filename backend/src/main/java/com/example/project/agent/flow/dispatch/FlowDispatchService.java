package com.example.project.agent.flow.dispatch;

import com.example.project.agent.flow.AgentFlow;
import com.example.project.agent.flow.FlowContext;
import com.example.project.agent.flow.FlowEngine;
import com.example.project.agent.flow.dto.FlowResult;
import com.example.project.agent.flow.enums.FlowMode;
import com.example.project.agent.flow.factory.AgentFlowFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程调度服务
 * <p>
 * 负责接收外部请求并调度到对应的流程执行，是 AgentFlow 的入口层。
 * 提供同步、异步、带超时和聊天等多种调度方式，
 * 同时维护执行历史记录供查询。
 * </p>
 * <p>
 * <b>设计优化</b>：FlowEngine 是 Spring 单例，无状态可复用。
 * 每次请求只创建新的 FlowContext，避免重复创建引擎实例。
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowDispatchService {

    /** 流程工厂 */
    private final AgentFlowFactory flowFactory;
    /** 流程执行引擎（Spring 单例，无状态可复用） */
    private final FlowEngine flowEngine;
    /** 执行历史记录，key 为 executionId */
    private final Map<String, FlowContext> executionHistory = new ConcurrentHashMap<>();

    /**
     * 同步调度流程
     *
     * @param flowName 流程名称
     * @param input    输入参数
     * @return 流程执行结果
     */
    public FlowResult dispatch(String flowName, Map<String, Object> input) {
        log.info("Dispatching flow: flowName={}", flowName);
        AgentFlow flow = flowFactory.getFlow(flowName);
        FlowContext context = new FlowContext(flow.getFlowId(), input);
        FlowResult result = flowEngine.execute(flow, context);
        executionHistory.put(result.getExecutionId(), result.getContext());
        return result;
    }

    /**
     * 同步调度流程（带超时）
     *
     * @param flowName  流程名称
     * @param input     输入参数
     * @param timeoutMs 超时时间（毫秒）
     * @return 流程执行结果
     */
    public FlowResult dispatch(String flowName, Map<String, Object> input, long timeoutMs) {
        log.info("Dispatching flow with timeout: flowName={}, timeout={}ms", flowName, timeoutMs);
        AgentFlow flow = flowFactory.getFlow(flowName);
        FlowContext context = new FlowContext(flow.getFlowId(), input);
        FlowResult result = flowEngine.execute(flow, context, timeoutMs);
        executionHistory.put(result.getExecutionId(), result.getContext());
        return result;
    }

    /**
     * 调度聊天请求
     *
     * @param message        用户消息
     * @param conversationId 会话 ID
     * @param mode           执行模式（REACT 或 PLANNING）
     * @return 流程执行结果
     */
    public FlowResult dispatchChat(String message, String conversationId, String mode) {
        log.info("Dispatching chat: mode={}, conversationId={}", mode, conversationId);

        Map<String, Object> input = new java.util.HashMap<>();
        input.put("message", message);
        input.put("conversationId", conversationId);

        FlowMode flowMode = "REACT".equalsIgnoreCase(mode) ? FlowMode.REACT : FlowMode.PLANNING;
        String flowName = flowMode == FlowMode.REACT ? "react-chat" : "simple-chat";

        return dispatch(flowName, input);
    }

    /**
     * 异步调度流程
     *
     * @param flowName 流程名称
     * @param input    输入参数
     * @return 异步流程执行结果
     */
    public CompletableFuture<FlowResult> dispatchAsync(String flowName, Map<String, Object> input) {
        return CompletableFuture.supplyAsync(() -> dispatch(flowName, input));
    }

    /**
     * 获取执行历史记录
     *
     * @param executionId 执行 ID
     * @return 流程上下文，不存在则返回 null
     */
    public FlowContext getExecutionHistory(String executionId) {
        return executionHistory.get(executionId);
    }
}
