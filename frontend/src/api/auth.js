import http from './http'

// 登录
export function login(data) {
  return http.post('/auth/login', data)
}

// 注册
export function register(data) {
  return http.post('/auth/register', data)
}

// 获取当前用户信息
export function getUserInfo() {
  return http.get('/auth/me')
}

// 退出登录 (前端清除token即可，可选后端注销)
export function logout() {
  return http.post('/auth/logout')
}
