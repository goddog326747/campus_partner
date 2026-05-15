package com.example.project.agent.flow.dispatch;

import com.example.project.agent.flow.AgentFlow;
import com.example.project.agent.flow.FlowContext;
import com.example.project.agent.flow.FlowEngine;
import com.example.project.agent.flow.dto.FlowResult;
import com.example.project.agent.flow.factory.AgentFlowFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowDispatchService {

    private final AgentFlowFactory flowFactory;
    private final FlowEngine flowEngine;
    private final Map<String, FlowContext> executionHistory = new ConcurrentHashMap<>();

    public FlowResult dispatch(String flowName, Map<String, Object> input) {
        log.info("Dispatching flow: flowName={}", flowName);
        AgentFlow flow = flowFactory.getFlow(flowName);
        FlowContext context = new FlowContext(flow.getFlowId(), input);
        FlowResult result = flowEngine.execute(flow, context);
        executionHistory.put(result.getExecutionId(), result.getContext());
        return result;
    }

    public FlowResult dispatch(String flowName, Map<String, Object> input, long timeoutMs) {
        log.info("Dispatching flow with timeout: flowName={}, timeout={}ms", flowName, timeoutMs);
        AgentFlow flow = flowFactory.getFlow(flowName);
        FlowContext context = new FlowContext(flow.getFlowId(), input);
        FlowResult result = flowEngine.execute(flow, context, timeoutMs);
        executionHistory.put(result.getExecutionId(), result.getContext());
        return result;
    }

    public FlowResult dispatchChat(String message, String conversationId, String mode) {
        log.info("Dispatching chat: mode={}, conversationId={}", mode, conversationId);

        Map<String, Object> input = new java.util.HashMap<>();
        input.put("message", message);
        input.put("conversationId", conversationId);

        String flowName = "REACT".equalsIgnoreCase(mode) ? "react-chat" : "simple-chat";
        return dispatch(flowName, input);
    }

    public CompletableFuture<FlowResult> dispatchAsync(String flowName, Map<String, Object> input) {
        return CompletableFuture.supplyAsync(() -> dispatch(flowName, input));
    }

    public FlowContext getExecutionHistory(String executionId) {
        return executionHistory.get(executionId);
    }
}
