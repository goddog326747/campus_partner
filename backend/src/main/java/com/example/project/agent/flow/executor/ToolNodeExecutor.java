package com.example.project.agent.flow.executor;

import com.example.project.agent.flow.FlowContext;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

@Component
public class ToolNodeExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ToolNodeExecutor.class);

    private final List<Object> toolProviders = new ArrayList<>();
    private final Map<String, ToolExecutor> toolExecutorMap = new HashMap<>();

    public ToolNodeExecutor(ApplicationContext applicationContext) {
        autoDiscoverTools(applicationContext);
    }

    private void autoDiscoverTools(ApplicationContext ctx) {
        String[] beanNames = ctx.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            try {
                Object bean = ctx.getBean(beanName);
                Class<?> beanClass = bean.getClass();

                boolean hasToolMethods = false;
                for (Method method : beanClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Tool.class)) {
                        toolExecutorMap.put(method.getName(),
                                new DefaultToolExecutor(bean, method));
                        hasToolMethods = true;
                        logger.info("Auto-discovered @Tool method: {}", method.getName());
                    }
                }

                if (hasToolMethods) {
                    toolProviders.add(bean);
                }
            } catch (Exception e) {
                // skip
            }
        }
        logger.info("Auto-discovered {} tool methods from {} providers",
                toolExecutorMap.size(), toolProviders.size());
    }

    public List<ToolSpecification> buildToolSpecifications() {
        List<ToolSpecification> specs = new ArrayList<>();
        for (Object provider : toolProviders) {
            specs.addAll(ToolSpecifications.toolSpecificationsFrom(provider));
        }
        return specs;
    }

    public ToolExecutionResultMessage executeTool(ToolExecutionRequest request, FlowContext context) {
        String toolName = request.name();
        ToolExecutor executor = toolExecutorMap.get(toolName);

        if (executor == null) {
            logger.warn("Tool not found: {}", toolName);
            return ToolExecutionResultMessage.from(request,
                    "Error: Tool '" + toolName + "' not found");
        }

        try {
            String result = executor.execute(request, context);
            logger.debug("Tool executed: name={}, result={}", toolName,
                    result.substring(0, Math.min(100, result.length())));
            return ToolExecutionResultMessage.from(request, result);
        } catch (Exception e) {
            logger.error("Tool execution failed: tool={}", toolName, e);
            return ToolExecutionResultMessage.from(request, "Error: " + e.getMessage());
        }
    }

    public boolean hasTools() {
        return !toolExecutorMap.isEmpty();
    }
}
