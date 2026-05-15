package com.example.project.agent.flow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.function.Function;

/**
 * 流程边，表示流程节点之间的连接关系。
 * <p>
 * 每条边包含起始节点、目标节点、边类型（顺序或条件）以及可选的条件判断函数，
 * 用于控制流程的执行路径和流转逻辑。
 *
 * @author example
 * @since 1.0.0
 */
@Getter
@Builder
@AllArgsConstructor
public class FlowEdge {

    /** 起始节点 ID */
    private final String fromNodeId;
    /** 目标节点 ID */
    private final String toNodeId;
    /** 条件判断函数，用于条件边 */
    private final Function<FlowContext, Boolean> condition;
    /** 边类型，默认为顺序边 */
    @Builder.Default
    private final EdgeType type = EdgeType.SEQUENTIAL;
    /** 边标签，用于描述或标识 */
    @Builder.Default
    private final String label = "";

    /**
     * 判断当前边是否可以通过（遍历）。
     * <p>
     * 若无边条件则直接返回 true，否则执行条件函数判断。
     *
     * @param context 流程执行上下文
     * @return 是否可以通过
     */
    public boolean canTraverse(FlowContext context) {
        if (condition == null) {
            return true;
        }
        return condition.apply(context);
    }

    /**
     * 创建一条顺序边。
     *
     * @param from 起始节点 ID
     * @param to   目标节点 ID
     * @return 顺序边实例
     */
    public static FlowEdge sequential(String from, String to) {
        return FlowEdge.builder()
                .fromNodeId(from)
                .toNodeId(to)
                .type(EdgeType.SEQUENTIAL)
                .build();
    }

    /**
     * 创建一条条件边。
     *
     * @param from      起始节点 ID
     * @param to        目标节点 ID
     * @param condition 条件判断函数
     * @return 条件边实例
     */
    public static FlowEdge conditional(String from, String to, Function<FlowContext, Boolean> condition) {
        return FlowEdge.builder()
                .fromNodeId(from)
                .toNodeId(to)
                .condition(condition)
                .type(EdgeType.CONDITIONAL)
                .build();
    }

    /**
     * 边类型枚举。
     */
    public enum EdgeType {
        /** 顺序边，无条件直接流转 */
        SEQUENTIAL,
        /** 条件边，需满足条件才流转 */
        CONDITIONAL
    }
}
