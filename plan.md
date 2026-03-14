# Redis优化计划

## 一、Redis客户端选择

### 推荐使用 Redisson

**对比分析：**

| 客户端 | 优点 | 缺点 |
|--------|------|------|
| **Redisson** | API清晰、功能丰富、支持分布式锁/布隆过滤器、Spring Boot集成好 | 依赖较重 |
| Lettuce | Spring Boot默认、异步支持好 | API较底层 |
| Jedis | 简单直接 | 不支持异步、线程不安全 |

**选择Redisson的原因：**
1. API设计优雅，提供丰富的分布式对象（Map、Set、List等）
2. 内置分布式锁，可用于防重复提交、防并发问题
3. 支持布隆过滤器，可用于过滤已存在的数据
4. 与Spring Boot无缝集成，配置简单

---

## 二、Redis配置

### 2.1 添加依赖 (pom.xml)
```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.27.0</version>
</dependency>
```

### 2.2 配置文件 (application.yml)
```yaml
spring:
  redis:
    host: 192.168.192.128
    port: 16379
    timeout: 5000

# Redisson配置
redisson:
  config: |
    singleServerConfig:
      address: "redis://192.168.192.128:16379"
      connectionPoolSize: 20
      connectionMinimumIdleSize: 5
      idleConnectionTimeout: 10000
      connectTimeout: 10000
      timeout: 3000
```

### 2.3 配置类 (RedissonConfig.java)
- 创建RedissonClient Bean
- 配置序列化器

---

## 三、缓存优化方案

### 3.1 帖子列表缓存（核心优化）

**场景：** 帖子列表查询频繁，筛选条件多样

**方案：**
```
缓存Key设计：
- post:list:all                    # 全部帖子列表
- post:list:category:{category}    # 按分类缓存
- post:list:hot                    # 热门帖子列表

缓存策略：
- 过期时间：5分钟
- 写入策略：Cache-Aside（先查缓存，miss则查库并写入）
- 失效策略：帖子创建/更新/删除时清除相关缓存
```

### 3.2 用户信息缓存

**场景：** 用户信息频繁查询（帖子作者、评论用户等）

**方案：**
```
缓存Key设计：
- user:info:{userId}    # 用户基本信息

缓存策略：
- 过期时间：30分钟
- 用户信息更新时清除缓存
```

### 3.3 分类数据缓存

**场景：** 分类列表几乎不变

**方案：**
```
缓存Key设计：
- post:categories    # 分类列表

缓存策略：
- 过期时间：1小时
- 永久缓存，手动失效
```

### 3.4 帖子详情缓存

**场景：** 热门帖子访问频繁

**方案：**
```
缓存Key设计：
- post:detail:{postId}    # 帖子详情

缓存策略：
- 过期时间：10分钟
- 帖子更新/删除时清除
```

---

## 四、高级功能应用

### 4.1 分布式锁

**场景1：防止重复提交**
```java
// 发帖时获取锁，防止重复提交
RLock lock = redissonClient.getLock("post:create:" + userId);
```

**场景2：防止并发修改**
```java
// 更新用户信息时加锁
RLock lock = redissonClient.getLock("user:update:" + userId);
```

### 4.2 布隆过滤器

**场景：判断用户名是否存在**
```java
RBloomFilter<String> usernameFilter = redissonClient.getBloomFilter("user:usernames");
// 注册时快速判断用户名是否存在
```

### 4.3 计数器

**场景：帖子浏览量**
```java
RAtomicLong viewCount = redissonClient.getAtomicLong("post:views:" + postId);
// 定时同步到数据库
```

---

## 五、实施步骤

### 第一阶段：基础配置
1. 添加Redisson依赖
2. 配置Redis连接
3. 创建RedissonConfig配置类
4. 创建RedisService工具类

### 第二阶段：核心缓存
1. 实现帖子列表缓存
2. 实现用户信息缓存
3. 实现分类数据缓存
4. 实现帖子详情缓存

### 第三阶段：高级功能
1. 实现分布式锁（防重复提交）
2. 实现布隆过滤器（用户名检查）
3. 实现计数器（浏览量统计）

### 第四阶段：缓存维护
1. 实现缓存失效机制
2. 添加缓存监控
3. 性能测试与调优

---

## 六、文件清单

### 新增文件
- `config/RedissonConfig.java` - Redisson配置类
- `service/RedisService.java` - Redis操作服务
- `service/impl/RedisServiceImpl.java` - Redis服务实现

### 修改文件
- `pom.xml` - 添加Redisson依赖
- `application.yml` - 添加Redis配置
- `PostServiceImpl.java` - 添加缓存逻辑
- `UserServiceImpl.java` - 添加缓存逻辑
- `PostController.java` - 添加缓存清除逻辑
- `UserController.java` - 添加缓存清除逻辑

---

## 七、注意事项

1. **缓存穿透**：对于不存在的数据，缓存空值或使用布隆过滤器
2. **缓存雪崩**：设置随机过期时间，避免同时失效
3. **缓存击穿**：使用分布式锁保护热点数据
4. **数据一致性**：采用Cache-Aside模式，先更新数据库再删除缓存
5. **内存管理**：合理设置过期时间，避免内存溢出
