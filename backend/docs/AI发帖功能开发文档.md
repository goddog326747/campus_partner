# AI 发帖功能开发文档

## 目录

1. [功能概述](#1-功能概述)
2. [技术架构](#2-技术架构)
3. [API 接口](#3-api-接口)
4. [代码结构](#4-代码结构)
5. [使用示例](#5-使用示例)
6. [后续扩展方向](#6-后续扩展方向)

---

## 1. 功能概述

### 1.1 功能说明

AI 发帖功能允许用户通过输入主题，让 AI 自动生成帖子内容并可选择直接发布。

### 1.2 核心能力

| 能力 | 说明 |
|------|------|
| 内容生成 | 根据主题自动生成标题和正文 |
| 分类支持 | 支持指定帖子分类 |
| 风格定制 | 支持指定生成风格 |
| 一键发布 | 生成后可直接发布到平台 |

---

## 2. 技术架构

### 2.1 技术栈

```
┌─────────────────────────────────────────────────────────────┐
│                     技术栈                                   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  框架层                                                      │
│  ├── Spring Boot 2.7.17                                    │
│  ├── LangChain4j 0.36.2                                    │
│  └── MyBatis-Plus 3.5.3.1                                  │
│                                                             │
│  AI 模型                                                     │
│  └── 阿里云通义千问 qwen3.5-plus                             │
│                                                             │
│  数据库                                                      │
│  └── MySQL                                                  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        AI 发帖架构                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   客户端请求                                                     │
│       │                                                         │
│       ▼                                                         │
│   ┌─────────────────┐                                          │
│   │ AiPostController │  接收请求，参数校验                       │
│   └────────┬────────┘                                          │
│            │                                                    │
│            ▼                                                    │
│   ┌─────────────────┐                                          │
│   │  AiPostService  │  业务逻辑处理                             │
│   └────────┬────────┘                                          │
│            │                                                    │
│            ├──────────────────────┐                             │
│            │                      │                             │
│            ▼                      ▼                             │
│   ┌─────────────────┐    ┌─────────────────┐                   │
│   │ChatLanguageModel│    │   PostService   │                   │
│   │   (LangChain4j)  │    │   (帖子存储)    │                   │
│   └────────┬────────┘    └────────┬────────┘                   │
│            │                      │                             │
│            ▼                      ▼                             │
│   ┌─────────────────┐    ┌─────────────────┐                   │
│   │   通义千问 API   │    │     MySQL      │                   │
│   └─────────────────┘    └─────────────────┘                   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 2.3 处理流程

```
用户输入主题
    │
    ▼
构建 Prompt（包含主题、分类、风格等）
    │
    ▼
调用 LangChain4j ChatLanguageModel
    │
    ▼
解析 AI 返回的 JSON 内容
    │
    ├──► 仅生成：返回生成结果
    │
    └──► 生成并发布：保存到数据库 → 返回结果（含帖子ID）
```

---

## 3. API 接口

### 3.1 生成帖子（不发布）

**请求**
```
POST /api/ai/post/generate
Content-Type: application/json
```

**请求体**
```json
{
    "topic": "周末一起去爬山",
    "category": "旅行远游",
    "destination": "黄山",
    "style": "轻松活泼"
}
```

**参数说明**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| topic | String | 是 | 帖子主题 |
| category | String | 否 | 帖子分类，可选值见下方 |
| destination | String | 否 | 目的地 |
| style | String | 否 | 生成风格 |

**分类可选值**
- 游戏组队
- 出游搭子
- 旅行远游
- 自习监督
- 运动健身
- 其他活动

**响应**
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "title": "周末黄山爬山搭子招募！",
        "content": "小伙伴们，周末有没有想去黄山爬山的？...",
        "category": "旅行远游",
        "destination": "黄山",
        "postId": null
    }
}
```

### 3.2 生成并发布帖子

**请求**
```
POST /api/ai/post/publish
Content-Type: application/json
```

**请求体**
```json
{
    "topic": "周末一起去爬山",
    "category": "旅行远游",
    "destination": "黄山",
    "style": "轻松活泼"
}
```

**响应**
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "title": "周末黄山爬山搭子招募！",
        "content": "小伙伴们，周末有没有想去黄山爬山的？...",
        "category": "旅行远游",
        "destination": "黄山",
        "postId": 123
    }
}
```

### 3.3 获取分类列表

**请求**
```
GET /api/ai/post/categories
```

**响应**
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "categories": [
            "游戏组队",
            "出游搭子",
            "旅行远游",
            "自习监督",
            "运动健身",
            "其他活动"
        ],
        "description": "可选的帖子分类列表"
    }
}
```

---

## 4. 代码结构

### 4.1 文件清单

```
backend/src/main/java/com/example/project/
├── config/
│   └── LangChain4jConfig.java      # LangChain4j 配置类
├── controller/
│   └── AiPostController.java       # AI 发帖控制器
├── dto/
│   ├── AiPostGenerateRequest.java  # 生成请求 DTO
│   └── AiPostGenerateResponse.java # 生成响应 DTO
└── service/
    ├── AiPostService.java          # 服务接口
    └── impl/
        └── AiPostServiceImpl.java  # 服务实现
```

### 4.2 核心类说明

#### LangChain4jConfig.java

配置 LangChain4j 的 ChatLanguageModel Bean，连接阿里云通义千问 API。

```java
@Configuration
public class LangChain4jConfig {
    
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(model)
                .timeout(Duration.ofSeconds(60))
                .temperature(0.7)
                .maxTokens(2000)
                .build();
    }
}
```

#### AiPostServiceImpl.java

核心业务逻辑：
1. 构建 Prompt
2. 调用 AI 模型
3. 解析响应
4. 可选：保存到数据库

---

## 5. 使用示例

### 5.1 cURL 示例

```bash
# 生成帖子
curl -X POST http://localhost:8080/api/ai/post/generate \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "周末一起打羽毛球",
    "category": "运动健身",
    "style": "热情邀请"
  }'

# 生成并发布
curl -X POST http://localhost:8080/api/ai/post/publish \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "寻找自习搭子",
    "category": "自习监督",
    "destination": "图书馆",
    "style": "认真严肃"
  }'
```

### 5.2 JavaScript 示例

```javascript
// 生成帖子
async function generatePost(topic, category, style) {
    const response = await fetch('/api/ai/post/generate', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            topic: topic,
            category: category,
            style: style
        })
    });
    
    return await response.json();
}

// 使用示例
const result = await generatePost('周末爬山', '旅行远游', '轻松活泼');
console.log(result.data.title);
console.log(result.data.content);
```

---

## 6. 后续扩展方向

### 6.1 短期扩展（1-2周）

#### 6.1.1 敏感词过滤

```java
@Service
public class SensitiveWordService {
    
    public String filter(String content) {
        // 实现敏感词过滤逻辑
        return filteredContent;
    }
}

// 在 AiPostServiceImpl 中集成
@Override
public AiPostGenerateResponse generateAndPublishPost(AiPostGenerateRequest request) {
    AiPostGenerateResponse response = generatePost(request);
    
    // 敏感词过滤
    response.setContent(sensitiveWordService.filter(response.getContent()));
    
    // ... 发布逻辑
}
```

#### 6.1.2 内容审核

```java
@Service
public class ContentReviewService {
    
    public ReviewResult review(String content) {
        // 调用内容审核 API 或本地规则
        return new ReviewResult(approved, issues);
    }
}
```

#### 6.1.3 流式输出

```java
// 支持流式生成，提升用户体验
public Flux<String> generatePostStream(AiPostGenerateRequest request) {
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
        });
    });
}
```

### 6.2 中期扩展（1-2月）

#### 6.2.1 Tool Calling 集成

让 AI 能够调用更多工具：

```java
public class PostTools {
    
    @Tool("查询用户历史帖子风格")
    public String getUserPostStyle(Long userId) {
        // 分析用户历史帖子风格
        return style;
    }
    
    @Tool("获取热门话题")
    public List<String> getHotTopics() {
        // 返回当前热门话题
        return topics;
    }
    
    @Tool("推荐标签")
    public List<String> suggestTags(String content) {
        // 根据内容推荐标签
        return tags;
    }
}

// 组装 Agent
AiServices.builder(PostAssistant.class)
    .chatLanguageModel(model)
    .tools(new PostTools())
    .build();
```

#### 6.2.2 RAG 增强

基于知识库生成更精准的内容：

```java
@Service
public class RagPostService {
    
    private final EmbeddingStore<TextSegment> vectorStore;
    private final EmbeddingModel embeddingModel;
    
    public String generateWithContext(String topic) {
        // 1. 检索相关优质帖子
        List<TextSegment> relevantPosts = searchRelevantPosts(topic);
        
        // 2. 构建带上下文的 Prompt
        String context = buildContext(relevantPosts);
        String prompt = "参考以下优质帖子风格：\n" + context + "\n\n生成主题为：" + topic;
        
        // 3. 生成
        return model.generate(prompt);
    }
}
```

#### 6.2.3 多轮对话优化

支持通过对话逐步完善帖子：

```java
@Service
public class ConversationalPostService {
    
    private final ChatMemory chatMemory;
    
    public String refinePost(String sessionId, String userFeedback) {
        // 记住之前的生成内容
        // 根据用户反馈修改
        // 支持多轮优化
    }
}
```

### 6.3 长期扩展（3-6月）

#### 6.3.1 多智能体协作

```
┌─────────────────────────────────────────────────────────────┐
│                    多智能体发帖系统                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌─────────────┐                                          │
│   │   用户请求   │                                          │
│   └──────┬──────┘                                          │
│          │                                                  │
│          ▼                                                  │
│   ┌─────────────┐                                          │
│   │  内容生成器  │ 生成初稿                                  │
│   └──────┬──────┘                                          │
│          │                                                  │
│          ▼                                                  │
│   ┌─────────────┐                                          │
│   │   内容审核员  │ 检查敏感词、合规性                         │
│   └──────┬──────┘                                          │
│          │                                                  │
│          ▼                                                  │
│   ┌─────────────┐                                          │
│   │   风格优化师  │ 优化表达、增加吸引力                       │
│   └──────┬──────┘                                          │
│          │                                                  │
│          ▼                                                  │
│   ┌─────────────┐                                          │
│   │   最终发布   │                                          │
│   └─────────────┘                                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

#### 6.3.2 MCP 协议支持

让外部 AI 客户端（如 Claude Desktop）能够调用发帖功能：

```java
// MCP Server 实现
@McpTool(name = "create_post", description = "创建帖子")
public McpToolResult createPost(
    @McpParam(name = "title") String title,
    @McpParam(name = "content") String content
) {
    Long id = postService.create(title, content);
    return McpToolResult.success(Map.of("postId", id));
}
```

#### 6.3.3 个性化生成

根据用户画像定制内容：

```java
@Service
public class PersonalizedPostService {
    
    public String generateForUser(Long userId, String topic) {
        // 1. 获取用户画像
        UserProfile profile = userService.getProfile(userId);
        
        // 2. 分析用户偏好风格
        String preferredStyle = analyzeUserStyle(profile);
        
        // 3. 个性化生成
        return generateWithStyle(topic, preferredStyle);
    }
}
```

### 6.4 扩展路线图

```
当前版本 v1.0
├── 基础 AI 生成
├── 分类支持
└── 一键发布

    ↓

v1.1（短期）
├── 敏感词过滤
├── 内容审核
└── 流式输出

    ↓

v1.5（中期）
├── Tool Calling
├── RAG 增强
└── 多轮对话

    ↓

v2.0（长期）
├── 多智能体协作
├── MCP 协议
└── 个性化生成
```

---

## 附录

### A. 配置说明

```yaml
# application.yml
aliyun:
  ai:
    api-key: your-api-key        # 阿里云 API Key
    api-url: https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
    model: qwen3.5-plus          # 模型名称
```

### B. 依赖说明

```xml
<!-- pom.xml -->
<properties>
    <langchain4j.version>0.36.2</langchain4j.version>
</properties>

<dependencies>
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j</artifactId>
        <version>${langchain4j.version}</version>
    </dependency>
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j-open-ai</artifactId>
        <version>${langchain4j.version}</version>
    </dependency>
</dependencies>
```

### C. 常见问题

**Q: 生成速度慢怎么办？**
A: 可以考虑使用流式输出，或者使用更快的模型（如 qwen-turbo）。

**Q: 如何切换其他 AI 模型？**
A: 修改 LangChain4jConfig 中的配置，或添加新的 Model Bean。

**Q: 如何处理生成失败？**
A: 已实现 fallback 机制，解析失败时会返回原始内容作为帖子内容。
