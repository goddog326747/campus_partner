# 项目设计文档 (Database & API)

## 1. 数据库设计 (MySQL)

> 基于 Spring Boot 后端，使用 MySQL 数据库。

### 1.1 用户表 (sys_user)
用户登录、注册、基本信息。

```sql
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码(加密)',
  nickname VARCHAR(50) DEFAULT '' COMMENT '昵称',
  avatar VARCHAR(255) DEFAULT '' COMMENT '头像URL',
  school VARCHAR(100) DEFAULT '' COMMENT '学校',
  major VARCHAR(100) DEFAULT '' COMMENT '专业',
  grade VARCHAR(20) DEFAULT '' COMMENT '年级(2021级等)',
  gender TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
  status TINYINT DEFAULT 1 COMMENT '状态 1正常 0禁用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 1.2 帖子表 (post)
发布的内容，包含分类、目的地等。

```sql
CREATE TABLE post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '发布者ID',
  title VARCHAR(100) NOT NULL COMMENT '标题',
  content TEXT NOT NULL COMMENT '内容',
  category VARCHAR(50) NOT NULL COMMENT '分区(游戏组队/漫展搭子/旅行出游...)',
  destination VARCHAR(100) DEFAULT '' COMMENT '目的地/地点',
  images TEXT COMMENT '图片列表(JSON数组或逗号分隔)',
  view_count INT DEFAULT 0 COMMENT '浏览量',
  status TINYINT DEFAULT 1 COMMENT '状态 1发布 0草稿 -1删除',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_id (user_id),
  INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';
```

### 1.3 评论表 (comment)
帖子下的评论。

```sql
CREATE TABLE comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  post_id BIGINT NOT NULL COMMENT '帖子ID',
  user_id BIGINT NOT NULL COMMENT '评论者ID',
  content VARCHAR(500) NOT NULL COMMENT '评论内容',
  parent_id BIGINT DEFAULT 0 COMMENT '父评论ID(回复)',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';
```

---

## 2. API 接口文档 (RESTful)

> Base URL: `/api`

### 2.1 认证模块 (Auth)

#### 登录
- **URL**: `POST /auth/login`
- **Body**: `{ "username": "admin", "password": "123" }`
- **Response**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "userInfo": { "id": 1, "nickname": "张三", "avatar": "..." }
    }
  }
  ```

#### 注册
- **URL**: `POST /auth/register`
- **Body**: `{ "username": "test", "password": "123", "nickname": "测试用户" }`
- **Response**: `{ "code": 200, "msg": "注册成功" }`

#### 获取当前用户信息
- **URL**: `GET /auth/me`
- **Header**: `Authorization: Bearer <token>`
- **Response**: `{ "code": 200, "data": { ...userInfo } }`

### 2.2 帖子模块 (Post)

#### 获取帖子列表 (分页/搜索)
- **URL**: `GET /posts`
- **Query**:
  - `page`: 页码 (默认1)
  - `size`: 每页条数 (默认10)
  - `keyword`: 搜索关键词 (标题/内容/地点)
  - `category`: 分区筛选
- **Response**:
  ```json
  {
    "code": 200,
    "data": {
      "total": 100,
      "records": [
        {
          "id": 1,
          "title": "周末去爬山",
          "category": "户外运动",
          "destination": "岳麓山",
          "author": { "nickname": "李四", "avatar": "..." },
          "createTime": "2023-10-01 10:00:00"
        }
      ]
    }
  }
  ```

#### 发布帖子
- **URL**: `POST /posts`
- **Header**: `Authorization: Bearer <token>`
- **Body**:
  ```json
  {
    "title": "求组队打王者",
    "content": "缺个辅助...",
    "category": "游戏组队",
    "destination": "线上",
    "images": ["http://.../1.jpg"]
  }
  ```
- **Response**: `{ "code": 200, "msg": "发布成功" }`

#### 获取帖子详情
- **URL**: `GET /posts/{id}`
- **Response**: `{ "code": 200, "data": { ...postDetail, "author": {...} } }`

### 2.3 评论模块 (Comment)

#### 获取评论列表
- **URL**: `GET /posts/{id}/comments`
- **Response**:
  ```json
  {
    "code": 200,
    "data": [
      {
        "id": 101,
        "content": "带我一个",
        "user": { "nickname": "王五", "avatar": "..." },
        "createTime": "2023-10-01 10:05:00"
      }
    ]
  }
  ```

#### 发表评论
- **URL**: `POST /posts/{id}/comments`
- **Header**: `Authorization: Bearer <token>`
- **Body**: `{ "content": "我也想去" }`
- **Response**: `{ "code": 200, "msg": "评论成功" }`

### 2.4 文件上传 (File)

#### 上传图片
- **URL**: `POST /upload/image`
- **Form-Data**: `file: (binary)`
- **Response**: `{ "code": 200, "data": "http://server/uploads/xxx.jpg" }`
