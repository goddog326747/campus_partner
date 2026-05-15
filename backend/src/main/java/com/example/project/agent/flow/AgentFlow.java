package com.example.project.agent.flow;

import com.example.project.agent.flow.enums.FlowMode;
import com.example.project.agent.flow.enums.NodeType;
import lombok.Getter;

import java.util.*;
import java.util.function.Function;

/**
 * 智能体流程定义类，用于构建和管理 AI 工作流。
 * <p>
 * 该类通过 Builder 模式支持灵活地定义流程节点、边、执行模式等，
 * 并支持同步、异步及带超时的执行方式。
 * <p>
 * <b>注意</b>：流程本身不绑定 LLM 模型或工具执行器，这些由 {@link FlowEngine} 在执行时注入。
 *
 * @author example
 * @since 1.0.0
 */
@Getter
public class AgentFlow {

    /** 流程唯一标识 */
    private final String flowId;
    /** 流程名称 */
    private final String name;
    /** 流程中所有节点，key 为节点 ID */
    private final Map<String, FlowNode> nodes;
    /** 流程中所有边（连接关系） */
    private final List<FlowEdge> edges;
    /** 流程执行模式 */
    private final FlowMode mode;
    /** 入口节点 ID */
    private final String entryNodeId;

    private AgentFlow(String flowId, String name,
                      Map<String, FlowNode> nodes,
                      List<FlowEdge> edges,
                      FlowMode mode,
                      String entryNodeId) {
        this.flowId = flowId != null ? flowId : UUID.randomUUID().toString();
        this.name = name != null ? name : "UnnamedFlow";
        this.nodes = new HashMap<>(nodes);
        this.edges = new ArrayList<>(edges);
        this.mode = mode != null ? mode : FlowMode.PLANNING;
        this.entryNodeId = entryNodeId;

        if (this.nodes.isEmpty()) {
            throw new IllegalStateException("Flow must have at least one node");
        }
    }

    /**
     * 根据节点 ID 获取节点。
     *
     * @param nodeId 节点 ID
     * @return 对应的流程节点，若不存在则返回 null
     */
    public FlowNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    /**
     * 获取指定节点的所有出边。
     *
     * @param nodeId 节点 ID
     * @return 出边列表
     */
    public List<FlowEdge> getOutgoingEdges(String nodeId) {
        return edges.stream()
                .filter(e -> e.getFromNodeId().equals(nodeId))
                .toList();
    }

    /**
     * 获取流程的起始节点列表。
     * <p>
     * 优先返回指定的入口节点，其次返回类型为 START 的节点，
     * 最后返回没有入边的节点。
     *
     * @return 起始节点列表
     */
    public List<FlowNode> getStartNodes() {
        if (entryNodeId != null) {
            FlowNode entryNode = nodes.get(entryNodeId);
            if (entryNode != null) {
                return List.of(entryNode);
            }
        }

        List<FlowNode> explicitStartNodes = nodes.values().stream()
                .filter(n -> n.getType() == NodeType.START)
                .toList();
        if (!explicitStartNodes.isEmpty()) {
            return explicitStartNodes;
        }

        Set<String> targetNodes = edges.stream()
                .map(FlowEdge::getToNodeId)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);

        return nodes.values().stream()
                .filter(n -> !targetNodes.contains(n.getNodeId()))
                .toList();
    }

    /**
     * 获取流程构建器。
     *
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * AgentFlow 的构建器类，用于链式构建流程定义。
     */
    public static class Builder {
        private String flowId;
        private String name;
        private final Map<String, FlowNode> nodes = new HashMap<>();
        private final List<FlowEdge> edges = new ArrayList<>();
        private FlowMode mode = FlowMode.PLANNING;
        private String entryNodeId;

        public Builder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder mode(FlowMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder entryNode(String entryNodeId) {
            this.entryNodeId = entryNodeId;
            return this;
        }

        public Builder node(String nodeId, FlowNode node) {
            this.nodes.put(nodeId, node);
            return this;
        }

        public Builder edge(String from, String to) {
            this.edges.add(FlowEdge.sequential(from, to));
            return this;
        }

        public Builder edge(String from, String to, Function<FlowContext, Boolean> condition) {
            this.edges.add(FlowEdge.conditional(from, to, condition));
            return this;
        }

        public AgentFlow build() {
            return new AgentFlow(flowId, name, nodes, edges, mode, entryNodeId);
        }
    }
}
