import http from './http'

export function aiChat(text) {
  return http.post('/ai/chat', { text })
}

export function aiGeneratePost(data) {
  return http.post('/ai/post/generate', data)
}

export function aiGenerateAndPublishPost(data) {
  return http.post('/ai/post/publish', data)
}

export function getAiPostCategories() {
  return http.get('/ai/post/categories')
}
