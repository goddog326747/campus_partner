import http from './http'

// ========== 非流式接口（普通 HTTP）==========

/**
 * AI 对话（非流式）
 * @param {Object} data - { message, mode: 'SIMPLE' | 'REACT', conversationId }
 */
export function aiChat(data) {
  return http.post('/ai/chat', data)
}

/**
 * 生成帖子（非流式）
 * @param {Object} data - { topic, category, style, requirements }
 */
export function aiGeneratePost(data) {
  return http.post('/ai/post/generate', data)
}

/**
 * 生成并发布帖子
 * @param {Object} data - { topic, category, style, requirements }
 */
export function aiGenerateAndPublishPost(data) {
  return http.post('/ai/post/publish', data)
}

// ========== 流式接口（SSE）==========

/**
 * AI 对话（流式 SSE）
 * @param {Object} data - { message, mode: 'SIMPLE' | 'REACT' }
 * @returns {Promise<Response>} 返回原始 Response，body 是 ReadableStream
 */
export function aiChatStream(data) {
  return fetch('/api/ai/stream/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream',
      'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
    },
    body: JSON.stringify(data)
  })
}

/**
 * 生成帖子（流式 SSE）
 * @param {Object} data - { topic, category, style, requirements }
 * @returns {Promise<Response>} 返回原始 Response，body 是 ReadableStream
 */
export function aiGeneratePostStream(data) {
  return fetch('/api/ai/stream/post/generate', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream',
      'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
    },
    body: JSON.stringify(data)
  })
}

// ========== 其他接口（预留）==========

/**
 * 执行自定义流程
 * @param {string} flowName - 流程名称: 'post-generation' | 'react-qa'
 * @param {Object} input - 流程输入参数
 */
export function executeFlow(flowName, input) {
  return http.post(`/ai/flow/${flowName}`, input)
}

/**
 * 获取执行历史
 * @param {string} executionId - 执行ID
 */
export function getExecutionHistory(executionId) {
  return http.get(`/ai/history/${executionId}`)
}
