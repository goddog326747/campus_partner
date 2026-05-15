import http from './http'

export function login(data) {
  return http.post('/auth/login', data)
}

export function register(data) {
  return http.post('/auth/register', data)
}

export function getUserInfo() {
  return http.get('/auth/me')
}

export function logout() {
  return http.post('/auth/logout')
}

export function getUserProfile() {
  return http.get('/user/profile')
}

export function updateProfile(data) {
  return http.put('/user/profile', data)
}

export function updateAvatar(data) {
  return http.put('/user/avatar', data)
}

export function updatePassword(data) {
  return http.put('/user/password', data)
}

/**
 * 上传头像文件
 * @param {FormData} formData 包含 file 字段的 FormData 对象
 * @returns {Promise} 返回文件URL
 */
export function uploadAvatarFile(formData) {
  return http.post('/file/upload/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
