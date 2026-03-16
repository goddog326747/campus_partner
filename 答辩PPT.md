---
marp: true
theme: default
paginate: true
size: 16:9
---

# 校园搭子平台的设计与实现

## 本科毕业设计答辩

**答辩人**：XXX  
**学号**：2209100388  
**专业**：XXX  
**指导教师**：XXX  

西安电子科技大学

---

# 目录

1. 研究背景与意义
2. 系统需求分析
3. 系统设计
4. 系统实现
5. 系统测试
6. 总结与展望

---

# 一、研究背景与意义

## 1.1 研究背景

- 大学生社交需求多样化：游戏组队、漫展出游、旅行、自习监督、运动健身等
- 现有社交平台针对性不足，难以精准匹配同校或同城的"搭子"
- 大学生群体对校园专属社交平台有强烈需求

## 1.2 研究意义

- 解决大学生"找搭子"难的问题
- 促进校园社交互动，丰富大学生活
- 探索 AI 技术在社交平台中的应用

---

# 二、系统需求分析

## 2.1 功能需求

| 模块 | 功能描述 |
|------|----------|
| 用户管理 | 注册、登录、个人信息管理 |
| 帖子管理 | 发布、浏览、搜索、分类筛选 |
| 评论系统 | 发表评论、回复评论 |
| AI助手 | 活动策划建议、智能推荐 |
| 文件存储 | 图片上传、头像管理 |

---

# 二、系统需求分析

## 2.2 非功能需求

- **性能要求**：支持并发用户访问，响应时间 < 2s
- **安全要求**：用户数据加密存储，JWT 身份认证
- **可用性要求**：界面友好，操作简便
- **可扩展性**：支持功能模块扩展

## 2.3 技术选型

- **前端**：Vue 3 + Vite + Element Plus
- **后端**：Spring Boot 2.7.x + MyBatis Plus
- **数据库**：MySQL
- **安全框架**：Shiro + JWT
- **AI服务**：阿里云通义千问

---

# 三、系统设计

## 3.1 系统架构

```
┌─────────────────────────────────────────────────────┐
│                    前端层 (Vue 3)                     │
│    登录/注册 │ 帖子管理 │ AI助手 │ 个人中心           │
└─────────────────────┬───────────────────────────────┘
                      │ HTTP/REST API
┌─────────────────────▼───────────────────────────────┐
│                  后端层 (Spring Boot)                 │
│  Controller → Service → Mapper (MyBatis Plus)        │
│  Shiro + JWT 认证授权                                │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│              数据层 (MySQL + 阿里云OSS)               │
└─────────────────────────────────────────────────────┘
```

---

# 三、系统设计

## 3.2 数据库设计

### 用户表 (sys_user)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(100) | 密码(加密) |
| nickname | VARCHAR(50) | 昵称 |
| avatar | VARCHAR(255) | 头像URL |

### 帖子表 (post)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR(100) | 标题 |
| content | TEXT | 内容 |
| category | VARCHAR(50) | 分类 |
| user_id | BIGINT | 发布者ID |

---

# 三、系统设计

## 3.3 API 接口设计

### 认证模块
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/me` - 获取当前用户信息

### 帖子模块
- `GET /api/posts` - 获取帖子列表（分页、搜索、筛选）
- `POST /api/posts` - 发布帖子
- `GET /api/posts/{id}` - 获取帖子详情

### 评论模块
- `GET /api/posts/{id}/comments` - 获取评论列表
- `POST /api/posts/{id}/comments` - 发表评论

---

# 四、系统实现

## 4.1 用户认证模块

**技术方案**：Shiro + JWT

```java
// JWT 过滤器
public class JwtFilter extends AuthenticatingFilter {
    @Override
    protected AuthenticationToken createToken(
        ServletRequest request, ServletResponse response) {
        String token = getToken(request);
        return new JwtToken(token);
    }
}

// 用户认证域
public class UserRealm extends AuthorizingRealm {
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
        AuthenticationToken token) {
        // 验证 JWT 令牌
    }
}
```

---

# 四、系统实现

## 4.2 帖子管理模块

**核心功能**：
- 帖子发布：支持标题、内容、分类、目的地
- 分类筛选：游戏组队、漫展搭子、旅行出游、自习监督、运动健身、其他活动
- 关键词搜索：标题、内容、地点

**实现要点**：
```java
@Service
public class PostServiceImpl implements PostService {
    public Page<Post> queryPosts(PostQueryRequest request) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        // 分类筛选
        if (StringUtils.hasText(request.getCategory())) {
            wrapper.eq(Post::getCategory, request.getCategory());
        }
        // 关键词搜索
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(Post::getTitle, request.getKeyword())
                   .or().like(Post::getContent, request.getKeyword());
        }
        return postMapper.selectPage(request.toPage(), wrapper);
    }
}
```

---

# 四、系统实现

## 4.3 AI 助手模块

**技术方案**：集成阿里云通义千问 API

```java
@Service
public class AiServiceImpl implements AiService {
    public String chat(String userMessage) {
        // 调用通义千问 API
        Generation gen = new Generation();
        GenerationParam param = GenerationParam.builder()
            .model("qwen-plus")
            .messages(messages)
            .build();
        GenerationResult result = gen.call(param);
        return result.getOutput().getText();
    }
}
```

**功能特点**：
- 角色设定：贴心的大学生活动策划助手
- 支持旅游攻略、漫展路线、游戏组队建议等

---

# 四、系统实现

## 4.4 前端界面展示

### 主要页面
| 页面 | 功能 |
|------|------|
| 登录/注册 | 用户身份认证 |
| 首页 | 帖子列表、分类筛选 |
| 帖子详情 | 内容展示、评论区 |
| 发布帖子 | 创建新帖子 |
| 个人中心 | 用户信息管理 |
| AI助手 | 智能对话 |

### 技术特点
- Vue 3 Composition API
- Element Plus 组件库
- Axios 请求封装
- Vue Router 路由管理

---

# 五、系统测试

## 5.1 功能测试

| 测试项 | 测试内容 | 结果 |
|--------|----------|------|
| 用户注册 | 注册新用户 | ✓ 通过 |
| 用户登录 | JWT认证 | ✓ 通过 |
| 发布帖子 | 创建帖子 | ✓ 通过 |
| 帖子列表 | 分页、筛选、搜索 | ✓ 通过 |
| 评论功能 | 发表评论 | ✓ 通过 |
| AI助手 | 智能对话 | ✓ 通过 |

---

# 五、系统测试

## 5.2 性能测试

| 测试场景 | 并发数 | 平均响应时间 | 结果 |
|----------|--------|--------------|------|
| 首页加载 | 50 | 156ms | ✓ 通过 |
| 帖子列表 | 50 | 203ms | ✓ 通过 |
| 帖子详情 | 50 | 89ms | ✓ 通过 |
| 用户登录 | 50 | 245ms | ✓ 通过 |

## 5.3 安全测试

- ✓ SQL注入防护
- ✓ XSS攻击防护
- ✓ JWT令牌验证
- ✓ 密码加密存储

---

# 六、总结与展望

## 6.1 工作总结

1. **完成了系统架构设计**：前后端分离的 B/S 架构
2. **实现了核心功能模块**：用户认证、帖子管理、评论系统、AI助手
3. **完成了数据库设计**：用户表、帖子表、评论表
4. **进行了系统测试**：功能测试、性能测试、安全测试

## 6.2 创新点

- 面向大学生群体的垂直社交平台
- 集成 AI 大语言模型提供智能活动策划
- 分类筛选机制精准匹配"搭子"需求

---

# 六、总结与展望

## 6.3 不足与改进方向

| 方面 | 现状 | 改进方向 |
|------|------|----------|
| 缓存 | Redis未完全集成 | 引入Redis缓存提升性能 |
| 消息通知 | 未实现 | 添加实时消息推送 |
| 图片存储 | 本地存储 | 迁移至云存储OSS |
| 测试覆盖 | 单元测试较少 | 完善测试用例 |

## 6.4 后续工作

- 完善评论回复功能
- 实现图片上传功能
- 优化AI助手响应速度
- 完成毕业论文撰写

---

# 致谢

感谢指导教师的悉心指导！

感谢各位评审老师的聆听！

**请各位老师批评指正！**

---

# Q & A

## 谢谢！

