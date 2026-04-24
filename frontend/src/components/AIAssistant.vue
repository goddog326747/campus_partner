<template>
  <div class="ai-assistant-container">
    <!-- 触发按钮 -->
    <div class="ai-trigger" @click="toggleChat" v-show="!isOpen && isVisible">
      <el-icon><Service /></el-icon>
      <span>AI助手</span>
    </div>
    
    <!-- 聊天窗口 -->
    <div class="chat-window" v-show="isOpen && isVisible">
      <div class="chat-header">
        <div class="chat-header-left">
          <el-icon class="header-icon"><Service /></el-icon>
          <span>活动策划助手</span>
        </div>
        <div class="header-controls">
           <el-icon class="minimize-btn" @click="toggleChat" title="最小化"><Minus /></el-icon>
        </div>
      </div>
      
      <div class="chat-messages" ref="messagesRef">
        <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="msg.role">
          <div class="avatar">
            <el-icon v-if="msg.role === 'ai'"><Service /></el-icon>
            <el-icon v-else><User /></el-icon>
          </div>
          <div class="content" :class="{ 'markdown-content': msg.role === 'ai' }" v-html="msg.role === 'ai' ? renderMarkdown(msg.content) : msg.content"></div>
        </div>
      </div>
      
      <!-- 预制选项 -->
      <div class="preset-options" v-if="showPresets">
        <div class="preset-header">
          <span class="preset-title">💡 你可以问我</span>
          <span v-if="loadingHotTopics" class="loading-text">更新中...</span>
        </div>
        <div class="preset-list">
          <div 
            v-for="(option, index) in presetOptions" 
            :key="index"
            class="preset-item"
            :class="{ 'hot-topic': option.isHotTopic }"
            @click="sendPresetMessage(option)"
          >
            {{ option.icon }} {{ option.text }}
          </div>
        </div>
      </div>
      
      <div class="chat-input">
        <input 
          v-model="inputMessage" 
          @keyup.enter="sendMessage"
          type="text" 
          placeholder="询问活动策划建议..."
        >
        <button @click="sendMessage" :disabled="!inputMessage.trim()">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Service, User, Minus } from '@element-plus/icons-vue'
import { marked } from 'marked'
import http from '../api/http'

marked.setOptions({
  breaks: true,
  gfm: true
})

const route = useRoute()
const isOpen = ref(false)
const inputMessage = ref('')
const messagesRef = ref(null)
const hotTopics = ref([])
const loadingHotTopics = ref(false)

// 基础预制选项（通用功能）
const basePresetOptions = [
  { icon: '🎯', text: '如何策划一场成功的活动？', value: '如何策划一场成功的活动？请给出详细的步骤和建议。' },
  { icon: '📝', text: '帮我写个活动招募文案', value: '请帮我写一份吸引人的活动招募文案，包括标题、活动介绍和报名方式。' }
]

// 动态预制选项（包含热门话题）
const presetOptions = computed(() => {
  const options = [...basePresetOptions]
  
  // 添加热门话题选项（最多2个）
  if (hotTopics.value.length > 0) {
    hotTopics.value.slice(0, 2).forEach((topic, index) => {
      options.push({
        icon: '🔥',
        text: `大家在讨论：${topic}`,
        value: `最近大家在讨论"${topic}"，请帮我分析一下这个话题为什么热门，以及如何围绕这个话题策划活动？`,
        isHotTopic: true
      })
    })
  }
  
  return options
})

// 获取热门话题
const fetchHotTopics = async () => {
  loadingHotTopics.value = true
  try {
    const res = await http.get('/search/hot-keywords', { size: 3 })
    if (res.code === 200 && res.data) {
      hotTopics.value = res.data
    }
  } catch (e) {
    console.error('获取热门话题失败:', e)
    hotTopics.value = []
  } finally {
    loadingHotTopics.value = false
  }
}

const showPresets = computed(() => {
  // 只在有AI欢迎消息且没有用户消息时显示预制选项
  return messages.value.length === 1 && messages.value[0].role === 'ai'
})

const allowedRoutes = ['/partners', '/post/create', '/ai/generate']

const isVisible = computed(() => {
  return allowedRoutes.includes(route.path)
})

const messages = ref([
  { role: 'ai', content: '你好！我是你的活动策划AI助手，有什么可以帮你的吗？\n\n你可以直接输入问题，或点击下方快捷选项 👇' }
])

const toggleChat = () => {
  isOpen.value = !isOpen.value
  // 打开对话框时刷新热门话题
  if (isOpen.value) {
    fetchHotTopics()
  }
}

// 组件挂载时获取热门话题
onMounted(() => {
  fetchHotTopics()
})

// 发送预制消息
const sendPresetMessage = (option) => {
  inputMessage.value = option.value
  sendMessage()
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  
  const userText = inputMessage.value
  
  messages.value.push({
    role: 'user',
    content: userText
  })
  
  inputMessage.value = ''
  
  scrollToBottom()
  
  await fetchQwenResponse(userText)
}

const fetchQwenResponse = async (text) => {
  // 添加一个空的AI消息，用于流式更新
  const aiMessageIndex = messages.value.length
  messages.value.push({
    role: 'ai',
    content: '',
    loading: true
  })
  scrollToBottom()

  try {
    // 使用流式接口
    const response = await fetch('/api/ai/stream/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify({ 
        message: text,
        mode: 'SIMPLE'
      })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    if (!response.body) {
      throw new Error('Response body is null')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let accumulatedContent = ''

    while (true) {
      const { done, value } = await reader.read()
      
      if (done) {
        console.log('Stream completed')
        break
      }

      // 解码收到的数据
      const chunk = decoder.decode(value, { stream: true })
      buffer += chunk
      
      // 处理 SSE 格式的数据
      const events = buffer.split('\n\n')
      buffer = events.pop() || '' // 保留不完整的最后一部分

      for (const eventText of events) {
        if (!eventText.trim()) continue
        
        const lines = eventText.split('\n')
        let currentEvent = null
        let currentData = null

        for (const line of lines) {
          const trimmedLine = line.trim()
          if (trimmedLine.startsWith('event:')) {
            currentEvent = trimmedLine.slice(6).trim()
          } else if (trimmedLine.startsWith('data:')) {
            currentData = trimmedLine.slice(5).trim()
          }
        }

        if (currentEvent && currentData) {
          try {
            const data = JSON.parse(currentData)
            console.log('Received event:', currentEvent, data)
            
            if (currentEvent === 'token') {
              // 流式输出token
              accumulatedContent = data.accumulated || ''
              messages.value[aiMessageIndex].content = accumulatedContent
              messages.value[aiMessageIndex].loading = false
              scrollToBottom()
            } else if (currentEvent === 'done') {
              // 流式输出完成
              accumulatedContent = data.content || ''
              messages.value[aiMessageIndex].content = accumulatedContent
              messages.value[aiMessageIndex].loading = false
              scrollToBottom()
            } else if (currentEvent === 'error') {
              // 发生错误
              messages.value[aiMessageIndex].content = data.error || '生成失败，请稍后重试'
              messages.value[aiMessageIndex].loading = false
              scrollToBottom()
            }
          } catch (e) {
            console.error('解析事件数据失败:', e, currentData)
          }
        }
      }
    }

    // 处理缓冲区中剩余的数据
    if (buffer.trim()) {
      const lines = buffer.split('\n')
      let currentEvent = null
      let currentData = null
      
      for (const line of lines) {
        const trimmedLine = line.trim()
        if (trimmedLine.startsWith('event:')) {
          currentEvent = trimmedLine.slice(6).trim()
        } else if (trimmedLine.startsWith('data:')) {
          currentData = trimmedLine.slice(5).trim()
        }
      }
      
      if (currentEvent && currentData) {
        try {
          const data = JSON.parse(currentData)
          if (currentEvent === 'done') {
            accumulatedContent = data.content || ''
            messages.value[aiMessageIndex].content = accumulatedContent
            messages.value[aiMessageIndex].loading = false
          }
        } catch (e) {
          console.error('解析最后事件数据失败:', e)
        }
      }
    }

    // 如果内容为空，显示默认消息
    if (!messages.value[aiMessageIndex].content) {
      messages.value[aiMessageIndex].content = '抱歉，没有收到回复内容。'
      messages.value[aiMessageIndex].loading = false
    }
  } catch (error) {
    console.error('AI API Error:', error)

    let errorMessage = '网络连接出错，请检查网络或稍后再试。'
    if (error.response) {
      console.error('Error response:', error.response.data)
      if (error.response.data && error.response.data.msg) {
        errorMessage = '服务错误: ' + error.response.data.msg
      } else {
        errorMessage = '服务错误: ' + error.response.statusText
      }
    } else if (error.request) {
      console.error('No response received:', error.request)
      errorMessage = '请求超时，请检查网络连接或稍后再试。'
    } else {
      console.error('Request config error:', error.message)
      errorMessage = '请求配置错误: ' + error.message
    }

    messages.value[aiMessageIndex].content = errorMessage
    messages.value[aiMessageIndex].loading = false
  }
  
  scrollToBottom()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const renderMarkdown = (content) => {
  if (!content) return ''
  return marked.parse(content)
}

// 监听路由变化，切换页面时重置状态
watch(() => route.path, () => {
  if (!allowedRoutes.includes(route.path)) {
    isOpen.value = false
  }
})
</script>

<style scoped>
.ai-assistant-container {
  position: fixed;
  bottom: 80px;
  right: 24px;
  z-index: 1000;
}

.ai-trigger {
  width: 60px;
  height: 60px;
  background: var(--gradient-primary);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  font-size: 12px;
  transition: all var(--transition-normal);
}
.ai-trigger:hover {
  transform: scale(1.08);
  box-shadow: 0 8px 28px rgba(102, 126, 234, 0.5);
}
.ai-trigger .el-icon {
  font-size: 24px;
  margin-bottom: 2px;
}

.chat-window {
  width: 380px;
  height: 520px;
  background: white;
  border-radius: var(--radius-lg);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: fadeInUp 0.3s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chat-header {
  height: 54px;
  background: var(--gradient-primary);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  font-weight: 600;
}

.chat-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 20px;
}

.header-controls {
  display: flex;
  gap: 12px;
}

.header-controls .el-icon {
  cursor: pointer;
  font-size: 18px;
  transition: opacity var(--transition-fast);
}
.header-controls .el-icon:hover {
  opacity: 0.8;
}

.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: var(--bg-page);
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}
.message-item.user {
  flex-direction: row-reverse;
}
.message-item .avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #e0e0e0;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 8px;
  flex-shrink: 0;
  font-size: 16px;
}
.message-item.ai .avatar {
  background: rgba(79, 110, 247, 0.1);
  color: var(--primary);
}
.message-item.user .avatar {
  background: rgba(255, 107, 107, 0.1);
  color: var(--accent);
}
.message-item .content {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}
.message-item.ai .content {
  background: white;
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
}
.message-item.user .content {
  background: var(--gradient-primary);
  color: white;
}

.message-item .content.markdown-content {
  line-height: 1.6;
}

.message-item .content.markdown-content h1,
.message-item .content.markdown-content h2,
.message-item .content.markdown-content h3 {
  margin: 8px 0;
  font-size: 16px;
  font-weight: bold;
}

.message-item .content.markdown-content h1 {
  font-size: 18px;
}

.message-item .content.markdown-content h2 {
  font-size: 16px;
}

.message-item .content.markdown-content h3 {
  font-size: 15px;
}

.message-item .content.markdown-content p {
  margin: 6px 0;
}

.message-item .content.markdown-content ul,
.message-item .content.markdown-content ol {
  margin: 6px 0;
  padding-left: 20px;
}

.message-item .content.markdown-content li {
  margin: 4px 0;
}

.message-item .content.markdown-content code {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.message-item .content.markdown-content pre {
  background: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 13px;
}

.message-item .content.markdown-content pre code {
  background: none;
  padding: 0;
}

.message-item .content.markdown-content strong {
  font-weight: bold;
}

.message-item .content.markdown-content em {
  font-style: italic;
}

.message-item .content.markdown-content blockquote {
  border-left: 3px solid var(--primary);
  padding-left: 10px;
  margin: 8px 0;
  color: #666;
}

/* 预制选项样式 */
.preset-options {
  padding: 12px 16px;
  background: #f8f9fa;
  border-top: 1px solid var(--border-light);
}

.preset-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.preset-title {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.loading-text {
  font-size: 12px;
  color: #999;
  font-style: italic;
}

.preset-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.preset-item {
  background: white;
  border: 1px solid var(--border-light);
  border-radius: 16px;
  padding: 6px 12px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #333;
  display: flex;
  align-items: center;
  gap: 4px;
}

.preset-item:hover {
  background: var(--primary);
  color: white;
  border-color: var(--primary);
  transform: translateY(-1px);
}

.preset-item.hot-topic {
  background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
  border-color: #ff6b6b;
  color: #c92a2a;
}

.preset-item.hot-topic:hover {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8787 100%);
  color: white;
  border-color: #ff6b6b;
}

.chat-input {
  padding: 12px 16px;
  border-top: 1px solid var(--border-light);
  display: flex;
  gap: 10px;
  background: white;
}
.chat-input input {
  flex: 1;
  border: 1px solid var(--border-light);
  border-radius: 24px;
  padding: 10px 16px;
  outline: none;
  font-size: 14px;
  transition: border-color var(--transition-fast);
}
.chat-input input:focus {
  border-color: var(--primary);
}
.chat-input button {
  background: var(--gradient-primary);
  color: white;
  border: none;
  padding: 0 18px;
  border-radius: 24px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: opacity var(--transition-fast);
}
.chat-input button:hover {
  opacity: 0.9;
}
.chat-input button:disabled {
  background: #ccc;
  cursor: not-allowed;
  opacity: 1;
}
</style>
