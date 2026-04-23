package com.example.project.agent.flow;

/**
 * 流程状态枚举
 */
public enum FlowStatus {
    
    /**
     * 运行中
     */
    RUNNING,
    
    /**
     * 成功完成
     */
    COMPLETED,
    
    /**
     * 失败
     */
    FAILED,
    
    /**
     * 已取消
     */
    CANCELLED,
    
    /**
     * 暂停
     */
    PAUSED,
    
    /**
     * 超时
     */
    TIMEOUT
}
