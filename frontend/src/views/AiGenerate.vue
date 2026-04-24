<template>
  <div class="ai-generate-page">
    <div class="header">
      <div class="header-back" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="header-title">AI 智能生成</div>
      <div class="header-action"></div>
    </div>

    <div class="content" v-if="!generating && !generated">
      <div class="intro-section">
        <div class="intro-icon">
          <el-icon><MagicStick /></el-icon>
        </div>
        <h2>AI 智能创作助手</h2>
        <p>输入主题，AI 将为您生成一篇精彩的社交帖子</p>
      </div>

      <div class="form-section">
        <div class="form-item">
          <label>主题 <span class="required">*</span></label>
          <input 
            v-model="form.topic" 
            type="text" 
            placeholder="例如：周末一起去爬山、寻找学习伙伴..."
          >
        </div>

        <div class="form-item">
          <label>分类</label>
          <div class="category-list">
            <div 
              v-for="c in categories" 
              :key="c" 
              class="category-item"
              :class="{ active: form.category === c }"
              @click="form.category = c"
            >
              {{ c }}
            </div>
          </div>
        </div>

        <div class="form-item">
          <label>风格</label>
          <div class="style-list">
            <div 
              v-for="s in styles" 
              :key="s" 
              class="style-item"
              :class="{ active: form.style === s }"
              @click="form.style = s"
            >
              {{ s }}
            </div>
          </div>
        </div>

        <div class="form-item">
          <label>补充要求（选填）</label>
          <textarea 
            v-model="form.requirements" 
            placeholder="例如：希望找3-5个伙伴、需要有一定经验..."
            rows="3"
          ></textarea>
        </div>
      </div>

      <div class="action-section">
        <button class="generate-btn" @click="startGenerate" :disabled="!form.topic.trim()">
          <el-icon><MagicStick /></el-icon>
          <span>开始生成</span>
        </button>
      </div>
    </div>

    <!-- 生成中状态 -->
    <div class="generating-section" v-if="generating">
      <div class="generating-animation">
        <div class="pulse-ring"></div>
        <div class="pulse-ring"></div>
        <div class="pulse-ring"></div>
        <div class="center-icon">
          <el-icon><MagicStick /></el-icon>
        </div>
      </div>
      <h3>AI 正在创作中...</h3>
      <p class="progress-text">{{ progressText }}</p>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: progress + '%' }"></div>
      </div>
      
      <!-- 实时预览 -->
      <div class="preview-section" v-if="accumulatedContent">
        <div class="preview-label">实时预览</div>
        <div class="preview-content markdown-preview">
          <MarkdownRenderer :content="accumulatedContent" />
        </div>
      </div>
    </div>

    <!-- 生成完成状态 -->
    <div class="result-section" v-if="generated && result">
      <div class="result-header">
        <div class="success-icon">
          <el-icon><Check /></el-icon>
        </div>
        <h3>生成完成！</h3>
      </div>

      <div class="result-content">
        <div class="result-item">
          <label>标题</label>
          <div class="result-text">{{ result.title }}</div>
        </div>
        
        <div class="result-item">
          <label>内容</label>
          <div class="result-text markdown-content">
            <MarkdownRenderer :content="result.content" />
          </div>
        </div>

        <div class="result-item" v-if="result.tags">
          <label>标签</label>
          <div class="result-tags">
            <span v-for="tag in result.tags.split(/[,，]/).filter(t => t.trim())" :key="tag" class="tag">
              #{{ tag.trim() }}
            </span>
          </div>
        </div>
      </div>

      <div class="result-actions">
        <button class="btn-secondary" @click="regenerate">
          <el-icon><Refresh /></el-icon>
          重新生成
        </button>
        <button class="btn-primary" @click="useResult">
          <el-icon><Edit /></el-icon>
          使用此内容
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, MagicStick, Check, Refresh, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCategories } from '../api/posts'
import MarkdownRenderer from '../components/MarkdownRenderer.vue'

const router = useRouter()

const categories = ref([])
const styles = ['轻松活泼', '热情邀请', '认真严肃', '幽默风趣', '文艺清新']

const form = reactive({
  topic: '',
  category: '',
  style: '轻松活泼',
  requirements: ''
})

const generating = ref(false)
const generated = ref(false)
const progress = ref(0)
const progressText = ref('准备中...')
const accumulatedContent = ref('')
const result = ref(null)

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    if (res.code === 200) {
      categories.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchCategories()
})

const goBack = () => {
  router.back()
}

const startGenerate = () => {
  if (!form.topic.trim()) {
    ElMessage.warning('请输入主题')
    return
  }

  generating.value = true
  generated.value = false
  progress.value = 0
  progressText.value = '连接 AI 服务...'
  accumulatedContent.value = ''
  result.value = null

  // 使用 fetch + ReadableStream 实现流式请求
  const requestData = {
    topic: form.topic,
    category: form.category || undefined,
    style: form.style || undefined,
    requirements: form.requirements || undefined
  }

  streamGenerate(requestData)
}

const streamGenerate = async (requestData) => {
  try {
    // 使用 fetch 获取 ReadableStream 并处理 SSE
    const response = await fetch('/api/ai/stream/post/generate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify(requestData)
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
      // SSE 格式: event: xxx\ndata: xxx\n\n
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
            handleStreamEvent(currentEvent, data)
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
          handleStreamEvent(currentEvent, data)
        } catch (e) {
          console.error('解析最后事件数据失败:', e)
        }
      }
    }
  } catch (e) {
    console.error('流式生成失败:', e)
    ElMessage.error('生成失败，请稍后重试')
    generating.value = false
  }
}

const handleStreamEvent = (eventType, data) => {
  switch (eventType) {
    case 'start':
      progress.value = 10
      progressText.value = '开始生成...'
      break
    case 'generating':
      progress.value = 30
      progressText.value = 'AI 正在思考...'
      break
    case 'token':
      // 真正的流式输出，逐字显示
      progress.value = data.progress || 50
      progressText.value = `生成中... ${data.charCount || 0} 字`
      accumulatedContent.value = data.accumulated || ''
      break
    case 'done':
      // 流式生成完成，解析内容
      progress.value = 100
      progressText.value = '生成完成！'
      const parsedResult = parseGeneratedContent(data.content)
      result.value = parsedResult
      generating.value = false
      generated.value = true
      break
    case 'error':
      ElMessage.error(data.error || '生成失败')
      generating.value = false
      break
    case 'complete':
      break
  }
}

// 解析生成的内容
const parseGeneratedContent = (content) => {
  // 简单解析，提取标题、内容和标签
  let title = extractSection(content, '标题', '【标题】')
  let body = extractSection(content, '正文', '【正文】')
  let tags = extractSection(content, '标签', '【标签】')

  if (!title) {
    // 如果没有明确标记，使用第一行作为标题
    const lines = content.split('\n', 2)
    title = lines[0].trim()
    body = lines.length > 1 ? lines[1].trim() : content
  }

  return {
    title: title,
    content: body || content,
    tags: tags
  }
}

// 提取章节内容
const extractSection = (content, keyword, marker) => {
  let start = content.indexOf(marker)
  if (start === -1) {
    start = content.indexOf(keyword + '：')
    if (start === -1) {
      start = content.indexOf(keyword + ':')
    }
  }

  if (start === -1) {
    return ''
  }

  start = content.indexOf('：', start)
  if (start === -1) {
    start = content.indexOf(':', start)
  }
  if (start === -1) {
    return ''
  }
  start++

  let end = content.length
  const nextMarkers = ['【', '标题', '正文', '标签', '\n\n']
  for (const nextMarker of nextMarkers) {
    const nextPos = content.indexOf(nextMarker, start)
    if (nextPos !== -1 && nextPos < end) {
      end = nextPos
    }
  }

  return content.substring(start, end).trim()
}

const regenerate = () => {
  generated.value = false
  result.value = null
  accumulatedContent.value = ''
}

const useResult = () => {
  // 将结果存储到 localStorage，然后跳转到发帖页面
  localStorage.setItem('aiGeneratedPost', JSON.stringify({
    title: result.value.title,
    content: result.value.content,
    category: form.category,
    tags: result.value.tags
  }))
  
  router.push('/post/create')
}
</script>

<style scoped>
.ai-generate-page {
  min-height: 100vh;
  background: var(--bg-page);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-back {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  transition: background var(--transition-fast);
}

.header-back:hover {
  background: var(--bg-page);
}

.header-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-action {
  width: 36px;
}

.content {
  padding: 24px 16px;
}

.intro-section {
  text-align: center;
  padding: 40px 20px;
  margin-bottom: 24px;
}

.intro-icon {
  width: 80px;
  height: 80px;
  background: var(--gradient-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 36px;
  color: #fff;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.intro-section h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.intro-section p {
  font-size: 14px;
  color: var(--text-muted);
}

.form-section {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm);
}

.form-item {
  margin-bottom: 20px;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-item label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.form-item label .required {
  color: var(--accent);
}

.form-item input,
.form-item textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
  transition: all var(--transition-fast);
  background: var(--bg-page);
  color: var(--text-primary);
}

.form-item input:focus,
.form-item textarea:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(79, 110, 247, 0.1);
}

.form-item textarea {
  resize: vertical;
  min-height: 80px;
}

.category-list,
.style-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-item,
.style-item {
  padding: 8px 16px;
  background: var(--bg-page);
  border-radius: 20px;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
  font-weight: 500;
  border: 1px solid transparent;
}

.category-item:hover,
.style-item:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.category-item.active,
.style-item.active {
  background: var(--gradient-primary);
  color: #fff;
  border-color: transparent;
}

.action-section {
  padding: 0 16px;
}

.generate-btn {
  width: 100%;
  padding: 14px 24px;
  background: var(--gradient-primary);
  color: #fff;
  border: none;
  border-radius: 28px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-normal);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.generate-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.generate-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 生成中状态 */
.generating-section {
  padding: 60px 24px;
  text-align: center;
}

.generating-animation {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 30px;
}

.pulse-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: var(--gradient-primary);
  opacity: 0.3;
  animation: pulse 2s ease-out infinite;
}

.pulse-ring:nth-child(2) {
  animation-delay: 0.5s;
}

.pulse-ring:nth-child(3) {
  animation-delay: 1s;
}

@keyframes pulse {
  0% {
    transform: translate(-50%, -50%) scale(0.5);
    opacity: 0.5;
  }
  100% {
    transform: translate(-50%, -50%) scale(1.5);
    opacity: 0;
  }
}

.center-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60px;
  height: 60px;
  background: var(--gradient-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.generating-section h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.progress-text {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 20px;
}

.progress-bar {
  width: 200px;
  height: 6px;
  background: var(--border-light);
  border-radius: 3px;
  margin: 0 auto 30px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--gradient-primary);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.preview-section {
  margin-top: 30px;
  text-align: left;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.preview-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.preview-content {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 16px;
  max-height: 300px;
  overflow-y: auto;
  text-align: left;
  border: 1px solid var(--border-light);
}

/* 结果状态 */
.result-section {
  padding: 24px 16px;
}

.result-header {
  text-align: center;
  margin-bottom: 24px;
}

.success-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  font-size: 32px;
  color: #fff;
}

.result-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.result-content {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm);
}

.result-item {
  margin-bottom: 20px;
}

.result-item:last-child {
  margin-bottom: 0;
}

.result-item label {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.result-text {
  font-size: 15px;
  color: var(--text-primary);
  line-height: 1.6;
  padding: 12px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
}

.markdown-content {
  padding: 0;
  background: transparent;
}

.result-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 4px 10px;
  background: rgba(79, 110, 247, 0.1);
  color: var(--primary);
  border-radius: 12px;
  font-size: 13px;
}

.result-actions {
  display: flex;
  gap: 12px;
}

.result-actions button {
  flex: 1;
  padding: 14px 24px;
  border-radius: 28px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-normal);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: none;
}

.btn-secondary {
  background: var(--bg-page);
  color: var(--text-secondary);
  border: 1px solid var(--border-light);
}

.btn-secondary:hover {
  background: var(--border-light);
}

.btn-primary {
  background: var(--gradient-primary);
  color: #fff;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.markdown-preview {
  font-size: 14px;
  line-height: 1.6;
}
</style>
