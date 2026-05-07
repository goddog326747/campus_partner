import http from './http'

// ========== CRUD + 基础查询（走 MySQL）==========

export function listPosts(params) {
  return http.get('/posts', { params })
}

export function getCategories() {
  return http.get('/posts/categories')
}

export function createPost(formData) {
  return http.post('/posts', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function getPost(id) {
  return http.get(`/posts/${id}`)
}

export function deletePost(id) {
  return http.delete(`/posts/${id}`)
}

export function getPostsByUser(userId) {
  return http.get(`/posts/user/${userId}`)
}

// ========== 搜索接口（走 Elasticsearch）==========

export function searchPostsByKeyword(params) {
  return http.get('/posts/search/keyword', { params })
}

export function searchPostsAdvanced(data) {
  return http.post('/posts/search/advanced', data)
}

export function searchPostsByCategory(category, params) {
  return http.get(`/posts/search/category/${category}`, { params })
}

export function searchPostsByDestination(params) {
  return http.get('/posts/search/destination', { params })
}

export function getSearchSuggestions(params) {
  return http.get('/posts/search/suggestions', { params })
}

export function getHotSearchKeywords(params) {
  return http.get('/posts/search/hot-keywords', { params })
}

// ========== 评论接口 ==========

export function listComments(postId, params = {}) {
  const defaultParams = { pageNum: 1, pageSize: 10 }
  return http.get('/comment/list', { params: { ...defaultParams, ...params, postId } })
}

export function createComment(data) {
  return http.post('/comment/create', data)
}

export function deleteComment(commentId) {
  return http.delete(`/comment/delete/${commentId}`)
}

export function toggleLike(commentId) {
  return http.post(`/comment/like/${commentId}`)
}

export function listReplies(parentCommentId) {
  return http.get(`/comment/replies/${parentCommentId}`)
}
