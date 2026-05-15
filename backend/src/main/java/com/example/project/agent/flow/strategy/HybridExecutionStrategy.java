package com.example.project.agent.flow.strategy;

import com.example.project.agent.flow.AgentFlow;
import com.example.project.agent.flow.FlowContext;
import com.example.project.agent.flow.dto.FlowResult;
import com.example.project.agent.flow.enums.FlowStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 混合执行策略
 * <p>
 * 组合主策略和回退策略，当主策略执行失败时自动切换到回退策略。
 * 适用于需要高可用性保障的流程场景。
 * </p>
 *
 * @author system
 * @since 1.0
 */
public class HybridExecutionStrategy implements ExecutionStrategy {

    /** 日志记录器 */
    private static final Logger logger = LoggerFactory.getLogger(HybridExecutionStrategy.class);

    /** 主执行策略 */
    private final ExecutionStrategy primaryStrategy;
    /** 回退执行策略 */
    private final ExecutionStrategy fallbackStrategy;

    /**
     * 构造混合执行策略
     *
     * @param primaryStrategy  主执行策略
     * @param fallbackStrategy 回退执行策略
     */
    public HybridExecutionStrategy(ExecutionStrategy primaryStrategy, ExecutionStrategy fallbackStrategy) {
        this.primaryStrategy = primaryStrategy;
        this.fallbackStrategy = fallbackStrategy;
    }

    /**
     * 执行流程，主策略失败时自动回退
     *
     * @param flow    流程定义
     * @param context 流程上下文
     * @return 执行结果
     */
    @Override
    public FlowResult execute(AgentFlow flow, FlowContext context) {
        logger.debug("Using Hybrid execution strategy");

        FlowResult result = primaryStrategy.execute(flow, context);

        if (!result.isSuccess()) {
            logger.debug("Primary strategy failed, switching to fallback with fresh context");
            FlowContext retryContext = context.createRetryContext();
            FlowResult fallbackResult = fallbackStrategy.execute(flow, retryContext);
            fallbackResult.getMetadata().put("fallbackFromPrimary", true);
            fallbackResult.getMetadata().put("primaryError", result.getError());
            return fallbackResult;
        }

        return result;
    }
}
