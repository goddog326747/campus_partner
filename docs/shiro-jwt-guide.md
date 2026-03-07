# Shiro + JWT 登录集成说明

## 1. 简介
本方案采用 **Shiro** 进行权限控制，**JWT (JSON Web Token)** 进行无状态会话管理。
- **Shiro**: 负责认证（Authentication）和授权（Authorization）。
- **JWT**: 用于在客户端和服务器之间传递安全可靠的信息，替代传统的 Session。

## 2. 核心流程
1. **登录**: 用户请求 `/api/auth/login`，后端验证账号密码通过后，生成 JWT Token 返回给前端。
2. **请求**: 前端将 Token 存储在 `localStorage`，并在后续请求的 Header 中携带 `Authorization: Bearer <token>`。
3. **拦截**: 后端 `JwtFilter` 拦截请求，提取 Token。
4. **认证**: `JwtFilter` 将 Token 封装为 `JwtToken` 提交给 `UserRealm`。
5. **鉴权**: `UserRealm` 校验 Token 有效性，并根据 Token 中的用户信息（如用户名）查询权限信息。

## 3. 关键类说明

### 3.1 JwtUtils (工具类)
- `sign(username)`: 生成 Token，包含用户名和过期时间。
- `verify(token)`: 校验 Token 签名是否正确。
- `getUsername(token)`: 从 Token 中解析用户名。

### 3.2 JwtToken (Token封装)
- 实现 `AuthenticationToken` 接口，替代 Shiro 默认的 `UsernamePasswordToken`。

### 3.3 JwtFilter (过滤器)
- 继承 `BasicHttpAuthenticationFilter`。
- `isAccessAllowed`: 拦截请求，判断是否需要登录。
- `executeLogin`: 提取 Header 中的 Token 并执行 Shiro 登录。

### 3.4 UserRealm (核心Realm)
- `doGetAuthenticationInfo`: 认证逻辑。校验 Token 是否合法。
- `doGetAuthorizationInfo`: 授权逻辑。根据 Token 解析出的用户，查询数据库获取角色和权限。

### 3.5 ShiroConfig (配置类)
- 关闭 Shiro 自带 Session (`sessionStorageEnabled(false)`).
- 配置 `SecurityManager` 使用自定义 Realm。
- 配置 `ShiroFilterFactoryBean` 使用自定义 Filter，并设置拦截规则（如 `/api/auth/**` 放行）。

## 4. 前端对接 (Vue)
前端代码已在 `src/api/http.js` 和 `src/store/index.js` 中完成适配：
- **请求头**: `config.headers.Authorization = 'Bearer ' + token`
- **响应处理**: 遇到 401 错误自动清除 Token 并跳转登录。

## 5. 运行后端
请确保 `backend` 目录是一个完整的 Spring Boot 项目（包含 Application 启动类），配置好 Maven 依赖后即可运行。
