package com.example.project.agent.flow.executor;

import com.example.project.agent.flow.FlowContext;
import com.example.project.agent.flow.FlowNode;
import com.example.project.agent.flow.dto.FlowNodeExecutionResult;
import com.example.project.agent.flow.enums.NodeType;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class LoopNodeExecutor implements NodeExecutor {

    private static final Logger logger = LoggerFactory.getLogger(LoopNodeExecutor.class);

    private final ChatLanguageModel chatModel;

    public LoopNodeExecutor(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public FlowNodeExecutionResult execute(FlowNode node, FlowContext context) {
        String loopCondition = node.getConfig("loopCondition", "检查内容是否满足要求");
        String lastOutput = context.getLastOutput() != null ? context.getLastOutput().toString() : "";

        String prompt = loopCondition + "\n\n当前内容：\n" + lastOutput +
                "\n\n如果内容已经满足要求，回复'PASS'，否则回复'RETRY'并说明需要改进的地方。";

        try {
            Response<AiMessage> response = chatModel.generate(Arrays.asList(
                    SystemMessage.from("你是一个质量评估助手，负责判断内容是否满足要求。只回复 PASS 或 RETRY。"),
                    UserMessage.from(prompt)
            ));

            String decision = response.content().text().trim().toUpperCase();
            boolean shouldContinue = !decision.contains("PASS");

            logger.debug("Loop evaluation: node={}, decision={}, continue={}", node.getName(), decision, shouldContinue);

            return FlowNodeExecutionResult.builder()
                    .nodeId(node.getNodeId())
                    .nodeName(node.getName())
                    .success(true)
                    .output(decision)
                    .metadata("loopContinue", shouldContinue)
                    .build();
        } catch (Exception e) {
            logger.error("Loop evaluation failed: node={}, error={}", node.getName(), e.getMessage(), e);
            return FlowNodeExecutionResult.builder()
                    .nodeId(node.getNodeId())
                    .nodeName(node.getName())
                    .success(false)
                    .error(e.getMessage())
                    .metadata("loopContinue", false)
                    .build();
        }
    }

    @Override
    public String getSupportedNodeType() {
        return NodeType.LOOP.name();
    }
}
