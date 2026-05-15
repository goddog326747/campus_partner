package com.example.project.agent.flow.enums;

/**
 * Agent Flow 执行模式枚举
 * <p>
 * 定义流程执行的三种核心模式：
 * <ul>
 *   <li><b>PLANNING</b>：先规划所有步骤，再按顺序执行，适合确定性任务</li>
 *   <li><b>REACT</b>：边推理边执行，根据中间结果动态决定下一步，适合探索性任务</li>
 *   <li><b>HYBRID</b>：结合规划和 ReAct，在规划中支持动态调整</li>
 * </ul>
 * </p>
 *
 * @author system
 * @since 1.0
 */
public enum FlowMode {

    /**
     * 规划执行模式
     * <p>
     * 先规划，后执行。执行路径在开始前就已确定，每个节点的输入输出预先定义。
     * 适合确定性任务，如生成帖子、数据报表等需要严格控制的流程。
     * </p>
     */
    PLANNING,

    /**
     * ReAct 模式
     * <p>
     * ReAct = Reasoning (推理) + Acting (行动)
     * <p>
     * 边推理边执行，根据中间结果动态决定下一步，支持循环和回溯。
     * 适合探索性任务，如复杂问题解答、多轮对话等不确定性较高的任务。
     * 执行循环：Thought → Action → Observation → ... → Answer
     * </p>
     */
    REACT,

    /**
     * 混合模式
     * <p>
     * 结合规划和 ReAct 的优点，在规划中支持动态调整。
     * 当主策略失败时可自动切换到回退策略。
     * </p>
     */
    HYBRID
}
