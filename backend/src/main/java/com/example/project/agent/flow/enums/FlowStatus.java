package com.example.project.agent.flow.enums;

/**
 * 流程状态枚举
 * <p>
 * 表示 AgentFlow 在整个生命周期中的各种状态。
 * </p>
 *
 * @author system
 * @since 1.0
 */
public enum FlowStatus {

    /**
     * 运行中
     * <p>流程正在执行中</p>
     */
    RUNNING,

    /**
     * 成功完成
     * <p>流程正常结束，所有节点执行成功</p>
     */
    COMPLETED,

    /**
     * 失败
     * <p>流程执行过程中发生错误，未能正常完成</p>
     */
    FAILED,

    /**
     * 已取消
     * <p>流程被外部主动取消</p>
     */
    CANCELLED,

    /**
     * 暂停
     * <p>流程被暂停，可以恢复继续执行</p>
     */
    PAUSED,

    /**
     * 超时
     * <p>流程执行超过设定的时间限制</p>
     */
    TIMEOUT
}
