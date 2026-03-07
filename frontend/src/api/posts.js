import http from './http'

export function listPosts(params) {
  // Backend endpoint: /api/post/list
  return http.get('/post/list', { params })
}

export function getCategories() {
  // Backend endpoint: /api/post/categories
  return http.get('/post/categories')
}

export function createPost(data) {
  // Backend endpoint: /api/post/create
  return http.post('/post/create', data)
}

export function getPost(id) {
  // Backend endpoint: /api/post/detail?id=... or similar. 
  // Wait, I haven't implemented detail endpoint in backend yet. 
  // But let's keep it for now or comment it out if not used yet.
  // Actually I should probably add a detail endpoint in backend controller.
  return http.get(`/post/detail/${id}`)
}
