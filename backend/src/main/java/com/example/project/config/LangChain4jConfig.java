package com.example.project.config;

import com.example.project.agent.flow.advisor.ChatMemoryAdvisor;
import com.example.project.agent.flow.advisor.FlowAdvisor;
import com.example.project.agent.flow.advisor.InMemoryChatMemoryStore;
import com.example.project.agent.flow.executor.ContentLLMNodeExecutor;
import com.example.project.agent.flow.executor.LLMNodeExecutor;
import com.example.project.agent.flow.executor.LoopNodeExecutor;
import com.example.project.agent.flow.executor.ToolNodeExecutor;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class LangChain4jConfig {

    @Value("${aliyun.ai.api-key}")
    private String apiKey;

    @Value("${aliyun.ai.api-url}")
    private String apiUrl;

    @Value("${aliyun.ai.model}")
    private String flashModelName;

    @Value("${aliyun.ai.content-model}")
    private String proModelName;

    @Value("${aliyun.ai.glm-model}")
    private String glmModelName;

    @Bean
    @Qualifier("flashModel")
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(extractBaseUrl(apiUrl))
                .modelName(flashModelName)
                .timeout(Duration.ofSeconds(60))
                .temperature(0.7)
                .maxTokens(2000)
                .build();
    }

    @Bean
    @Qualifier("proModel")
    public ChatLanguageModel contentChatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(extractBaseUrl(apiUrl))
                .modelName(proModelName)
                .timeout(Duration.ofSeconds(120))
                .temperature(0.5)
                .maxTokens(4000)
                .build();
    }

    @Bean
    @Qualifier("glmModel")
    public ChatLanguageModel glmChatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(extractBaseUrl(apiUrl))
                .modelName(glmModelName)
                .timeout(Duration.ofSeconds(120))
                .temperature(0.7)
                .maxTokens(4000)
                .build();
    }

    @Bean
    @Qualifier("proStreamingModel")
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(extractBaseUrl(apiUrl))
                .modelName(glmModelName)
                .timeout(Duration.ofSeconds(120))
                .temperature(0.8)
                .maxTokens(4000)
                .build();
    }

    @Bean
    public ChatMemoryStore chatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    @Bean
    public ChatMemoryAdvisor chatMemoryAdvisor(ChatMemoryStore chatMemoryStore) {
        return new ChatMemoryAdvisor(chatMemoryStore, 20);
    }

    @Bean
    public List<FlowAdvisor> flowAdvisors(ChatMemoryAdvisor chatMemoryAdvisor) {
        return List.of(chatMemoryAdvisor);
    }

    @Bean
    public LLMNodeExecutor llmNodeExecutor(@Qualifier("flashModel") ChatLanguageModel chatModel,
                                            ToolNodeExecutor toolExecutor,
                                            List<FlowAdvisor> advisors) {
        return new LLMNodeExecutor(chatModel, toolExecutor, advisors);
    }

    @Bean
    public ContentLLMNodeExecutor contentLLMNodeExecutor(@Qualifier("proModel") ChatLanguageModel contentModel,
                                                          ToolNodeExecutor toolExecutor,
                                                          List<FlowAdvisor> advisors) {
        return new ContentLLMNodeExecutor(contentModel, toolExecutor, advisors);
    }

    @Bean
    public LoopNodeExecutor loopNodeExecutor(@Qualifier("flashModel") ChatLanguageModel chatModel) {
        return new LoopNodeExecutor(chatModel);
    }

    private String extractBaseUrl(String apiUrl) {
        if (apiUrl.contains("/chat/completions")) {
            return apiUrl.replace("/chat/completions", "");
        }
        return apiUrl;
    }
}
