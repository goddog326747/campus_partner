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
        <button class="agent-btn" @click="startAgentGenerate" :disabled="!form.topic.trim()">
          <span class="agent-icon">🤖</span>
          <span>Agent 模式</span>
        </button>
      </div>
    </div>

    <div class="generating-section" v-if="generating && !agentMode">
      <div class="stream-content">
        <div class="stream-header">
          <el-icon class="generating-icon"><MagicStick /></el-icon>
          <span>AI 正在创作...</span>
        </div>
        <div class="stream-body markdown-preview">
          <MarkdownRenderer :content="accumulatedContent || '正在思考...'" />
        </div>
      </div>
    </div>

    <div class="agent-generating-section" v-if="generating && agentMode">
      <div class="agent-visual">
        <div class="agent-orb-container">
          <div class="agent-orb">
            <div class="orb-ring ring-1"></div>
            <div class="orb-ring ring-2"></div>
            <div class="orb-ring ring-3"></div>
            <div class="orb-core"></div>
          </div>
          <div class="agent-label">Agent 深度思考中</div>
          <div class="agent-timer-badge">
            <el-icon class="timer-icon"><Loading /></el-icon>
            {{ agentTimer }}s
          </div>
        </div>
      </div>

      <div class="agent-steps-card">
        <div class="agent-step" :class="{ active: agentStep >= 1, done: agentStep > 1, current: agentStep === 1 }">
          <div class="step-indicator">
            <div class="step-icon-wrap">
              <span v-if="agentStep > 1" class="step-check">✓</span>
              <span v-else class="step-num">1</span>
            </div>
            <div class="step-connector" :class="{ filled: agentStep > 1 }"></div>
          </div>
          <div class="step-body">
            <div class="step-title">信息收集</div>
            <div class="step-desc">搜索热门话题、相关帖子、用户风格</div>
            <div class="step-status" v-if="agentStep === 1">
              <span class="status-dot blinking"></span>
              进行中...
            </div>
            <div class="step-status done-status" v-else-if="agentStep > 1">
              <span class="status-dot done-dot"></span>
              已完成
            </div>
          </div>
        </div>

        <div class="agent-step" :class="{ active: agentStep >= 2, done: agentStep > 2, current: agentStep === 2 }">
          <div class="step-indicator">
            <div class="step-icon-wrap">
              <span v-if="agentStep > 2" class="step-check">✓</span>
              <span v-else class="step-num">2</span>
            </div>
            <div class="step-connector" :class="{ filled: agentStep > 2 }"></div>
          </div>
          <div class="step-body">
            <div class="step-title">内容生成</div>
            <div class="step-desc">基于收集信息创作高质量帖子</div>
            <div class="step-status" v-if="agentStep === 2">
              <span class="status-dot blinking"></span>
              进行中...
            </div>
            <div class="step-status done-status" v-else-if="agentStep > 2">
              <span class="status-dot done-dot"></span>
              已完成
            </div>
          </div>
        </div>

        <div class="agent-step" :class="{ active: agentStep >= 3, done: agentStep > 3, current: agentStep === 3 }">
          <div class="step-indicator">
            <div class="step-icon-wrap">
              <span v-if="agentStep > 3" class="step-check">✓</span>
              <span v-else class="step-num">3</span>
            </div>
          </div>
          <div class="step-body">
            <div class="step-title">质量检查</div>
            <div class="step-desc">验证内容质量，不达标则优化重写</div>
            <div class="step-status" v-if="agentStep === 3">
              <span class="status-dot blinking"></span>
              进行中...
            </div>
            <div class="step-status done-status" v-else-if="agentStep > 3">
              <span class="status-dot done-dot"></span>
              已完成
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="result-section" v-if="generated && result">
      <div class="result-header">
        <div class="success-icon" :class="{ 'agent-success': agentMode }">
          <el-icon><Check /></el-icon>
        </div>
        <h3>{{ agentMode ? 'Agent 深度思考完成' : '生成完成' }}</h3>
        <p v-if="agentMode && result.executionTime" class="result-meta">
          <span class="meta-item">
            <span class="meta-icon">⏱</span>
            {{ (result.executionTime / 1000).toFixed(1) }}s
          </span>
          <span class="meta-divider">·</span>
          <span class="meta-item">
            <span class="meta-icon">🔗</span>
            {{ result.nodesExecuted || 0 }} 个节点
          </span>
        </p>
      </div>

      <div class="result-content">
        <div class="result-item">
          <label>标题</label>
          <div class="result-text title-text">{{ result.title }}</div>
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
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, MagicStick, Check, Refresh, Edit, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCategories } from '../api/posts'
import { aiGeneratePostStream, aiAgentGeneratePost } from '../api/ai'
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
const agentMode = ref(false)
const agentStep = ref(0)
const agentTimer = ref(0)
let agentTimerInterval = null
let agentStepInterval = null
const progress = ref(0)
const progressText = ref('准备中...')
const accumulatedContent = ref('')
const result = ref(null)

const conversationId = ref('conv-' + Date.now())

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

onUnmounted(() => {
  clearAgentTimers()
})

const clearAgentTimers = () => {
  if (agentTimerInterval) { clearInterval(agentTimerInterval); agentTimerInterval = null }
  if (agentStepInterval) { clearInterval(agentStepInterval); agentStepInterval = null }
}

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
  agentMode.value = false
  progress.value = 0
  progressText.value = '连接 AI 服务...'
  accumulatedContent.value = ''
  result.value = null

  const requestData = {
    topic: form.topic,
    category: form.category || undefined,
    style: form.style || undefined,
    requirements: form.requirements || undefined,
    conversationId: conversationId.value
  }

  streamGenerate(requestData)
}

const startAgentGenerate = async () => {
  if (!form.topic.trim()) {
    ElMessage.warning('请输入主题')
    return
  }

  generating.value = true
  generated.value = false
  agentMode.value = true
  agentStep.value = 1
  agentTimer.value = 0
  result.value = null

  agentTimerInterval = setInterval(() => { agentTimer.value++ }, 1000)
  agentStepInterval = setInterval(() => {
    if (agentStep.value < 3) agentStep.value++
  }, 6000)

  try {
    const requestData = {
      topic: form.topic,
      category: form.category || undefined,
      style: form.style || undefined,
      requirements: form.requirements || undefined,
      conversationId: conversationId.value
    }

    const res = await aiAgentGeneratePost(requestData)

    clearAgentTimers()
    agentStep.value = 3

    setTimeout(() => {
      if (res.code === 200 && res.data) {
        result.value = {
          title: res.data.title || '生成的帖子',
          content: res.data.content || '',
          tags: res.data.tags || '',
          executionTime: res.data.executionTime,
          nodesExecuted: res.data.nodesExecuted
        }
        generating.value = false
        generated.value = true
      } else {
        ElMessage.error(res.message || 'Agent 生成失败')
        generating.value = false
      }
    }, 500)
  } catch (e) {
    clearAgentTimers()
    console.error('Agent 生成失败:', e)
    ElMessage.error('Agent 生成失败，请稍后重试')
    generating.value = false
  }
}

const streamGenerate = async (requestData) => {
  try {
    const response = await aiGeneratePostStream(requestData)

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
      
      if (done) break

      const chunk = decoder.decode(value, { stream: true })
      buffer += chunk
      
      const events = buffer.split('\n\n')
      buffer = events.pop() || ''

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
            handleStreamEvent(currentEvent, data)
          } catch (e) {
            console.error('解析事件数据失败:', e, currentData)
          }
        }
      }
    }

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
      progress.value = data.progress || 50
      progressText.value = `生成中... ${data.charCount || 0} 字`
      accumulatedContent.value = data.accumulated || ''
      break
    case 'done':
      progress.value = 100
      progressText.value = '生成完成！'
      if (data.title || data.content) {
        result.value = {
          title: data.title || '生成的帖子',
          content: data.content || '',
          tags: data.tags || ''
        }
      } else {
        result.value = parseGeneratedContent(data.content || accumulatedContent.value)
      }
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

const parseGeneratedContent = (content) => {
  let title = extractSection(content, '标题', '【标题】')
  let body = extractSection(content, '正文', '【正文】')
  let tags = extractSection(content, '标签', '【标签】')

  if (!title) {
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

const extractSection = (content, keyword, marker) => {
  let start = content.indexOf(marker)
  if (start === -1) {
    start = content.indexOf(keyword + '：')
    if (start === -1) {
      start = content.indexOf(keyword + ':')
    }
  }

  if (start === -1) return ''

  start = content.indexOf('：', start)
  if (start === -1) {
    start = content.indexOf(':', start)
  }
  if (start === -1) return ''
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
  conversationId.value = 'conv-' + Date.now()
}

const useResult = () => {
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

.agent-btn {
  width: 100%;
  padding: 14px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  margin-top: 12px;
}

.agent-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(118, 75, 162, 0.4);
}

.agent-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.agent-icon {
  font-size: 18px;
}

.generating-section {
  padding: 24px 16px;
}

.stream-content {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  overflow: hidden;
}

.stream-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--gradient-primary);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
}

.generating-icon {
  font-size: 16px;
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.stream-body {
  padding: 16px;
  min-height: 200px;
  max-height: 400px;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-primary);
}

.agent-generating-section {
  padding: 24px 16px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.agent-visual {
  display: flex;
  justify-content: center;
  padding: 32px 0 40px;
}

.agent-orb-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.agent-orb {
  position: relative;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.orb-core {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  box-shadow: 0 0 30px rgba(118, 75, 162, 0.5), 0 0 60px rgba(102, 126, 234, 0.3);
  animation: corePulse 2s ease-in-out infinite;
  z-index: 2;
}

@keyframes corePulse {
  0%, 100% { transform: scale(1); box-shadow: 0 0 30px rgba(118, 75, 162, 0.5), 0 0 60px rgba(102, 126, 234, 0.3); }
  50% { transform: scale(1.1); box-shadow: 0 0 40px rgba(118, 75, 162, 0.6), 0 0 80px rgba(102, 126, 234, 0.4); }
}

.orb-ring {
  position: absolute;
  border-radius: 50%;
  border: 2px solid transparent;
}

.ring-1 {
  width: 70px;
  height: 70px;
  border-color: rgba(118, 75, 162, 0.4);
  animation: ringPulse1 2s ease-in-out infinite;
}

.ring-2 {
  width: 85px;
  height: 85px;
  border-color: rgba(102, 126, 234, 0.3);
  animation: ringPulse2 2.5s ease-in-out infinite;
}

.ring-3 {
  width: 100px;
  height: 100px;
  border-color: rgba(102, 126, 234, 0.15);
  animation: ringPulse3 3s ease-in-out infinite;
}

@keyframes ringPulse1 {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.15); opacity: 0.6; }
}

@keyframes ringPulse2 {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.5; }
}

@keyframes ringPulse3 {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.08); opacity: 0.4; }
}

.agent-label {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.5px;
}

.agent-timer-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  background: rgba(118, 75, 162, 0.1);
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  color: #764ba2;
}

.timer-icon {
  animation: rotate 2s linear infinite;
  font-size: 14px;
}

.agent-steps-card {
  background: var(--bg-card);
  border-radius: 16px;
  padding: 24px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.agent-step {
  display: flex;
  gap: 16px;
  opacity: 0.35;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.agent-step.active {
  opacity: 1;
}

.agent-step.current {
  opacity: 1;
}

.agent-step.done {
  opacity: 0.65;
}

.step-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.step-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--bg-page);
  border: 2px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-muted);
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
}

.agent-step.active .step-icon-wrap {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 4px 16px rgba(118, 75, 162, 0.35);
}

.agent-step.current .step-icon-wrap {
  animation: stepBounce 1.5s ease-in-out infinite;
}

@keyframes stepBounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.08); }
}

.agent-step.done .step-icon-wrap {
  background: linear-gradient(135deg, #52c41a, #389e0d);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}

.step-check {
  font-size: 16px;
  font-weight: 700;
}

.step-connector {
  width: 2px;
  height: 24px;
  background: var(--border-light);
  margin: 4px 0;
  transition: background 0.5s ease;
  border-radius: 1px;
}

.step-connector.filled {
  background: linear-gradient(180deg, #52c41a, #667eea);
}

.step-body {
  padding: 6px 0 20px;
  min-width: 0;
}

.agent-step:last-child .step-body {
  padding-bottom: 0;
}

.step-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.step-desc {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.step-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #764ba2;
  padding: 3px 10px;
  background: rgba(118, 75, 162, 0.08);
  border-radius: 12px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #764ba2;
}

.status-dot.blinking {
  animation: blink 1.2s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.done-status {
  color: #52c41a;
  background: rgba(82, 196, 26, 0.08);
}

.done-dot {
  background: #52c41a;
  animation: none;
}

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

.success-icon.agent-success {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.result-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.result-meta {
  font-size: 13px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.meta-icon {
  font-size: 14px;
}

.meta-divider {
  color: var(--border-light);
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

.title-text {
  font-size: 17px;
  font-weight: 600;
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
  background: rgba(118, 75, 162, 0.08);
  color: #764ba2;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
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
