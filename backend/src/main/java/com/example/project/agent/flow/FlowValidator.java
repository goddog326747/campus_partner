package com.example.project.agent.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 流程验证器，用于校验 AgentFlow 定义的合法性和完整性。
 * <p>
 * 验证内容包括：节点非空、起始节点存在、边引用有效性以及循环检测等，
 * 确保流程在执行前满足基本约束条件。
 *
 * @author example
 * @since 1.0.0
 */
public class FlowValidator {

    /** 日志记录器 */
    private static final Logger logger = LoggerFactory.getLogger(FlowValidator.class);

    /**
     * 验证流程定义的合法性。
     * <p>
     * 检查节点数量、起始节点、边引用以及循环风险，若验证失败则抛出异常。
     *
     * @param flow 待验证的流程定义
     * @throws IllegalStateException 当验证不通过时抛出
     */
    public static void validate(AgentFlow flow) {
        List<String> errors = new ArrayList<>();

        if (flow.getNodes().isEmpty()) {
            errors.add("Flow must have at least one node");
        }

        List<FlowNode> startNodes = flow.getStartNodes();
        if (startNodes.isEmpty()) {
            errors.add("Flow must have at least one start node (type=START or no incoming edges)");
        }

        for (FlowEdge edge : flow.getEdges()) {
            if (!flow.getNodes().containsKey(edge.getFromNodeId())) {
                errors.add("Edge references unknown source node: " + edge.getFromNodeId());
            }
            if (!flow.getNodes().containsKey(edge.getToNodeId())) {
                errors.add("Edge references unknown target node: " + edge.getToNodeId());
            }
        }

        if (hasCycle(flow)) {
            logger.warn("Flow may contain cycles - execution could loop indefinitely for non-PLANNING modes");
        }

        if (!errors.isEmpty()) {
            throw new IllegalStateException("Flow validation failed: " + String.join("; ", errors));
        }
    }

    /**
     * 检测流程中是否存在循环。
     * <p>
     * 使用深度优先搜索（DFS）从所有起始节点出发检测回边。
     *
     * @param flow 流程定义
     * @return 若存在循环则返回 true，否则返回 false
     */
    public static boolean hasCycle(AgentFlow flow) {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (FlowNode startNode : flow.getStartNodes()) {
            if (detectCycleDFS(flow, startNode.getNodeId(), visited, recursionStack)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 使用深度优先搜索检测从指定节点出发是否存在循环。
     *
     * @param flow           流程定义
     * @param nodeId         当前节点 ID
     * @param visited        已访问节点集合
     * @param recursionStack 当前递归栈中的节点集合
     * @return 若检测到循环则返回 true，否则返回 false
     */
    private static boolean detectCycleDFS(AgentFlow flow, String nodeId,
                                          Set<String> visited, Set<String> recursionStack) {
        visited.add(nodeId);
        recursionStack.add(nodeId);

        for (FlowEdge edge : flow.getOutgoingEdges(nodeId)) {
            String nextId = edge.getToNodeId();
            if (!visited.contains(nextId)) {
                if (detectCycleDFS(flow, nextId, visited, recursionStack)) {
                    return true;
                }
            } else if (recursionStack.contains(nextId)) {
                return true;
            }
        }

        recursionStack.remove(nodeId);
        return false;
    }
}
