package com.example.project.agent.flow;

import com.example.project.agent.flow.dto.FlowNodeExecutionResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.function.Function;

/**
 * 流程边 - 连接节点的有向边
 * 
 * ============================================================
 *                    边的类型和作用
 * ============================================================
 * 
 * 【顺序边】
 * - 默认边类型
 * - 前一个节点成功后执行后一个节点
 * - condition = null 或返回 true
 * 
 * 【条件边】
 * - 根据条件决定是否流转
 * - condition 返回 true 时才流转
 * - 支持复杂逻辑判断
 * 
 * 【默认边】
 * - 当其他条件边都不满足时执行
 * - 用于条件分支的默认路径
 * 
 * 【错误边】
 * - 当前节点执行失败时执行
 * - 用于错误处理和恢复
 * 
 * ============================================================
 */
@Getter
@Builder
@AllArgsConstructor
public class FlowEdge {
    
    private final String fromNodeId;
    private final String toNodeId;
    private final Function<FlowContext, Boolean> condition;
    @Builder.Default
    private final EdgeType type = EdgeType.SEQUENTIAL;
    @Builder.Default
    private final String label = "";
    
    /**
     * 判断是否可以通过此边流转
     */
    public boolean canTraverse(FlowContext context) {
        if (condition == null) {
            return true;
        }
        return condition.apply(context);
    }
    
    /**
     * 创建顺序边
     */
    public static FlowEdge sequential(String from, String to) {
        return FlowEdge.builder()
                .fromNodeId(from)
                .toNodeId(to)
                .type(EdgeType.SEQUENTIAL)
                .label("sequential")
                .build();
    }
    
    /**
     * 创建条件边
     */
    public static FlowEdge conditional(String from, String to, Function<FlowContext, Boolean> condition, String label) {
        return FlowEdge.builder()
                .fromNodeId(from)
                .toNodeId(to)
                .condition(condition)
                .type(EdgeType.CONDITIONAL)
                .label(label)
                .build();
    }
    
    /**
     * 创建成功边（前一个节点执行成功时）
     */
    public static FlowEdge onSuccess(String from, String to) {
        return FlowEdge.builder()
                .fromNodeId(from)
                .toNodeId(to)
                .condition(ctx -> {
                    FlowNodeExecutionResult lastResult = ctx.getLastResult();
                    return lastResult != null && lastResult.isSuccess();
                })
                .type(EdgeType.ON_SUCCESS)
                .label("onSuccess")
                .build();
    }
    
    /**
     * 创建失败边（前一个节点执行失败时）
     */
    public static FlowEdge onFailure(String from, String to) {
        return FlowEdge.builder()
                .fromNodeId(from)
                .toNodeId(to)
                .condition(ctx -> {
                    FlowNodeExecutionResult lastResult = ctx.getLastResult();
                    return lastResult != null && !lastResult.isSuccess();
                })
                .type(EdgeType.ON_FAILURE)
                .label("onFailure")
                .build();
    }
    
    /**
     * 边类型枚举
     */
    public enum EdgeType {
        SEQUENTIAL,     // 顺序边
        CONDITIONAL,    // 条件边
        ON_SUCCESS,     // 成功边
        ON_FAILURE,     // 失败边
        DEFAULT         // 默认边
    }
}
