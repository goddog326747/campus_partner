package com.example.project.agent.flow.advisor;

import com.example.project.agent.flow.FlowContext;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

public interface FlowAdvisor {

    List<ChatMessage> before(List<ChatMessage> messages, FlowContext context);

    void after(List<ChatMessage> messages, AiMessage response, FlowContext context);

    String getName();

    int getOrder();
}
