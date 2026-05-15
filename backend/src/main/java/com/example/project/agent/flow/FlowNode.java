package com.example.project.agent.flow;

import com.example.project.agent.flow.enums.NodeType;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.*;

@Getter
public class FlowNode {

    private final String nodeId;
    private final String name;
    private final NodeType type;
    private final String executorType;
    private final Map<String, Object> config;
    private final List<String> dependencies;

    @Builder
    public FlowNode(String nodeId, String name, NodeType type, String executorType,
                    @Singular("config") Map<String, Object> config,
                    @Singular("dependency") List<String> dependencies) {
        this.nodeId = nodeId != null ? nodeId : UUID.randomUUID().toString();
        this.name = name != null ? name : "UnnamedNode";
        this.type = type != null ? type : NodeType.LLM;
        this.executorType = executorType;
        this.config = new HashMap<>(config != null ? config : new HashMap<>());
        this.dependencies = new ArrayList<>(dependencies != null ? dependencies : new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfig(String key) {
        return (T) config.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfig(String key, T defaultValue) {
        return (T) config.getOrDefault(key, defaultValue);
    }

    public static FlowNodeBuilder llmBuilder(String nodeId, String name,
                                              String systemPrompt, String userPromptTemplate) {
        return builder()
                .nodeId(nodeId)
                .name(name)
                .type(NodeType.LLM)
                .config("systemPrompt", systemPrompt)
                .config("userPromptTemplate", userPromptTemplate);
    }

    public static FlowNode startNode(String nodeId) {
        return builder()
                .nodeId(nodeId)
                .name("Start")
                .type(NodeType.START)
                .build();
    }

    public static FlowNodeBuilder loopBuilder(String nodeId, String name,
                                               String loopTarget, String loopCondition) {
        return builder()
                .nodeId(nodeId)
                .name(name)
                .type(NodeType.LOOP)
                .config("loopTarget", loopTarget)
                .config("loopCondition", loopCondition)
                .config("maxIterations", 3);
    }

    public static FlowNode endNode(String nodeId) {
        return builder()
                .nodeId(nodeId)
                .name("End")
                .type(NodeType.END)
                .build();
    }
}
