# Spring AI vs LangChain4j 开发对比指南

## 📋 目录

- [概述](#概述)
- [核心概念对比](#核心概念对比)
- [依赖配置](#依赖配置)
- [模型配置](#模型配置)
- [基础调用方式](#基础调用方式)
- [流式输出](#流式输出)
- [对话记忆](#对话记忆)
- [工具调用](#工具调用)
- [Prompt模板](#prompt模板)
- [RAG检索增强](#rag检索增强)
- [拦截器/Advisor](#拦截器advisor)
- [迁移指南](#迁移指南)
- [选型建议](#选型建议)

---

## 概述

### Spring AI

| 属性 | 说明 |
|------|------|
| **维护方** | Spring官方团队 |
| **发布时间** | 2023年 |
| **设计理念** | Spring生态原生集成，开箱即用 |
| **支持模型** | OpenAI、Azure OpenAI、Ollama、百度文心、阿里通义等 |
| **文档地址** | https://docs.spring.io/spring-ai/reference/ |

### LangChain4j

| 属性 | 说明 |
|------|------|
| **维护方** | 开源社区 |
| **发布时间** | 2023年 |
| **设计理念** | Java版LangChain，跨框架通用 |
| **支持模型** | OpenAI、Azure OpenAI、Google AI、阿里通义、百度文心等 |
| **文档地址** | https://docs.langchain4j.dev/ |

---

## 核心概念对比

| 功能 | Spring AI | LangChain4j |
|------|-----------|-------------|
| **模型调用** | `ChatClient` | `ChatLanguageModel` |
| **流式模型** | `StreamingChatModel` | `StreamingChatLanguageModel` |
| **对话记忆** | `ChatMemory` | `ChatMemory` |
| **工具调用** | `FunctionCallback` | `@Tool` 注解 |
| **Agent构建** | `Advisor` 链 | `AiServices` |
| **Prompt模板** | `PromptTemplate` | 字符串模板 |
| **RAG** | `VectorStore` + `QuestionAnswerAdvisor` | `EmbeddingStore` + `ContentRetriever` |
| **配置方式** | YAML自动配置 | Java代码配置 |

---

## 依赖配置

### Spring AI

```xml
<!-- pom.xml -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- OpenAI 兼容接口 -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    </dependency>
    
    <!-- 向量数据库（可选） -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-pgvector-store-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

### LangChain4j

```xml
<!-- pom.xml -->
<properties>
    <langchain4j.version>0.36.2</langchain4j.version>
</properties>

<dependencies>
    <!-- 核心库 -->
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j</artifactId>
        <version>${langchain4j.version}</version>
    </dependency>
    
    <!-- OpenAI 兼容接口 -->
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j-open-ai</artifactId>
        <version>${langchain4j.version}</version>
    </dependency>
    
    <!-- 向量存储（可选） -->
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j-embeddings-store</artifactId>
        <version>${langchain4j.version}</version>
    </dependency>
</dependencies>
```

---

## 模型配置

### Spring AI

```yaml
# application.yml
spring:
  ai:
    openai:
      api-key: ${AI_API_KEY}
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1  # 阿里云兼容接口
      chat:
        options:
          model: qwen-plus
          temperature: 0.7
          max-tokens: 2000
      embedding:
        options:
          model: text-embedding-v3
```

```java
// 无需手动配置Bean，直接注入使用
@Service
public class AiService {
    
    @Autowired
    private ChatClient chatClient;  // 自动注入
    
    @Autowired
    private EmbeddingModel embeddingModel;  // 向量模型
}
```

### LangChain4j

```yaml
# application.yml
aliyun:
  ai:
    api-key: ${AI_API_KEY}
    api-url: https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
    model: qwen-plus
```

```java
// 需要手动配置Bean
@Configuration
public class LangChain4jConfig {
    
    @Value("${aliyun.ai.api-key}")
    private String apiKey;
    
    @Value("${aliyun.ai.api-url}")
    private String apiUrl;
    
    @Value("${aliyun.ai.model}")
    private String model;
    
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(extractBaseUrl(apiUrl))
                .modelName(model)
                .timeout(Duration.ofSeconds(60))
                .temperature(0.7)
                .maxTokens(2000)
                .build();
    }
    
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(extractBaseUrl(apiUrl))
                .modelName(model)
                .timeout(Duration.ofSeconds(60))
                .temperature(0.7)
                .maxTokens(2000)
                .build();
    }
    
    private String extractBaseUrl(String apiUrl) {
        if (apiUrl.contains("/chat/completions")) {
            return apiUrl.replace("/chat/completions", "");
        }
        return apiUrl;
    }
}
```

---

## 基础调用方式

### Spring AI

```java
@Service
public class AiService {
    
    @Autowired
    private ChatClient chatClient;
    
    /**
     * 简单调用
     */
    public String chat(String prompt) {
        return chatClient.call(prompt);
    }
    
    /**
     * 带选项调用
     */
    public String chatWithOptions(String prompt) {
        ChatResponse response = chatClient.call(
            new Prompt(prompt, 
                OpenAiChatOptions.builder()
                    .withModel("qwen-plus")
                    .withTemperature(0.8)
                    .withMaxTokens(1000)
                    .build()
            )
        );
        return response.getResult().getOutput().getContent();
    }
    
    /**
     * 多轮对话
     */
    public String chatWithHistory(List<Message> messages) {
        ChatResponse response = chatClient.call(new Prompt(messages));
        return response.getResult().getOutput().getContent();
    }
}
```

### LangChain4j

```java
@Service
public class AiService {
    
    private final ChatLanguageModel chatModel;
    
    public AiService(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }
    
    /**
     * 简单调用
     */
    public String chat(String prompt) {
        return chatModel.generate(prompt);
    }
    
    /**
     * 带选项调用（需要在配置时设置）
     */
    public String chatWithOptions(String prompt) {
        // LangChain4j 的选项通常在构建模型时配置
        // 也可以动态创建新模型实例
        ChatLanguageModel customModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName("qwen-plus")
                .temperature(0.8)
                .maxTokens(1000)
                .build();
        return customModel.generate(prompt);
    }
    
    /**
     * 多轮对话
     */
    public String chatWithHistory(List<ChatMessage> messages) {
        return chatModel.generate(messages);
    }
}
```

---

## 流式输出

### Spring AI

```java
@Service
public class StreamingService {
    
    @Autowired
    private ChatClient chatClient;
    
    /**
     * 返回Flux流
     */
    public Flux<String> streamChat(String prompt) {
        return chatClient.stream(prompt)
                .map(chunk -> chunk.getResult().getOutput().getContent());
    }
    
    /**
     * SSE接口
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamSSE(String prompt) {
        return chatClient.stream(prompt)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk.getResult().getOutput().getContent())
                        .build());
    }
}
```

### LangChain4j

```java
@Service
public class StreamingService {
    
    private final StreamingChatLanguageModel streamingModel;
    
    public StreamingService(StreamingChatLanguageModel streamingModel) {
        this.streamingModel = streamingModel;
    }
    
    /**
     * 使用回调处理
     */
    public void streamChat(String prompt, Consumer<String> onToken) {
        streamingModel.generate(prompt, new StreamingResponseHandler<>() {
            @Override
            public void onNext(String token) {
                onToken.accept(token);
            }
            
            @Override
            public void onComplete(Response<AiMessage> response) {
                // 完成回调
            }
            
            @Override
            public void onError(Throwable error) {
                // 错误处理
            }
        });
    }
    
    /**
     * 转换为Flux流
     */
    public Flux<String> toFlux(String prompt) {
        return Flux.create(emitter -> {
            streamingModel.generate(prompt, new StreamingResponseHandler<>() {
                @Override
                public void onNext(String token) {
                    emitter.next(token);
                }
                
                @Override
                public void onComplete(Response<AiMessage> response) {
                    emitter.complete();
                }
                
                @Override
                public void onError(Throwable error) {
                    emitter.error(error);
                }
            });
        });
    }
    
    /**
     * SSE接口
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamSSE(String prompt) {
        return toFlux(prompt)
                .map(token -> ServerSentEvent.<String>builder()
                        .data(token)
                        .build());
    }
}
```

---

## 对话记忆

### Spring AI

```java
@Configuration
public class AiConfig {
    
    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();  // 内存存储
        // 或使用持久化存储
        // return new PersistentChatMemory(memoryStore);
    }
}

@Service
public class ChatService {
    
    @Autowired
    private ChatClient chatClient;
    
    @Autowired
    private ChatMemory chatMemory;
    
    /**
     * 带记忆的对话
     */
    public String chatWithMemory(String sessionId, String prompt) {
        // 添加用户消息到记忆
        chatMemory.add(sessionId, new UserMessage(prompt));
        
        // 获取历史消息
        List<Message> history = chatMemory.get(sessionId, 10);  // 最近10条
        
        // 调用模型
        ChatResponse response = chatClient.call(new Prompt(history));
        String reply = response.getResult().getOutput().getContent();
        
        // 添加助手回复到记忆
        chatMemory.add(sessionId, new AssistantMessage(reply));
        
        return reply;
    }
    
    /**
     * 使用Advisor自动管理记忆
     */
    public String chatWithAdvisor(String sessionId, String prompt) {
        ChatResponse response = ChatClient.builder(chatClient)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build()
                .call(new Prompt(
                    new UserMessage(prompt),
                    ChatOptionsBuilder.builder()
                        .withChatMemory(sessionId)
                        .build()
                ));
        return response.getResult().getOutput().getContent();
    }
}
```

### LangChain4j

```java
// 定义Agent接口
interface ChatAssistant {
    String chat(@MemoryId String sessionId, @UserMessage String prompt);
}

@Configuration
public class AiAgentConfig {
    
    @Bean
    public ChatAssistant chatAssistant(ChatLanguageModel chatModel) {
        return AiServices.builder(ChatAssistant.class)
                .chatLanguageModel(chatModel)
                // 自动管理对话记忆
                .chatMemoryProvider(sessionId -> 
                    MessageWindowChatMemory.withMaxMessages(10)
                )
                .build();
    }
}

@Service
public class ChatService {
    
    @Autowired
    private ChatAssistant chatAssistant;
    
    /**
     * 带记忆的对话（自动管理）
     */
    public String chatWithMemory(String sessionId, String prompt) {
        // 记忆自动管理，无需手动处理
        return chatAssistant.chat(sessionId, prompt);
    }
}
```

---

## 工具调用

### Spring AI

```java
/**
 * 定义工具函数
 */
@Component
public class PostTools {
    
    private final PostService postService;
    private final UserMapper userMapper;
    
    public PostTools(PostService postService, UserMapper userMapper) {
        this.postService = postService;
        this.userMapper = userMapper;
    }
    
    /**
     * 查询用户发帖风格
     */
    @Bean
    @Description("查询用户的发帖风格偏好")
    public Function<UserStyleRequest, String> getUserStyle() {
        return request -> {
            User user = userMapper.selectById(request.userId());
            return user != null ? user.getStyle() : "默认风格";
        };
    }
    
    /**
     * 获取热门话题
     */
    @Bean
    @Description("获取当前热门话题列表")
    public Function<Void, List<String>> getHotTopics() {
        return request -> postService.getHotTopics();
    }
    
    /**
     * 检查敏感词
     */
    @Bean
    @Description("检查文本是否包含敏感词")
    public Function<SensitiveCheckRequest, Boolean> checkSensitive() {
        return request -> postService.containsSensitiveWords(request.content());
    }
}

// 配置文件中启用
# application.yml
spring:
  ai:
    openai:
      chat:
        options:
          functions: getUserStyle, getHotTopics, checkSensitive

// 使用
@Service
public class AiService {
    
    @Autowired
    private ChatClient chatClient;
    
    public String chatWithTools(String prompt) {
        ChatResponse response = chatClient.call(
            new Prompt(prompt,
                ChatOptionsBuilder.builder()
                    .withFunction("getUserStyle")
                    .withFunction("getHotTopics")
                    .build()
            )
        );
        return response.getResult().getOutput().getContent();
    }
}
```

### LangChain4j

```java
/**
 * 定义工具类
 */
public class PostTools {
    
    private final PostService postService;
    private final UserMapper userMapper;
    
    public PostTools(PostService postService, UserMapper userMapper) {
        this.postService = postService;
        this.userMapper = userMapper;
    }
    
    /**
     * 查询用户发帖风格
     */
    @Tool("查询用户的发帖风格偏好")
    public String getUserStyle(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getStyle() : "默认风格";
    }
    
    /**
     * 获取热门话题
     */
    @Tool("获取当前热门话题列表")
    public List<String> getHotTopics() {
        return postService.getHotTopics();
    }
    
    /**
     * 检查敏感词
     */
    @Tool("检查文本是否包含敏感词，返回true表示包含敏感词")
    public boolean checkSensitive(String content) {
        return postService.containsSensitiveWords(content);
    }
}

// 定义Agent接口
interface PostAssistant {
    @SystemMessage("你是一个大学生活动策划助手")
    String chat(@UserMessage String prompt);
}

// 配置Agent
@Configuration
public class AiAgentConfig {
    
    @Bean
    public PostAssistant postAssistant(
            ChatLanguageModel chatModel,
            PostService postService,
            UserMapper userMapper
    ) {
        return AiServices.builder(PostAssistant.class)
                .chatLanguageModel(chatModel)
                .tools(new PostTools(postService, userMapper))
                .build();
    }
}

// 使用
@Service
public class AiService {
    
    @Autowired
    private PostAssistant postAssistant;
    
    public String chatWithTools(String prompt) {
        return postAssistant.chat(prompt);
    }
}
```

---

## Prompt模板

### Spring AI

```java
@Configuration
public class PromptConfig {
    
    /**
     * 定义模板Bean
     */
    @Bean
    public PromptTemplate postGenerateTemplate() {
        return new PromptTemplate("""
            你是一个{role}。
            
            请根据以下要求生成帖子：
            - 主题：{topic}
            - 分类：{category}
            - 风格：{style}
            
            输出格式：
            { "title": "标题", "content": "内容" }
            """);
    }
}

@Service
public class PromptService {
    
    @Autowired
    private PromptTemplate postGenerateTemplate;
    
    @Autowired
    private ChatClient chatClient;
    
    public String generatePost(String topic, String category, String style) {
        // 渲染模板
        Prompt prompt = postGenerateTemplate.create(Map.of(
            "role", "大学生活动策划助手",
            "topic", topic,
            "category", category,
            "style", style
        ));
        
        // 调用模型
        return chatClient.call(prompt).getResult().getOutput().getContent();
    }
}
```

### LangChain4j

```java
// 方式1：直接字符串拼接
@Service
public class PromptService {
    
    private final ChatLanguageModel chatModel;
    
    public String generatePost(String topic, String category, String style) {
        String prompt = """
            你是一个大学生活动策划助手。
            
            请根据以下要求生成帖子：
            - 主题：%s
            - 分类：%s
            - 风格：%s
            
            输出格式：
            { "title": "标题", "content": "内容" }
            """.formatted(topic, category, style);
        
        return chatModel.generate(prompt);
    }
}

// 方式2：使用AiServices的模板注解
interface PostAssistant {
    
    @SystemMessage("""
        你是一个{{role}}。
        请用{{style}}的风格回复。
        """)
    String generatePost(
        @UserMessage String topic,
        @V("role") String role,
        @V("style") String style
    );
}

// 方式3：使用PromptTemplate类
@Service
public class PromptService {
    
    public String generatePost(String topic, String category, String style) {
        PromptTemplate template = PromptTemplate.from("""
            你是一个{role}。
            
            请根据以下要求生成帖子：
            - 主题：{topic}
            - 分类：{category}
            - 风格：{style}
            """);
        
        Map<String, Object> variables = Map.of(
            "role", "大学生活动策划助手",
            "topic", topic,
            "category", category,
            "style", style
        );
        
        Prompt prompt = template.apply(variables);
        return chatModel.generate(prompt.toUserMessage());
    }
}
```

---

## RAG检索增强

### Spring AI

```java
@Configuration
public class RagConfig {
    
    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate) {
        return new PgVectorStore(jdbcTemplate);
    }
    
    @Bean
    public EmbeddingModel embeddingModel() {
        return new OpenAiEmbeddingModel();
    }
}

@Service
public class RagService {
    
    @Autowired
    private VectorStore vectorStore;
    
    @Autowired
    private EmbeddingModel embeddingModel;
    
    @Autowired
    private ChatClient chatClient;
    
    /**
     * 添加文档到向量库
     */
    public void addDocument(String content) {
        List<Double> embedding = embeddingModel.embed(content);
        vectorStore.add(List.of(
            new Document(content, Map.of(), embedding)
        ));
    }
    
    /**
     * RAG查询
     */
    public String ragQuery(String query) {
        // 1. 向量检索
        List<Document> docs = vectorStore.similaritySearch(
            SearchRequest.query(query).withTopK(3)
        );
        
        // 2. 构建增强Prompt
        String context = docs.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n"));
        
        String prompt = """
            根据以下参考信息回答问题：
            
            参考信息：
            %s
            
            问题：%s
            """.formatted(context, query);
        
        return chatClient.call(prompt);
    }
    
    /**
     * 使用QuestionAnswerAdvisor自动RAG
     */
    public String ragWithAdvisor(String query) {
        ChatClient ragClient = ChatClient.builder(chatClient)
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
        
        return ragClient.call(query).getResult().getOutput().getContent();
    }
}
```

### LangChain4j

```java
@Configuration
public class RagConfig {
    
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();  // 内存存储
        // 或使用持久化存储
        // return new PgVectorEmbeddingStore(dataSource);
    }
    
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName("text-embedding-v3")
                .build();
    }
    
    @Bean
    public EmbeddingStoreIngestor ingestor(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel
    ) {
        return EmbeddingStoreIngestor.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .documentSplitter(DocumentSplitters.recursive(500))
                .build();
    }
}

// 定义RAG Agent
interface RagAssistant {
    @SystemMessage("根据提供的上下文信息回答问题，如果上下文中没有相关信息，请说明。")
    String answer(String query);
}

@Configuration
public class RagAgentConfig {
    
    @Bean
    public RagAssistant ragAssistant(
            ChatLanguageModel chatModel,
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel
    ) {
        return AiServices.builder(RagAssistant.class)
                .chatLanguageModel(chatModel)
                // 配置内容检索器
                .contentRetriever(EmbeddingStoreContentRetriever.builder()
                        .embeddingStore(embeddingStore)
                        .embeddingModel(embeddingModel)
                        .maxResults(3)
                        .build())
                .build();
    }
}

@Service
public class RagService {
    
    @Autowired
    private EmbeddingStoreIngestor ingestor;
    
    @Autowired
    private RagAssistant ragAssistant;
    
    /**
     * 添加文档到向量库
     */
    public void addDocument(String content) {
        Document document = Document.from(content);
        ingestor.ingest(document);
    }
    
    /**
     * RAG查询（自动检索）
     */
    public String ragQuery(String query) {
        return ragAssistant.answer(query);
    }
}
```

---

## 拦截器/Advisor

### Spring AI

```java
/**
 * 自定义Advisor
 */
public class LoggingAdvisor implements RequestResponseAdvisor {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingAdvisor.class);
    
    @Override
    public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        log.info("AI请求: {}", request.userText());
        context.put("startTime", System.currentTimeMillis());
        return request;
    }
    
    @Override
    public ChatResponse adviseResponse(ChatResponse response, Map<String, Object> context) {
        long duration = System.currentTimeMillis() - (Long) context.get("startTime");
        log.info("AI响应耗时: {}ms", duration);
        log.info("AI响应: {}", response.getResult().getOutput().getContent());
        return response;
    }
}

/**
 * 敏感词过滤Advisor
 */
public class SensitiveWordAdvisor implements RequestResponseAdvisor {
    
    private final Set<String> sensitiveWords;
    
    public SensitiveWordAdvisor(Set<String> sensitiveWords) {
        this.sensitiveWords = sensitiveWords;
    }
    
    @Override
    public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        String filtered = filterSensitiveWords(request.userText());
        return AdvisedRequest.from(request)
                .withUserText(filtered)
                .build();
    }
    
    @Override
    public ChatResponse adviseResponse(ChatResponse response, Map<String, Object> context) {
        String content = response.getResult().getOutput().getContent();
        String filtered = filterSensitiveWords(content);
        // 返回过滤后的响应
        return new ChatResponse(List.of(
            new Generation(new AssistantMessage(filtered))
        ));
    }
    
    private String filterSensitiveWords(String text) {
        for (String word : sensitiveWords) {
            text = text.replace(word, "***");
        }
        return text;
    }
}

// 使用
@Configuration
public class AdvisorConfig {
    
    @Bean
    public ChatClient chatClientWithAdvisors(ChatClient.Builder builder) {
        return builder
                .defaultAdvisors(
                    new LoggingAdvisor(),
                    new SensitiveWordAdvisor(Set.of("敏感词1", "敏感词2"))
                )
                .build();
    }
}
```

### LangChain4j

```java
/**
 * 自定义拦截器接口
 */
public interface AiCallInterceptor {
    
    /**
     * 调用前拦截
     */
    default String beforeCall(String prompt) {
        return prompt;
    }
    
    /**
     * 调用后拦截
     */
    default String afterCall(String response) {
        return response;
    }
    
    /**
     * 异常处理
     */
    default void onError(Throwable error) {
        // 默认空实现
    }
}

/**
 * 日志拦截器
 */
public class LoggingInterceptor implements AiCallInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);
    
    @Override
    public String beforeCall(String prompt) {
        log.info("AI请求: {}", prompt);
        return prompt;
    }
    
    @Override
    public String afterCall(String response) {
        log.info("AI响应: {}", response);
        return response;
    }
    
    @Override
    public void onError(Throwable error) {
        log.error("AI调用异常", error);
    }
}

/**
 * 敏感词过滤拦截器
 */
public class SensitiveWordInterceptor implements AiCallInterceptor {
    
    private final Set<String> sensitiveWords;
    
    public SensitiveWordInterceptor(Set<String> sensitiveWords) {
        this.sensitiveWords = sensitiveWords;
    }
    
    @Override
    public String beforeCall(String prompt) {
        return filterSensitiveWords(prompt);
    }
    
    @Override
    public String afterCall(String response) {
        return filterSensitiveWords(response);
    }
    
    private String filterSensitiveWords(String text) {
        for (String word : sensitiveWords) {
            text = text.replace(word, "***");
        }
        return text;
    }
}

/**
 * 拦截器管理器
 */
@Service
public class InterceptedAiService {
    
    private final ChatLanguageModel chatModel;
    private final List<AiCallInterceptor> interceptors;
    
    public InterceptedAiService(
            ChatLanguageModel chatModel,
            List<AiCallInterceptor> interceptors
    ) {
        this.chatModel = chatModel;
        this.interceptors = interceptors;
    }
    
    public String chat(String prompt) {
        // 前置拦截
        for (AiCallInterceptor interceptor : interceptors) {
            prompt = interceptor.beforeCall(prompt);
        }
        
        try {
            String response = chatModel.generate(prompt);
            
            // 后置拦截
            for (AiCallInterceptor interceptor : interceptors) {
                response = interceptor.afterCall(response);
            }
            
            return response;
        } catch (Exception e) {
            for (AiCallInterceptor interceptor : interceptors) {
                interceptor.onError(e);
            }
            throw e;
        }
    }
}

// 配置
@Configuration
public class InterceptorConfig {
    
    @Bean
    public List<AiCallInterceptor> interceptors() {
        return List.of(
            new LoggingInterceptor(),
            new SensitiveWordInterceptor(Set.of("敏感词1", "敏感词2"))
        );
    }
    
    @Bean
    public InterceptedAiService interceptedAiService(
            ChatLanguageModel chatModel,
            List<AiCallInterceptor> interceptors
    ) {
        return new InterceptedAiService(chatModel, interceptors);
    }
}
```

---

## 迁移指南

### 从LangChain4j迁移到Spring AI

#### 1. 修改依赖

```xml
<!-- 删除 -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j</artifactId>
</dependency>
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
</dependency>

<!-- 添加 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>
```

#### 2. 修改配置

```yaml
# 删除
aliyun:
  ai:
    api-key: xxx
    api-url: xxx
    model: xxx

# 添加
spring:
  ai:
    openai:
      api-key: xxx
      base-url: xxx
      chat:
        options:
          model: xxx
```

#### 3. 删除手动配置类

```java
// 删除 LangChain4jConfig.java
// 删除 AiAgentConfig.java（需要重写）
```

#### 4. 修改服务类

```java
// LangChain4j
@Service
public class AiService {
    private final ChatLanguageModel chatModel;
    
    public String chat(String prompt) {
        return chatModel.generate(prompt);
    }
}

// Spring AI
@Service
public class AiService {
    @Autowired
    private ChatClient chatClient;
    
    public String chat(String prompt) {
        return chatClient.call(prompt);
    }
}
```

#### 5. 修改工具调用

```java
// LangChain4j
public class PostTools {
    @Tool("查询用户风格")
    public String getUserStyle(Long userId) {
        return userService.getStyle(userId);
    }
}

// Spring AI
@Component
public class PostTools {
    @Bean
    @Description("查询用户风格")
    public Function<Long, String> getUserStyle() {
        return userId -> userService.getStyle(userId);
    }
}
```

---

## 选型建议

### 选择 Spring AI 的场景

- ✅ 新项目，使用Spring Boot
- ✅ 需要深度Spring生态集成
- ✅ 希望配置简单，开箱即用
- ✅ 团队熟悉Spring开发模式
- ✅ 需要使用Spring的其他AI相关功能

### 选择 LangChain4j 的场景

- ✅ 跨框架项目（非Spring）
- ✅ 需要更灵活的Agent构建
- ✅ 已有LangChain（Python）经验
- ✅ 需要更多自定义控制
- ✅ 项目已有LangChain4j代码

### 对比总结

| 维度 | Spring AI | LangChain4j |
|------|-----------|-------------|
| **配置复杂度** | ⭐⭐ 简单 | ⭐⭐⭐ 中等 |
| **学习曲线** | ⭐⭐ 平缓 | ⭐⭐⭐ 中等 |
| **灵活性** | ⭐⭐⭐ 中等 | ⭐⭐⭐⭐ 较高 |
| **Spring集成** | ⭐⭐⭐⭐⭐ 原生 | ⭐⭐⭐ 需适配 |
| **文档质量** | ⭐⭐⭐⭐ 优秀 | ⭐⭐⭐⭐ 良好 |
| **社区活跃度** | ⭐⭐⭐⭐ 高 | ⭐⭐⭐⭐ 高 |
| **更新频率** | ⭐⭐⭐⭐ 快 | ⭐⭐⭐⭐ 快 |

---

## 附录：常用API对照表

| 功能 | Spring AI | LangChain4j |
|------|-----------|-------------|
| 同步调用 | `chatClient.call(prompt)` | `chatModel.generate(prompt)` |
| 流式调用 | `chatClient.stream(prompt)` | `streamingModel.generate(prompt, handler)` |
| 带选项调用 | `new Prompt(prompt, options)` | 构建时配置 |
| 对话记忆 | `ChatMemory` + `Advisor` | `ChatMemoryProvider` |
| 工具定义 | `@Bean` + `@Description` | `@Tool` 注解 |
| 工具注册 | 配置文件或`ChatOptionsBuilder` | `AiServices.builder().tools()` |
| Prompt模板 | `PromptTemplate` | 字符串模板或注解 |
| RAG检索 | `QuestionAnswerAdvisor` | `ContentRetriever` |
| 向量存储 | `VectorStore` | `EmbeddingStore` |
| 向量嵌入 | `EmbeddingModel.embed()` | `EmbeddingModel.embed()` |
| 拦截器 | `RequestResponseAdvisor` | 需自行封装 |

---

*文档版本: 1.0*  
*更新时间: 2026-04-17*
