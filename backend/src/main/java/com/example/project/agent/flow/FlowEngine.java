package com.example.project.agent.flow;

import com.example.project.agent.flow.dto.FlowResult;
import com.example.project.agent.flow.dto.FlowNodeExecutionResult;
import com.example.project.agent.flow.enums.FlowStatus;
import com.example.project.agent.flow.enums.NodeType;
import com.example.project.agent.flow.executor.NodeExecutor;
import com.example.project.agent.flow.executor.NodeExecutorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;

@Component
public class FlowEngine {

    private static final Logger logger = LoggerFactory.getLogger(FlowEngine.class);
    private static final long DEFAULT_TIMEOUT_MS = 120_000L;
    private static final int MAX_TOTAL_ITERATIONS = 50;

    private final NodeExecutorRegistry executorRegistry;

    public FlowEngine(NodeExecutorRegistry executorRegistry) {
        this.executorRegistry = executorRegistry;
    }

    public FlowResult execute(AgentFlow flow, FlowContext context) {
        return execute(flow, context, DEFAULT_TIMEOUT_MS);
    }

    public FlowResult execute(AgentFlow flow, FlowContext context, long timeoutMs) {
        logger.info("Starting flow execution: flowId={}, executionId={}",
                flow.getFlowId(), context.getExecutionId());

        FlowValidator.validate(flow);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<FlowResult> future = executor.submit(() -> doExecute(flow, context));
            return awaitResult(future, flow, context, timeoutMs);
        } finally {
            executor.shutdownNow();
        }
    }

    private FlowResult doExecute(AgentFlow flow, FlowContext context) {
        try {
            FlowNode currentNode = findStartNode(flow);
            int totalIterations = 0;
            Object lastContentOutput = null;

            while (currentNode != null && totalIterations < MAX_TOTAL_ITERATIONS) {
                totalIterations++;

                NodeExecutor executor = executorRegistry.resolveExecutor(currentNode);
                FlowNodeExecutionResult result = executor.execute(currentNode, context);
                context.recordNodeResult(result);

                logger.debug("Node executed: nodeId={}, success={}, time={}ms",
                        currentNode.getNodeId(), result.isSuccess(), result.getExecutionTimeMs());

                if (!result.isSuccess()) {
                    context.complete(FlowStatus.FAILED);
                    return FlowResult.failure(flow.getFlowId(), context.getExecutionId(),
                            result.getError(), context);
                }

                if (currentNode.getType() == NodeType.LLM) {
                    lastContentOutput = result.getOutput();
                }

                if (currentNode.getType() == NodeType.END) {
                    break;
                }

                currentNode = resolveNextNode(flow, currentNode, context, result);
            }

            if (totalIterations >= MAX_TOTAL_ITERATIONS) {
                logger.warn("Flow reached max iterations: flowId={}, max={}", flow.getFlowId(), MAX_TOTAL_ITERATIONS);
            }

            Object finalOutput = lastContentOutput != null ? lastContentOutput : context.getLastOutput();
            context.complete(FlowStatus.COMPLETED);
            return FlowResult.success(flow.getFlowId(), context.getExecutionId(),
                    finalOutput, context);

        } catch (Exception e) {
            logger.error("Flow execution failed: flowId={}, error={}", flow.getFlowId(), e.getMessage(), e);
            context.complete(FlowStatus.FAILED);
            return FlowResult.failure(flow.getFlowId(), context.getExecutionId(), e.getMessage(), context);
        }
    }

    private FlowNode resolveNextNode(AgentFlow flow, FlowNode currentNode,
                                      FlowContext context, FlowNodeExecutionResult result) {
        if (currentNode.getType() == NodeType.LOOP) {
            return resolveLoopNext(flow, currentNode, context, result);
        }

        List<FlowEdge> outgoingEdges = flow.getOutgoingEdges(currentNode.getNodeId());
        for (FlowEdge edge : outgoingEdges) {
            if (edge.canTraverse(context)) {
                return flow.getNode(edge.getToNodeId());
            }
        }

        return null;
    }

    private FlowNode resolveLoopNext(AgentFlow flow, FlowNode loopNode,
                                      FlowContext context, FlowNodeExecutionResult result) {
        String loopCountKey = "_loopCount_" + loopNode.getNodeId();
        int iteration = context.getVariable(loopCountKey, 0);
        int maxIterations = loopNode.getConfig("maxIterations", 3);

        Boolean loopContinue = result.getMetadata() != null
                ? (Boolean) result.getMetadata().get("loopContinue")
                : null;

        if (Boolean.TRUE.equals(loopContinue) && iteration < maxIterations) {
            context.setVariable(loopCountKey, iteration + 1);
            String loopTarget = loopNode.getConfig("loopTarget");
            logger.debug("Loop continuing: node={}, iteration={}/{}", loopNode.getNodeId(), iteration + 1, maxIterations);
            return flow.getNode(loopTarget);
        }

        context.setVariable(loopCountKey, 0);
        logger.debug("Loop ended: node={}", loopNode.getNodeId());

        List<FlowEdge> outgoingEdges = flow.getOutgoingEdges(loopNode.getNodeId());
        for (FlowEdge edge : outgoingEdges) {
            if (edge.canTraverse(context)) {
                return flow.getNode(edge.getToNodeId());
            }
        }

        return null;
    }

    private FlowNode findStartNode(AgentFlow flow) {
        List<FlowNode> startNodes = flow.getStartNodes();
        if (startNodes.isEmpty()) {
            throw new IllegalStateException("No start node found in flow: " + flow.getFlowId());
        }
        return startNodes.get(0);
    }

    private FlowResult awaitResult(Future<FlowResult> future, AgentFlow flow,
                                    FlowContext context, long timeoutMs) {
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            logger.error("Flow execution timed out: flowId={}, timeout={}ms", flow.getFlowId(), timeoutMs);
            context.complete(FlowStatus.TIMEOUT);
            return FlowResult.failure(flow.getFlowId(), context.getExecutionId(),
                    "Flow execution timed out after " + timeoutMs + "ms", context);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            context.complete(FlowStatus.CANCELLED);
            return FlowResult.failure(flow.getFlowId(), context.getExecutionId(),
                    "Flow execution was interrupted", context);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            logger.error("Flow execution failed: flowId={}, error={}", flow.getFlowId(), cause.getMessage(), cause);
            context.complete(FlowStatus.FAILED);
            return FlowResult.failure(flow.getFlowId(), context.getExecutionId(), cause.getMessage(), context);
        }
    }
}
