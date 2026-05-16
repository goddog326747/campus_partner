package com.example.project.agent.flow.advisor;

import com.example.project.agent.flow.FlowContext;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatMemoryAdvisor implements FlowAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(ChatMemoryAdvisor.class);

    private final ChatMemoryStore chatMemoryStore;
    private final int maxMessages;
    private final Map<Object, MessageWindowChatMemory> memoryCache = new ConcurrentHashMap<>();

    public ChatMemoryAdvisor(ChatMemoryStore chatMemoryStore, int maxMessages) {
        this.chatMemoryStore = chatMemoryStore;
        this.maxMessages = maxMessages;
    }

    @Override
    public List<ChatMessage> before(List<ChatMessage> messages, FlowContext context) {
        Object conversationId = resolveConversationId(context);
        if (conversationId == null) {
            return messages;
        }

        MessageWindowChatMemory chatMemory = memoryCache.computeIfAbsent(conversationId,
                id -> MessageWindowChatMemory.builder()
                        .maxMessages(maxMessages)
                        .chatMemoryStore(chatMemoryStore)
                        .id(id)
                        .build());

        for (ChatMessage message : messages) {
            chatMemory.add(message);
        }

        List<ChatMessage> allMessages = new ArrayList<>(chatMemory.messages());
        logger.debug("ChatMemory before: conversationId={}, historySize={}, totalSize={}",
                conversationId, allMessages.size() - messages.size(), allMessages.size());

        return allMessages;
    }

    @Override
    public void after(List<ChatMessage> messages, AiMessage response, FlowContext context) {
        Object conversationId = resolveConversationId(context);
        if (conversationId == null) {
            return;
        }

        MessageWindowChatMemory chatMemory = memoryCache.get(conversationId);
        if (chatMemory != null) {
            chatMemory.add(response);
            logger.debug("ChatMemory after: conversationId={}, stored response", conversationId);
        }
    }

    @Override
    public String getName() {
        return "ChatMemoryAdvisor";
    }

    @Override
    public int getOrder() {
        return 100;
    }

    private Object resolveConversationId(FlowContext context) {
        Object conversationId = context.getInput("conversationId");
        if (conversationId == null) {
            conversationId = context.getVariable("conversationId");
        }
        return conversationId;
    }
}
