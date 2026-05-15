package com.example.project.agent.flow.enums;

/**
 * 节点类型枚举
 * <p>
 * 定义流程图中各种节点的类型，每种类型对应不同的执行逻辑。
 * </p>
 *
 * @author system
 * @since 1.0
 */
public enum NodeType {

    /**
     * 起始节点
     * <p>流程的入口点，标识流程开始</p>
     */
    START,

    /**
     * AI 模型节点
     * <p>调用大语言模型生成内容，支持提示词模板和自主工具调用</p>
     */
    LLM,

    /**
     * 条件判断节点
     * <p>根据条件表达式决定流程走向，实现分支逻辑</p>
     */
    CONDITION,

    /**
     * 循环节点
     * <p>支持重复执行子流程，直到满足退出条件</p>
     */
    LOOP,

    /**
     * 结束节点
     * <p>流程的出口点，标识流程正常结束</p>
     */
    END
}
