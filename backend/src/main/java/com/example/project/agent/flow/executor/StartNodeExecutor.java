package com.example.project.agent.flow.executor;

import com.example.project.agent.flow.FlowContext;
import com.example.project.agent.flow.FlowNode;
import com.example.project.agent.flow.dto.FlowNodeExecutionResult;
import com.example.project.agent.flow.enums.NodeType;
import org.springframework.stereotype.Component;

@Component("startNodeExecutor")
public class StartNodeExecutor implements NodeExecutor {

    @Override
    public FlowNodeExecutionResult execute(FlowNode node, FlowContext context) {
        return FlowNodeExecutionResult.builder()
                .nodeId(node.getNodeId())
                .nodeName(node.getName())
                .success(true)
                .output("Started")
                .build();
    }

    @Override
    public String getSupportedNodeType() {
        return NodeType.START.name();
    }
}
