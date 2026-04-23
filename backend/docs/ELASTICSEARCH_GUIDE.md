# Elasticsearch 检索功能开发指南

## 目录

1. [项目概述](#1-项目概述)
2. [环境搭建](#2-环境搭建)
3. [项目架构](#3-项目架构)
4. [核心代码详解](#4-核心代码详解)
5. [API 接口文档](#5-api-接口文档)
6. [测试验证](#6-测试验证)
7. [常见问题](#7-常见问题)

---

## 1. 项目概述

### 1.1 功能目标

为校园伙伴项目集成 Elasticsearch，实现帖子的全文检索功能，包括：

- **全文搜索**：支持标题、内容、目的地的关键词搜索
- **多条件筛选**：分类、地点、学校、认证状态、性别等
- **智能提示**：搜索关键词自动补全
- **数据同步**：MySQL 与 ES 数据自动同步

### 1.2 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.17 | 基础框架 |
| Spring Data Elasticsearch | 4.4.x | ES 集成 |
| Elasticsearch | 7.17.14 | 搜索引擎 |
| IK 分词器 | 内置 | 中文分词 |

---

## 2. 环境搭建

### 2.1 安装 Elasticsearch

使用 Docker 安装 ES：

```bash
# 使用华为云镜像（国内可访问）
docker run -d \
  --name elasticsearch \
  --restart always \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "cluster.name=docker-cluster" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -e "xpack.security.enabled=false" \
  -v es-data:/usr/share/elasticsearch/data \
  swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/elasticsearch:7.17.14
```

### 2.2 验证安装

```bash
# 查看容器状态
docker ps | grep elasticsearch

# 测试 ES 是否启动成功
curl http://localhost:9200
```

预期输出：

```json
{
  "name" : "e532cd0878d2",
  "cluster_name" : "docker-cluster",
  "version" : {
    "number" : "7.17.14"
  },
  "tagline" : "You Know, for Search"
}
```

---

## 3. 项目架构

### 3.1 文件结构

```
backend/
├── pom.xml                                    # 添加 ES 依赖
├── src/main/resources/
│   ├── application.yml                        # ES 连接配置
│   └── elasticsearch/
│       ├── post-setting.json                  # 索引设置
│       └── post-mapping.json                  # 字段映射
├── src/main/java/com/example/project/
│   ├── config/
│   │   └── ElasticsearchConfig.java           # ES 配置类
│   ├── component/
│   │   └── PostSyncInitializer.java           # 启动同步器
│   ├── document/
│   │   └── PostDocument.java                  # ES 文档实体
│   ├── repository/
│   │   └── PostSearchRepository.java          # ES 仓库接口
│   ├── service/
│   │   ├── PostSearchService.java             # 搜索服务接口
│   │   ├── PostSyncService.java               # 同步服务接口
│   │   └── impl/
│   │       ├── PostSearchServiceImpl.java     # 搜索服务实现
│   │       └── PostSyncServiceImpl.java       # 同步服务实现
│   ├── dto/
│   │   └── PostSearchRequest.java             # 搜索请求 DTO
│   └── controller/
│       └── PostSearchController.java          # 搜索控制器
```

### 3.2 架构流程图

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   用户请求   │────▶│  Controller │────▶│   Service   │
└─────────────┘     └─────────────┘     └──────┬──────┘
                                                │
                       ┌────────────────────────┘
                       ▼
              ┌─────────────────┐
              │  Elasticsearch  │
              │   (搜索引擎)     │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │     MySQL       │
              │   (主数据库)     │
              └─────────────────┘
```

---

## 4. 核心代码详解

### 4.1 添加依赖 (pom.xml)

```xml
<!-- Elasticsearch -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

### 4.2 配置文件 (application.yml)

```yaml
spring:
  elasticsearch:
    uris: http://192.168.192.128:9200
    connection-timeout: 10s
    socket-timeout: 30s
```

### 4.3 ES 配置类

```java
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.example.project.repository")
@EnableAsync
public class ElasticsearchConfig {
}
```

**关键点**：
- `@EnableElasticsearchRepositories`：启用 ES 仓库扫描
- `@EnableAsync`：启用异步支持，数据同步不阻塞主线程

### 4.4 文档实体类 (PostDocument.java)

```java
@Data
@Document(indexName = "post")
@Setting(settingPath = "elasticsearch/post-setting.json")
@Mapping(mappingPath = "elasticsearch/post-mapping.json")
public class PostDocument {

    @Id
    @Field(type = FieldType.Long)
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Keyword)
    private String category;

    // ... 其他字段
}
```

**注解说明**：

| 注解 | 作用 |
|------|------|
| `@Document(indexName = "post")` | 指定索引名称 |
| `@Setting` | 指定索引设置文件（分片、副本、分词器等） |
| `@Mapping` | 指定字段映射文件 |
| `@Field(type = FieldType.Text)` | 文本类型，支持全文检索 |
| `@Field(type = FieldType.Keyword)` | 关键字类型，精确匹配 |
| `analyzer = "ik_max_word"` | 索引时使用 IK 最大粒度分词 |
| `searchAnalyzer = "ik_smart"` | 搜索时使用 IK 智能分词 |

### 4.5 索引设置 (post-setting.json)

```json
{
  "index": {
    "number_of_shards": 1,
    "number_of_replicas": 0,
    "max_result_window": 10000
  },
  "analysis": {
    "analyzer": {
      "ik_max_word": {
        "type": "custom",
        "tokenizer": "ik_max_word",
        "filter": ["lowercase"]
      }
    }
  }
}
```

**配置说明**：
- `number_of_shards`: 分片数，单机环境设为 1
- `number_of_replicas`: 副本数，单机环境设为 0
- `max_result_window`: 最大返回结果数
- `ik_max_word`: IK 分词器配置

### 4.6 字段映射 (post-mapping.json)

```json
{
  "properties": {
    "title": {
      "type": "text",
      "analyzer": "ik_max_word",
      "search_analyzer": "ik_smart",
      "fields": {
        "keyword": {
          "type": "keyword",
          "ignore_above": 256
        }
      }
    },
    "category": {
      "type": "keyword"
    }
  }
}
```

**字段类型对比**：

| 类型 | 特点 | 适用场景 |
|------|------|----------|
| `text` | 分词存储，支持全文检索 | 标题、内容、描述 |
| `keyword` | 不分词，精确匹配 | 分类、标签、ID |
| `date` | 日期格式 | 创建时间、更新时间 |
| `integer` | 整数 | 计数、状态码 |

### 4.7 Repository 接口

```java
@Repository
public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, Long> {

    // 方法名派生查询
    Page<PostDocument> findByCategory(String category, Pageable pageable);

    // 自定义查询
    @Query("{" +
            "  \"multi_match\": {" +
            "    \"query\": \"?0\"," +
            "    \"fields\": [\"title^3\", \"content\", \"destination^2\"]" +
            "  }" +
            "}")
    Page<PostDocument> searchByKeyword(String keyword, Pageable pageable);
}
```

**查询方式**：

1. **方法名派生**：Spring Data 自动实现，如 `findByCategory`
2. **@Query 注解**：自定义 JSON 查询语句
3. **ElasticsearchRestTemplate**：复杂动态查询

### 4.8 数据同步服务

```java
@Service
public class PostSyncServiceImpl implements PostSyncService {

    @Autowired
    private PostSearchRepository postSearchRepository;

    @Override
    @Async  // 异步执行
    public void syncPost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post != null) {
            PostDocument document = convertToDocument(post);
            postSearchRepository.save(document);
        }
    }

    @Override
    public void syncAllPosts() {
        List<Post> allPosts = postMapper.selectList(null);
        // 批量同步
        int batchSize = 100;
        for (int i = 0; i < allPosts.size(); i += batchSize) {
            List<Post> batch = allPosts.subList(i, Math.min(i + batchSize, allPosts.size()));
            syncPosts(batch);
        }
    }
}
```

**同步时机**：

| 时机 | 实现方式 |
|------|----------|
| 创建帖子 | PostServiceImpl.createPost() 中调用 |
| 删除帖子 | PostServiceImpl.deletePost() 中调用 |
| 启动同步 | PostSyncInitializer 延迟 10 秒执行 |
| 手动同步 | 通过 /api/search/sync/all 接口 |

### 4.9 搜索服务实现

```java
@Service
public class PostSearchServiceImpl implements PostSearchService {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Page<PostDocument> advancedSearch(PostSearchRequest request) {
        // 构建 Bool 查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 关键词搜索（must）
        if (StringUtils.hasText(request.getKeyword())) {
            boolQuery.must(QueryBuilders.multiMatchQuery(request.getKeyword())
                    .field("title", 3.0f)      // 标题权重 3
                    .field("content")           // 内容权重 1
                    .field("destination", 2.0f) // 目的地权重 2
                    .type("best_fields")
                    .fuzziness("AUTO"));        // 模糊匹配
        }

        // 筛选条件（filter，不参与评分）
        if (StringUtils.hasText(request.getCategory())) {
            boolQuery.filter(QueryBuilders.termQuery("category", request.getCategory()));
        }

        // 构建排序
        SortOrder sortOrder = "asc".equalsIgnoreCase(request.getSortOrder()) ? 
                SortOrder.ASC : SortOrder.DESC;

        // 执行查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(request.getPageNum() - 1, request.getPageSize()))
                .withSort(SortBuilders.fieldSort(request.getSortField()).order(sortOrder))
                .build();

        SearchHits<PostDocument> searchHits = 
                elasticsearchRestTemplate.search(searchQuery, PostDocument.class);

        // 转换结果
        List<PostDocument> documents = searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());

        return new PageImpl<>(documents, pageable, searchHits.getTotalHits());
    }
}
```

**查询构建要点**：

- `must`：必须满足，参与评分
- `filter`：必须满足，不参与评分（性能更好）
- `should`：至少满足一个
- `field("title", 3.0f)`：设置字段权重，提升相关性

---

## 5. API 接口文档

### 5.1 搜索接口

#### 关键词搜索

```http
GET /api/search/keyword?keyword={关键词}&pageNum=1&pageSize=10
```

**参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 是 | 搜索关键词 |
| pageNum | int | 否 | 页码，默认 1 |
| pageSize | int | 否 | 每页大小，默认 10 |

**示例**：

```bash
curl "http://localhost:8080/api/search/keyword?keyword=北京旅游&pageNum=1&pageSize=10"
```

#### 高级搜索

```http
POST /api/search/advanced
Content-Type: application/json
```

**请求体**：

```json
{
  "keyword": "北京",
  "category": "旅游",
  "location": "上海",
  "school": "复旦大学",
  "verified": true,
  "gender": 1,
  "pageNum": 1,
  "pageSize": 10,
  "sortField": "createTime",
  "sortOrder": "desc"
}
```

**参数说明**：

| 参数 | 类型 | 说明 |
|------|------|------|
| keyword | string | 关键词，搜索标题、内容、目的地 |
| category | string | 分类筛选 |
| location | string | 用户所在地筛选 |
| school | string | 用户学校筛选 |
| verified | boolean | 是否认证用户 |
| gender | int | 性别：0-女，1-男 |
| sortField | string | 排序字段：createTime/updateTime/likeCount/commentCount |
| sortOrder | string | 排序方式：asc/desc |

#### 分类搜索

```http
GET /api/search/category/{category}?pageNum=1&pageSize=10
```

#### 目的地搜索

```http
GET /api/search/destination?destination={目的地}&pageNum=1&pageSize=10
```

#### 搜索建议

```http
GET /api/search/suggestions?keyword={前缀}&size=10
```

**示例**：

```bash
curl "http://localhost:8080/api/search/suggestions?keyword=北&size=5"
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": ["北京旅游", "北海公园", "北戴河"]
}
```

### 5.2 同步接口

#### 全量同步

```http
POST /api/search/sync/all
```

**说明**：将 MySQL 中所有帖子同步到 ES，用于首次初始化或数据修复

#### 单条同步

```http
POST /api/search/sync/{postId}
```

**说明**：同步指定帖子到 ES

---

## 6. 测试验证

### 6.1 启动项目

```bash
# 编译
mvn clean compile

# 启动
mvn spring-boot:run
```

### 6.2 验证步骤

1. **检查 ES 连接**

   查看日志是否有 `Post sync initialization completed`

2. **检查索引创建**

   ```bash
   curl http://192.168.192.128:9200/_cat/indices
   ```

   应看到 `post` 索引

3. **检查数据同步**

   ```bash
   curl http://192.168.192.128:9200/post/_count
   ```

   返回文档数应与 MySQL 帖子数一致

4. **测试搜索**

   ```bash
   curl "http://localhost:8080/api/search/keyword?keyword=测试"
   ```

### 6.3 性能测试

使用 JMeter 或 Postman 进行压测：

```bash
# 并发搜索测试
ab -n 1000 -c 50 "http://localhost:8080/api/search/keyword?keyword=北京"
```

---

## 7. 常见问题

### Q1: ES 连接失败

**现象**：启动时报 `Connection refused`

**解决**：

1. 检查 ES 是否启动：`curl http://192.168.192.128:9200`
2. 检查防火墙：`firewall-cmd --list-ports`
3. 检查配置：`application.yml` 中的 IP 和端口

### Q2: 中文分词不生效

**现象**：搜索"北京"匹配不到"北京大学"

**解决**：

1. 确认使用了 IK 分词器
2. 检查 mapping 中 `analyzer` 配置
3. 重建索引：删除后重启应用

### Q3: 数据不同步

**现象**：MySQL 有数据，ES 查不到

**解决**：

1. 手动触发同步：`POST /api/search/sync/all`
2. 检查日志是否有同步错误
3. 确认 `PostSyncServiceImpl` 被正确调用

### Q4: 搜索结果排序不对

**现象**：相关性高的排在后面

**解决**：

1. 检查是否使用了 `filter`（不参与评分）
2. 调整字段权重：`.field("title", 3.0f)`
3. 添加 `sort` 参数按评分排序

### Q5: 内存不足

**现象**：ES 容器频繁重启

**解决**：

```bash
# 调整 JVM 内存
docker run -e "ES_JAVA_OPTS=-Xms256m -Xmx256m" ...
```

---

## 附录

### A. IK 分词器说明

IK 分词器提供两种分词模式：

- **ik_max_word**：细粒度模式，会分出尽可能多的词
  - 示例："中华人民共和国" → "中华人民共和国", "中华人民", "中华", "华人", "人民共和国"...
  
- **ik_smart**：智能模式，做最粗粒度的拆分
  - 示例："中华人民共和国" → "中华人民共和国"

**使用建议**：
- 索引时使用 `ik_max_word`，尽可能多匹配
- 搜索时使用 `ik_smart`，提高精确度

### B. 查询类型对比

| 查询类型 | 说明 | 示例 |
|----------|------|------|
| `match` | 分词后匹配 | `matchQuery("title", "北京")` |
| `term` | 精确匹配 | `termQuery("category", "旅游")` |
| `multi_match` | 多字段匹配 | `multiMatchQuery("北京", "title", "content")` |
| `bool` | 组合查询 | `boolQuery().must().filter()` |
| `prefix` | 前缀匹配 | `prefixQuery("title", "北")` |
| `fuzzy` | 模糊匹配 | `fuzzyQuery("title", "beijin")` |

### C. 参考文档

- [Elasticsearch 官方文档](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/index.html)
- [Spring Data Elasticsearch](https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/)
- [IK 分词器](https://github.com/medcl/elasticsearch-analysis-ik)

---

**文档版本**：v1.0  
**更新日期**：2026-04-19  
**作者**：AI Assistant
