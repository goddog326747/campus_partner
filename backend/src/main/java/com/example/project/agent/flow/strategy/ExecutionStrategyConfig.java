package com.example.project.agent.flow.strategy;

import com.example.project.agent.flow.executor.ToolNodeExecutor;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 执行策略配置类
 * <p>
 * 负责配置所有 ExecutionStrategy 的 Bean，包括：
 * <ul>
 *   <li>planningStrategy：规划执行策略</li>
 *   <li>reactStrategy：ReAct 执行策略</li>
 *   <li>hybridStrategy：混合执行策略（组合 Planning + ReAct）</li>
 * </ul>
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Configuration
public class ExecutionStrategyConfig {

    /**
     * 创建规划执行策略 Bean。
     */
    @Bean("planningStrategy")
    public ExecutionStrategy planningStrategy(ChatLanguageModel chatModel, ToolNodeExecutor toolExecutor) {
        return new PlanningExecutionStrategy(chatModel, toolExecutor);
    }

    /**
     * 创建 ReAct 执行策略 Bean。
     */
    @Bean("reactStrategy")
    public ExecutionStrategy reactStrategy(ChatLanguageModel chatModel, ToolNodeExecutor toolExecutor) {
        return new LLMReActStrategy(chatModel, toolExecutor);
    }

    /**
     * 创建混合执行策略 Bean。
     * <p>
     * HYBRID 模式组合 Planning 和 ReAct 策略，当 Planning 失败时自动回退到 ReAct。
     */
    @Bean("hybridStrategy")
    public ExecutionStrategy hybridStrategy(ChatLanguageModel chatModel, ToolNodeExecutor toolExecutor) {
        return new HybridExecutionStrategy(
                new PlanningExecutionStrategy(chatModel, toolExecutor),
                new LLMReActStrategy(chatModel, toolExecutor)
        );
    }
}
