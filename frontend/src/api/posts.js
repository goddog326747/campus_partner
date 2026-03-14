import http from './http'

export function listPosts(params) {
  return http.get('/post/list', { params })
}

export function listPostsWithFilter(params) {
  return http.get('/post/list/filter', { params })
}

export function getCategories() {
  return http.get('/post/categories')
}

export function createPost(formData) {
  return http.post('/post/create', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function getPost(id) {
  return http.get(`/post/detail/${id}`)
}

export function deletePost(id) {
  return http.delete(`/post/${id}`)
}

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

export function getPostsByUser(userId) {
  return http.get(`/post/user/${userId}`)
}
