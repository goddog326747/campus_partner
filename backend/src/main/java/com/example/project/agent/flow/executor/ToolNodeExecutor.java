package com.example.project.agent.flow.executor;

import com.example.project.agent.flow.FlowContext;
import com.example.project.agent.flow.FlowNode;
import com.example.project.agent.flow.dto.FlowNodeExecutionResult;
import com.example.project.agent.flow.enums.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * 工具节点执行器
 * 
 * ============================================================
 *                    工具节点执行原理
 * ============================================================
 * 
 * 这个执行器负责调用外部工具/函数：
 * 
 * 1. 根据工具名称查找对应的工具方法
 * 2. 解析参数（从上下文中获取）
 * 3. 调用工具方法
 * 4. 返回结果
 * 
 * 工具注册方式：
 * - 通过 ToolRegistry 注册
 * - 支持 Lambda 函数
 * - 支持 Spring Bean 方法
 * 
 * ============================================================
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
     * 执行工具节点
     */
    public FlowNodeExecutionResult execute(FlowNode node, FlowContext context) {
        try {
            long startTime = System.currentTimeMillis();
            
            // 1. 获取工具名称
            String toolName = node.getConfig("toolName");
            if (toolName == null || toolName.isEmpty()) {
                throw new IllegalArgumentException("Tool name is required");
            }
            
            // 2. 查找工具
            Function<FlowContext, Object> tool = toolRegistry.getTool(toolName);
            if (tool == null) {
                throw new IllegalArgumentException("Tool not found: " + toolName);
            }
            
            logger.debug("Tool Node executing: node={}, tool={}", node.getName(), toolName);
            
            // 3. 执行工具
            Object result = tool.apply(context);
            
            long endTime = System.currentTimeMillis();
            
            logger.debug("Tool Node completed: node={}, tool={}, time={}ms", 
                    node.getName(), toolName, endTime - startTime);
            
            return FlowNodeExecutionResult.builder()
                    .nodeId(node.getNodeId())
                    .nodeName(node.getName())
                    .success(true)
                    .output(result)
                    .executionTimeMs(endTime - startTime)
                    .metadata("toolName", toolName)
                    .build();
                    
        } catch (Exception e) {
            logger.error("Tool Node execution failed: node={}, error={}", node.getName(), e.getMessage(), e);
            return FlowNodeExecutionResult.builder()
                    .nodeId(node.getNodeId())
                    .nodeName(node.getName())
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }
    
    /**
     * 注册工具
     */
    public void registerTool(String name, Function<FlowContext, Object> tool) {
        toolRegistry.register(name, tool);
    }
    
    /**
     * 获取工具注册表
     */
    public ToolRegistry getToolRegistry() {
        return toolRegistry;
    }
    
    /**
     * 创建工具节点
     */
    public static FlowNode createNode(String nodeId, String name, String toolName, String description) {
        return FlowNode.builder()
                .nodeId(nodeId)
                .name(name)
                .type(NodeType.TOOL)
                .config("toolName", toolName)
                .config("description", description)
                .build();
    }
    
    /**
     * 工具注册表
     */
    public static class ToolRegistry {
        
        private final Map<String, Function<FlowContext, Object>> tools = new HashMap<>();
        
        /**
         * 注册工具
         */
        public void register(String name, Function<FlowContext, Object> tool) {
            tools.put(name, tool);
            logger.info("Tool registered: {}", name);
        }
        
        /**
         * 获取工具
         */
        public Function<FlowContext, Object> getTool(String name) {
            return tools.get(name);
        }
        
        /**
         * 检查工具是否存在
         */
        public boolean hasTool(String name) {
            return tools.containsKey(name);
        }
        
        /**
         * 获取所有工具名称
         */
        public Set<String> getToolNames() {
            return Collections.unmodifiableSet(tools.keySet());
        }
        
        /**
         * 从对象中自动注册带有 @Tool 注解的方法
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
                            // 简化版：假设方法只有一个参数且类型匹配
                            if (method.getParameterCount() == 0) {
                                return method.invoke(obj);
                            } else {
                                // 从上下文中获取参数
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
