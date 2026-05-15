package com.example.project.agent.flow.strategy;

import com.example.project.agent.flow.AgentFlow;
import com.example.project.agent.flow.FlowContext;
import com.example.project.agent.flow.dto.FlowResult;

/**
 * 流程执行策略接口
 * <p>
 * 定义流程的执行方式，不同策略实现不同的执行逻辑
 * </p>
 *
 * @author system
 * @since 1.0
 */
public interface ExecutionStrategy {

    /**
     * 执行流程
     *
     * @param flow    流程定义
     * @param context 流程上下文
     * @return 执行结果
     */
    FlowResult execute(AgentFlow flow, FlowContext context);
}
