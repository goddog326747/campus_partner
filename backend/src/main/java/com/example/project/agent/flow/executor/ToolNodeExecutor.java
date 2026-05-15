package com.example.project.agent.flow.executor;

import com.example.project.agent.flow.FlowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * 工具管理器
 * <p>
 * 负责管理所有可用工具的注册和查询，<b>不再负责节点执行</b>。
 * 在新架构中，工具由 LLM 节点内部自主调用，不需要定义独立的 TOOL 节点。
 * <p>
 * 核心职责：
 * <ol>
 *   <li>注册工具（Lambda、Spring Bean 方法等）</li>
 *   <li>提供工具元数据（名称、描述）供 LLM 决策使用</li>
 *   <li>执行工具调用</li>
 * </ol>
 * </p>
 *
 * @author system
 * @since 1.0
 */
public class ToolNodeExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ToolNodeExecutor.class);

    private final ToolRegistry toolRegistry;

    public ToolNodeExecutor() {
        this.toolRegistry = new ToolRegistry();
    }

    public ToolNodeExecutor(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    /**
     * 注册工具
     *
     * @param name 工具名称
     * @param tool 工具函数
     */
    public void registerTool(String name, Function<FlowContext, Object> tool) {
        toolRegistry.register(name, tool);
    }

    /**
     * 获取工具注册表
     *
     * @return 工具注册表实例
     */
    public ToolRegistry getToolRegistry() {
        return toolRegistry;
    }

    /**
     * 根据工具名称获取工具描述（用于构建 ToolSpecification）
     *
     * @param toolName 工具名称
     * @return 工具描述
     */
    public String getToolDescription(String toolName) {
        return "Tool: " + toolName;
    }

    /**
     * 工具注册表
     * <p>
     * 管理所有可用工具的注册、查询和发现。
     * </p>
     */
    public static class ToolRegistry {

        private final Map<String, Function<FlowContext, Object>> tools = new HashMap<>();

        public void register(String name, Function<FlowContext, Object> tool) {
            tools.put(name, tool);
            logger.info("Tool registered: {}", name);
        }

        public Function<FlowContext, Object> getTool(String name) {
            return tools.get(name);
        }

        public boolean hasTool(String name) {
            return tools.containsKey(name);
        }

        public Set<String> getToolNames() {
            return Collections.unmodifiableSet(tools.keySet());
        }

        /**
         * 从对象中自动注册带有 @Tool 注解的方法
         *
         * @param obj 包含 @Tool 注解方法的对象
         */
        public void registerFromObject(Object obj) {
            Class<?> clazz = obj.getClass();
            for (Method method : clazz.getDeclaredMethods()) {
                dev.langchain4j.agent.tool.Tool toolAnnotation =
                        method.getAnnotation(dev.langchain4j.agent.tool.Tool.class);
                if (toolAnnotation != null) {
                    String toolName = method.getName();
                    register(toolName, ctx -> {
                        try {
                            if (method.getParameterCount() == 0) {
                                return method.invoke(obj);
                            } else {
                                Object[] args = new Object[method.getParameterCount()];
                                Class<?>[] paramTypes = method.getParameterTypes();
                                for (int i = 0; i < paramTypes.length; i++) {
                                    if (paramTypes[i] == FlowContext.class) {
                                        args[i] = ctx;
                                    } else if (paramTypes[i] == String.class) {
                                        args[i] = ctx.getInput("param" + i);
                                    } else if (paramTypes[i] == Long.class || paramTypes[i] == long.class) {
                                        Object val = ctx.getInput("param" + i);
                                        args[i] = val != null ? Long.valueOf(val.toString()) : 0L;
                                    } else {
                                        args[i] = ctx.getInput("param" + i);
                                    }
                                }
                                return method.invoke(obj, args);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Tool execution failed: " + toolName, e);
                        }
                    });
                }
            }
        }
    }
}
