# Elasticsearch 检索优化实现说明

## 概述

本项目已集成 Elasticsearch 用于优化帖子检索功能，支持全文搜索、多条件筛选、智能提示等功能。

## 已实现的文件

### 1. 配置类
- `ElasticsearchConfig.java` - ES 配置类，启用仓库和异步支持
- `PostSyncInitializer.java` - 启动时自动同步数据到 ES

### 2. 文档实体
- `PostDocument.java` - ES 帖子文档实体类

### 3. Repository
- `PostSearchRepository.java` - ES 帖子搜索仓库接口

### 4. Service 层
- `PostSearchService.java` / `PostSearchServiceImpl.java` - 搜索服务
- `PostSyncService.java` / `PostSyncServiceImpl.java` - 数据同步服务

### 5. Controller
- `PostSearchController.java` - 搜索 API 接口

### 6. DTO
- `PostSearchRequest.java` - 搜索请求 DTO

### 7. 配置文件
- `application.yml` - 添加 ES 连接配置
- `elasticsearch/post-setting.json` - ES 索引设置
- `elasticsearch/post-mapping.json` - ES 字段映射

## API 接口

### 搜索接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/search/keyword` | GET | 关键词搜索 |
| `/api/search/advanced` | POST | 高级搜索（支持多条件筛选） |
| `/api/search/category/{category}` | GET | 按分类搜索 |
| `/api/search/destination` | GET | 按目的地搜索 |
| `/api/search/suggestions` | GET | 搜索建议 |
| `/api/search/hot-keywords` | GET | 热门搜索词 |

### 同步接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/search/sync/all` | POST | 全量同步所有帖子 |
| `/api/search/sync/{postId}` | POST | 同步单个帖子 |

## 使用示例

### 1. 关键词搜索
```bash
GET /api/search/keyword?keyword=旅游&pageNum=1&pageSize=10
```

### 2. 高级搜索
```bash
POST /api/search/advanced
Content-Type: application/json

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

### 3. 按分类搜索
```bash
GET /api/search/category/旅游?pageNum=1&pageSize=10
```

### 4. 搜索建议
```bash
GET /api/search/suggestions?keyword=北&size=10
```

## 数据同步机制

1. **自动同步**：创建帖子时自动同步到 ES
2. **自动删除**：删除帖子时自动从 ES 删除
3. **启动同步**：应用启动时自动全量同步（延迟10秒）
4. **手动同步**：可通过接口手动触发同步

## 注意事项

1. 确保 ES 服务已启动并可访问（http://192.168.192.128:9200）
2. 首次启动时会自动创建索引并同步数据
3. 如果 ES 连接失败，不会影响 MySQL 的正常操作
4. 支持中文分词（IK 分词器）

## 后续优化建议

1. 添加搜索日志记录，用于分析热门搜索词
2. 实现搜索结果缓存（Redis）
3. 添加搜索结果的个性化排序
4. 实现搜索结果的聚合统计
