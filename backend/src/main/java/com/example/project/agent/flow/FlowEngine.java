package com.example.project.agent.flow;

import com.example.project.agent.flow.dto.FlowResult;
import com.example.project.agent.flow.enums.FlowMode;
import com.example.project.agent.flow.enums.FlowStatus;
import com.example.project.agent.flow.strategy.ExecutionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 流程执行引擎，负责驱动 AgentFlow 的执行。
 * <p>
 * <b>设计原则</b>：FlowEngine 是<b>无状态服务</b>（Spring 单例），可以复用。
 * 每次执行时传入 {@link AgentFlow} 和 {@link FlowContext}，避免重复创建引擎实例。
 * <p>
 * <b>策略注入</b>：通过 Spring 注入所有 ExecutionStrategy，按 FlowMode 自动映射。
 * <p>
 * 引擎支持超时控制、异常处理、线程隔离以及多种执行模式的自动适配。
 *
 * @author example
 * @since 1.0.0
 */
@Component
public class FlowEngine {

    private static final Logger logger = LoggerFactory.getLogger(FlowEngine.class);
    private static final long DEFAULT_TIMEOUT_MS = 120_000L;

    private final Map<String, ExecutionStrategy> strategies;

    /**
     * 构造方法，通过 Spring 注入所有策略。
     * <p>
     * Spring 会自动收集所有 {@link ExecutionStrategy} 的实现类，
     * 按 Bean 名称映射到 Map 中。
     *
     * @param strategies 策略映射表，key 为 Bean 名称（如 "planningStrategy"、"reactStrategy"）
     */
    public FlowEngine(Map<String, ExecutionStrategy> strategies) {
        this.strategies = strategies;
        logger.info("Loaded {} execution strategies: {}", strategies.size(), strategies.keySet());
    }

    /**
     * 执行流程（默认超时）。
     * <p>
     * 在独立线程中执行策略，支持超时、中断和异常处理。
     *
     * @param flow    流程定义（每次请求不同）
     * @param context 执行上下文（每次请求不同）
     * @return 流程执行结果
     */
    public FlowResult execute(AgentFlow flow, FlowContext context) {
        return execute(flow, context, DEFAULT_TIMEOUT_MS);
    }

    /**
     * 执行流程（指定超时）。
     * <p>
     * 在独立线程中执行策略，支持超时、中断和异常处理。
     *
     * @param flow      流程定义（每次请求不同）
     * @param context   执行上下文（每次请求不同）
     * @param timeoutMs 超时时间（毫秒）
     * @return 流程执行结果
     */
    public FlowResult execute(AgentFlow flow, FlowContext context, long timeoutMs) {
        logger.info("Starting flow execution: flowId={}, mode={}, executionId={}",
                flow.getFlowId(), flow.getMode(), context.getExecutionId());

        FlowValidator.validate(flow);
        ExecutionStrategy strategy = resolveStrategy(flow.getMode());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<FlowResult> future = executor.submit(() -> {
                try {
                    return strategy.execute(flow, context);
                } catch (Exception e) {
                    logger.error("Flow execution failed: flowId={}, error={}", flow.getFlowId(), e.getMessage(), e);
                    context.complete(FlowStatus.FAILED);
                    return FlowResult.failure(flow.getFlowId(), context.getExecutionId(), e.getMessage(), context);
                }
            });

            return awaitResult(future, flow, context, timeoutMs);
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * 根据执行模式解析对应的策略。
     *
     * @param mode 执行模式
     * @return 对应的执行策略
     * @throws IllegalStateException 找不到对应策略时抛出
     */
    private ExecutionStrategy resolveStrategy(FlowMode mode) {
        String strategyName = switch (mode) {
            case PLANNING -> "planningStrategy";
            case REACT -> "reactStrategy";
            case HYBRID -> "hybridStrategy";
        };

        ExecutionStrategy strategy = strategies.get(strategyName);
        if (strategy == null) {
            throw new IllegalStateException("No strategy found for mode: " + mode +
                    ", available strategies: " + strategies.keySet());
        }
        return strategy;
    }

    /**
     * 等待异步执行结果，处理超时、中断和执行异常。
     */
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
