package com.example.project.agent.flow.executor;

import com.example.project.agent.flow.FlowNode;
import com.example.project.agent.flow.enums.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NodeExecutorRegistry {

    private static final Logger logger = LoggerFactory.getLogger(NodeExecutorRegistry.class);

    private final Map<String, NodeExecutor> executors;

    public NodeExecutorRegistry(Map<String, NodeExecutor> executorMap) {
        this.executors = executorMap;
        logger.info("Registered {} node executors: {}", executorMap.size(), executorMap.keySet());
    }

    public NodeExecutor resolveExecutor(FlowNode node) {
        if (node.getExecutorType() != null && !node.getExecutorType().isEmpty()) {
            return getExecutor(node.getExecutorType());
        }
        return getExecutor(node.getType());
    }

    public NodeExecutor getExecutor(NodeType nodeType) {
        for (NodeExecutor executor : executors.values()) {
            if (executor.getSupportedNodeType().equals(nodeType.name())) {
                return executor;
            }
        }
        throw new IllegalStateException("No executor found for node type: " + nodeType +
                ", available executors: " + executors.keySet());
    }

    public NodeExecutor getExecutor(String executorName) {
        NodeExecutor executor = executors.get(executorName);
        if (executor == null) {
            throw new IllegalStateException("No executor found with name: " + executorName +
                    ", available executors: " + executors.keySet());
        }
        return executor;
    }

    public boolean supports(NodeType nodeType) {
        for (NodeExecutor executor : executors.values()) {
            if (executor.getSupportedNodeType().equals(nodeType.name())) {
                return true;
            }
        }
        return false;
    }
}
