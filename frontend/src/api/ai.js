import http from './http'

/**
 * AI 对话
 * @param {Object} data - { message, mode: 'SIMPLE' | 'REACT', conversationId }
 */
export function aiChat(data) {
  return http.post('/ai/chat', data)
}

/**
 * 生成帖子
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
