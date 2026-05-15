package com.example.project.agent.flow.executor;

import com.example.project.agent.flow.FlowContext;
import com.example.project.agent.flow.FlowNode;
import com.example.project.agent.flow.dto.FlowNodeExecutionResult;

/**
 * 节点执行器接口
 * <p>
 * 每种节点类型对应一个执行器实现，负责具体的节点执行逻辑。
 * 执行器由 {@link NodeExecutorRegistry} 统一管理，由 {@link FlowEngine} 调度调用。
 * </p>
 *
 * @author system
 * @since 1.0
 */
public interface NodeExecutor {

    /**
     * 执行节点
     *
     * @param node    流程节点（纯配置）
     * @param context 流程上下文
     * @return 节点执行结果
     */
    FlowNodeExecutionResult execute(FlowNode node, FlowContext context);

    /**
     * 获取支持的节点类型
     *
     * @return 节点类型名称
     */
    String getSupportedNodeType();
}
