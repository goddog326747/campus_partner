package com.example.project.agent.flow.executor;

import com.example.project.agent.flow.advisor.FlowAdvisor;
import com.example.project.agent.flow.enums.NodeType;
import dev.langchain4j.model.chat.ChatLanguageModel;

import java.util.List;

public class ContentLLMNodeExecutor extends LLMNodeExecutor {

    public ContentLLMNodeExecutor(ChatLanguageModel chatModel, ToolNodeExecutor toolExecutor,
                                  List<FlowAdvisor> advisors) {
        super(chatModel, toolExecutor, advisors);
    }

    @Override
    public String getSupportedNodeType() {
        return NodeType.LLM.name();
    }
}
