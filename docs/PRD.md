# 项目需求文档 (PRD)

## 1. 项目简介
**项目名称**：校园搭子平台 (Campus Partner)
**项目愿景**：打造一个专注于大学生群体的活动策划与搭子匹配平台，解决大学生在游戏组队、漫展出游、旅行、自习监督、运动健身等方面的社交需求。

## 2. 系统架构
- **前端**：Vue 3 + Vite + Element Plus
- **后端**：Spring Boot 2.7.x + MyBatis Plus + Shiro + JWT
- **数据库**：MySQL
- **AI服务**：阿里云通义千问 (Qwen)

## 3. 功能模块

### 3.1 用户模块
- **注册/登录**：支持用户名/密码登录，使用 JWT 进行会话管理。
- **用户信息**：管理用户昵称、头像、学校等信息（当前使用 ThreadLocal 存储上下文）。

### 3.2 帖子模块 (核心)
- **发布帖子**：
  - 支持标题、内容文本输入。
  - **分类选择**：游戏组队、漫展搭子、旅行出游、自习监督、运动健身、其他活动。
  - 图片上传（前端已预留，后端待实现存储）。
- **帖子列表**：
  - **分类筛选**：顶部 Tab 切换不同活动分类。
  - **搜索功能**：支持按关键词搜索帖子标题和内容。
  - **展示信息**：标题、作者、分类标签、发布时间、摘要。
- **帖子详情**：
  - 展示完整内容、图片轮播。
  - 评论区（查看评论、发表评论）。

### 3.3 AI 助手
- **活动策划**：基于阿里云 Qwen 模型，为用户提供活动策划建议（如旅游攻略、漫展路线、游戏组队建议）。
- **角色设定**：贴心的大学生活动策划助手。

## 4. 数据库设计

### 4.1 用户表 (sys_user)
| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| id | bigint | 主键 |
| username | varchar | 用户名 |
| password | varchar | 密码 |
| nickname | varchar | 昵称 |
| avatar | varchar | 头像URL |

### 4.2 帖子表 (sys_post)
| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| id | bigint | 主键 |
| title | varchar | 标题 |
| content | text | 内容 |
| category | varchar | 分类 |
| user_id | bigint | 发布者ID |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

## 5. 环境配置

### 5.1 数据库配置 (MySQL)
- **Host**: 192.168.192.128
- **Port**: 13306
- **Database**: campus_partner
- **Username**: root
- **Password**: 123456
- **Connection Pool**: HikariCP (Min: 15, Max: 25, Idle Timeout: 3m, Max Lifetime: 30m)

### 5.2 Redis 配置 (可选/待启用)
- **Host**: 192.168.192.128
- **Port**: 16379
- **Pool**: Min 5, Max 10

### 5.3 AI 服务配置
- **Provider**: Aliyun DashScope
- **Model**: qwen-plus
