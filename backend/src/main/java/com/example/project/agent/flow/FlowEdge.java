package com.example.project.agent.flow;

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
public class FlowEdge {
    
    private final String fromNodeId;
    private final String toNodeId;
    private final Function<FlowContext, Boolean> condition;
    private final EdgeType type;
    private final String label;
    
    public FlowEdge(String fromNodeId, String toNodeId) {
        this(fromNodeId, toNodeId, null, EdgeType.SEQUENTIAL, null);
    }
    
    public FlowEdge(String fromNodeId, String toNodeId, Function<FlowContext, Boolean> condition) {
        this(fromNodeId, toNodeId, condition, EdgeType.CONDITIONAL, null);
    }
    
    public FlowEdge(String fromNodeId, String toNodeId, Function<FlowContext, Boolean> condition, 
                    EdgeType type, String label) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.condition = condition;
        this.type = type;
        this.label = label;
    }
    
    /**
     * 判断是否可以通过此边流转
     */
    public boolean canTraverse(FlowContext context) {
        if (condition == null) {
            return true;
        }
        return condition.apply(context);
    }
    
    // ============ Getters ============
    
    public String getFromNodeId() { return fromNodeId; }
    public String getToNodeId() { return toNodeId; }
    public EdgeType getType() { return type; }
    public String getLabel() { return label; }
    
    /**
     * 创建顺序边
     */
    public static FlowEdge sequential(String from, String to) {
        return new FlowEdge(from, to, null, EdgeType.SEQUENTIAL, "sequential");
    }
    
    /**
     * 创建条件边
     */
    public static FlowEdge conditional(String from, String to, Function<FlowContext, Boolean> condition, String label) {
        return new FlowEdge(from, to, condition, EdgeType.CONDITIONAL, label);
    }
    
    /**
     * 创建成功边（前一个节点执行成功时）
     */
    public static FlowEdge onSuccess(String from, String to) {
        return new FlowEdge(from, to, 
            ctx -> ctx.getLastResult() != null && ctx.getLastResult().isSuccess(),
            EdgeType.ON_SUCCESS, "onSuccess");
    }
    
    /**
     * 创建失败边（前一个节点执行失败时）
     */
    public static FlowEdge onFailure(String from, String to) {
        return new FlowEdge(from, to, 
            ctx -> ctx.getLastResult() != null && !ctx.getLastResult().isSuccess(),
            EdgeType.ON_FAILURE, "onFailure");
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
