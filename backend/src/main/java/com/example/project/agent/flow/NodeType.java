package com.example.project.agent.flow;

/**
 * 节点类型枚举
 */
public enum NodeType {
    
    /**
     * 起始节点
     * 流程的入口点
     */
    START,
    
    /**
     * AI 模型节点
     * 调用大语言模型生成内容
     */
    LLM,
    
    /**
     * 工具调用节点
     * 调用外部工具或函数
     */
    TOOL,
    
    /**
     * 条件判断节点
     * 根据条件决定流程走向
     */
    CONDITION,
    
    /**
     * 循环节点
     * 支持重复执行
     */
    LOOP,
    
    /**
     * 并行节点
     * 同时执行多个子流程
     */
    PARALLEL,
    
    /**
     * 聚合节点
     * 合并多个并行分支的结果
     */
    AGGREGATE,
    
    /**
     * 记忆节点
     * 存储或检索对话记忆
     */
    MEMORY,
    
    /**
     * 结束节点
     * 流程的出口点
     */
    END
}
