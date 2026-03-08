<template>
  <div class="ai-assistant-container" v-show="isVisible">
    <div class="ai-trigger" @click="toggleChat" v-if="!isOpen">
      <el-icon><Service /></el-icon>
      <span>AI助手</span>
    </div>
    
    <div class="chat-window" v-if="isOpen">
      <div class="chat-header">
        <span>活动策划助手</span>
        <div class="header-controls">
           <el-icon class="minimize-btn" @click="toggleChat" title="最小化"><Minus /></el-icon>
           <el-icon class="close-btn" @click="closeAssistant" title="关闭"><Close /></el-icon>
        </div>
      </div>
      
      <div class="chat-messages" ref="messagesRef">
        <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="msg.role">
          <div class="avatar">
            <el-icon v-if="msg.role === 'ai'"><Service /></el-icon>
            <el-icon v-else><User /></el-icon>
          </div>
          <div class="content">{{ msg.content }}</div>
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
import { ref, nextTick, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Service, Close, User, Minus } from '@element-plus/icons-vue'

const route = useRoute()
const isOpen = ref(false)
const isClosed = ref(false)
const inputMessage = ref('')
const messagesRef = ref(null)

// 控制显示逻辑：只有在特定路由且未被用户完全关闭时显示
const isVisible = computed(() => {
  const allowedRoutes = ['/partners', '/post/create']
  return allowedRoutes.includes(route.path) && !isClosed.value
})

const messages = ref([
  { role: 'ai', content: '你好！我是你的活动策划AI助手，有什么可以帮你的吗？' }
])

const toggleChat = () => {
  isOpen.value = !isOpen.value
}

const closeAssistant = () => {
  isClosed.value = true
  isOpen.value = false
}

// 监听路由变化，如果切换到不支持的页面自动收起（虽然v-show会隐藏，但状态最好重置）
watch(() => route.path, () => {
   if (!isVisible.value) {
     isOpen.value = false
   }
})

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  
  const userText = inputMessage.value
  
  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: userText
  })
  
  inputMessage.value = ''
  
  // 滚动到底部
  scrollToBottom()
  
  // 调用千问 API
  await fetchQwenResponse(userText)
}

import http from '../api/http'

// ... existing code ...

const fetchQwenResponse = async (text) => {
  // 添加一个临时的加载消息
  messages.value.push({
    role: 'ai',
    content: '正在思考中...',
    loading: true
  })
  scrollToBottom()

  try {
    // 调用后端接口
    const res = await http.post('/ai/chat', { text: text })
    
    // 移除加载消息
    messages.value.pop()

    if (res.code === 200) {
      messages.value.push({
        role: 'ai',
        content: res.data
      })
    } else {
      messages.value.push({
        role: 'ai',
        content: res.msg || '抱歉，我暂时无法回答这个问题，请稍后再试。'
      })
    }
  } catch (error) {
    // 移除加载消息
    messages.value.pop()
    console.error('AI API Error:', error)

    // 更详细的错误处理
    let errorMessage = '网络连接出错，请检查网络或稍后再试。'
    if (error.response) {
      // 服务器返回了错误响应
      console.error('Error response:', error.response.data)
      if (error.response.data && error.response.data.msg) {
        errorMessage = '服务错误: ' + error.response.data.msg
      } else {
        errorMessage = '服务错误: ' + error.response.statusText
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      console.error('No response received:', error.request)
      errorMessage = '请求超时，请检查网络连接或稍后再试。'
    } else {
      // 请求配置错误
      console.error('Request config error:', error.message)
      errorMessage = '请求配置错误: ' + error.message
    }

    messages.value.push({
      role: 'ai',
      content: errorMessage
    })
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
</script>

<style scoped>
.ai-assistant-container {
  position: fixed;
  bottom: 80px;
  right: 20px;
  z-index: 1000;
}

.ai-trigger {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #ff6633, #ff8855);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(255, 102, 51, 0.4);
  font-size: 12px;
  transition: transform 0.2s;
}
.ai-trigger:hover {
  transform: scale(1.05);
}
.ai-trigger .el-icon {
  font-size: 24px;
  margin-bottom: 2px;
}

.chat-window {
  width: 320px;
  height: 450px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #eee;
}

.chat-header {
  height: 50px;
  background: #ff6633;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 15px;
  font-weight: bold;
}

.header-controls {
  display: flex;
  gap: 10px;
}

.header-controls .el-icon {
  cursor: pointer;
  font-size: 18px;
  transition: opacity 0.2s;
}
.header-controls .el-icon:hover {
  opacity: 0.8;
}

.chat-messages {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  background: #f9f9f9;
}
/* ... rest of the styles remain same ... */

.message-item {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}
.message-item.user {
  flex-direction: row-reverse;
}
.message-item .avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e0e0e0;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 8px;
  flex-shrink: 0;
}
.message-item.ai .avatar {
  background: #e6f7ff;
  color: #1890ff;
}
.message-item.user .avatar {
  background: #fff0e6;
  color: #ff6633;
}
.message-item .content {
  max-width: 70%;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.4;
  word-break: break-word;
}
.message-item.ai .content {
  background: white;
  border: 1px solid #eee;
}
.message-item.user .content {
  background: #ff6633;
  color: white;
}

.chat-input {
  padding: 10px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 10px;
  background: white;
}
.chat-input input {
  flex: 1;
  border: 1px solid #ddd;
  border-radius: 20px;
  padding: 8px 12px;
  outline: none;
  font-size: 14px;
}
.chat-input input:focus {
  border-color: #ff6633;
}
.chat-input button {
  background: #ff6633;
  color: white;
  border: none;
  padding: 0 15px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
}
.chat-input button:disabled {
  background: #ffccbc;
  cursor: not-allowed;
}
</style>
