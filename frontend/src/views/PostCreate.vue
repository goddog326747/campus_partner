<template>
  <div class="blog-edit">
    <div class="header">
      <div class="header-cancel-btn" @click="$router.back()">取消</div>
      <div class="header-title">发笔记</div>
      <div class="header-commit">
        <div class="header-commit-btn" @click="submitBlog" :class="{ disabled: submitting }">
          {{ submitting ? '发布中...' : '发布' }}
        </div>
      </div>
    </div>
    
    <div class="upload-box">
      <input type="file" @change="fileSelected" name="file" ref="fileInput" style="display: none" accept="image/*" multiple>
      <div class="upload-btn" @click="openFileDialog" v-if="fileList.length < 9">
        <el-icon><Camera /></el-icon>
        <div style="font-size: 12px;line-height: 12px">上传照片</div>
      </div>
      <div class="pic-list">
        <div class="pic-box" v-for="(f, i) in fileList" :key="i">
          <img :src="f.preview" alt="">
          <el-icon class="close-icon" @click="deletePic(i)"><Close /></el-icon>
        </div>
      </div>
    </div>

    <div class="blog-title">
      <input v-model="form.title" type="text" placeholder="填写标题更容易上首页哦~">
    </div>
    
    <div class="blog-content">
      <textarea v-model="form.content" placeholder="最近打卡了什么地方，有什么新奇体验呢？"></textarea>
    </div>

    <div class="ai-generate-section">
      <div class="ai-generate-btn" @click="showAiDialog = true">
        <el-icon><MagicStick /></el-icon>
        <span>AI 智能生成</span>
      </div>
    </div>

    <div class="divider"></div>

    <div class="blog-option" @click="showPartitionDialog = true">
      <div class="option-left">选择分区</div>
      <div v-if="selectedPartition">{{ selectedPartition }}</div>
      <div v-else>去选择 <el-icon><ArrowRight /></el-icon></div>
    </div>

    <div class="blog-option">
      <div class="option-left">目的地</div>
      <input v-model="form.destination" type="text" placeholder="填写目的地（选填）" class="option-input">
    </div>

    <el-dialog v-model="showPartitionDialog" title="选择分区" width="90%" custom-class="bottom-dialog">
      <div class="partition-list">
        <div 
          v-for="p in partitions" 
          :key="p" 
          class="partition-item"
          :class="{ active: selectedPartition === p }"
          @click="selectPartition(p)"
        >
          {{ p }}
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="showAiDialog" title="AI 智能生成" width="90%" class="ai-dialog">
      <div class="ai-form">
        <div class="ai-form-item">
          <label>主题 <span class="required">*</span></label>
          <input v-model="aiForm.topic" type="text" placeholder="例如：周末一起去爬山">
        </div>
        <div class="ai-form-item">
          <label>分类</label>
          <div class="ai-category-list">
            <div 
              v-for="c in partitions" 
              :key="c" 
              class="ai-category-item"
              :class="{ active: aiForm.category === c }"
              @click="aiForm.category = c"
            >
              {{ c }}
            </div>
          </div>
        </div>
        <div class="ai-form-item">
          <label>目的地</label>
          <input v-model="aiForm.destination" type="text" placeholder="例如：黄山（选填）">
        </div>
        <div class="ai-form-item">
          <label>风格</label>
          <div class="ai-style-list">
            <div 
              v-for="s in aiStyles" 
              :key="s" 
              class="ai-style-item"
              :class="{ active: aiForm.style === s }"
              @click="aiForm.style = s"
            >
              {{ s }}
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="ai-dialog-footer">
          <el-button @click="showAiDialog = false">取消</el-button>
          <el-button type="primary" @click="generateWithAi" :loading="aiGenerating">
            {{ aiGenerating ? '生成中...' : '生成内容' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Camera, Close, ArrowRight, MagicStick } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { createPost, getCategories } from '../api/posts'
import { aiGeneratePost } from '../api/ai'

const router = useRouter()
const fileInput = ref(null)
const fileList = ref([])
const showPartitionDialog = ref(false)
const selectedPartition = ref('')
const submitting = ref(false)

const form = reactive({
  title: '',
  content: '',
  destination: ''
})

const partitions = ref([])

const showAiDialog = ref(false)
const aiGenerating = ref(false)
const aiStyles = ['轻松活泼', '热情邀请', '认真严肃', '幽默风趣', '文艺清新']

const aiForm = reactive({
  topic: '',
  category: '',
  destination: '',
  style: '轻松活泼'
})

const fetchCategories = async () => {
    try {
        const res = await getCategories()
        if (res.code === 200) {
            partitions.value = res.data
        }
    } catch (e) {
        console.error(e)
    }
}

onMounted(() => {
    fetchCategories()
})

const openFileDialog = () => {
    fileInput.value.click()
}

const fileSelected = (e) => {
    const files = Array.from(e.target.files)
    files.forEach(file => {
        if (fileList.value.length >= 9) {
            ElMessage.warning('最多上传9张图片')
            return
        }
        const reader = new FileReader()
        reader.readAsDataURL(file)
        reader.onload = (e) => {
            fileList.value.push({
                file: file,
                preview: e.target.result
            })
        }
    })
    e.target.value = ''
}

const deletePic = (index) => {
    fileList.value.splice(index, 1)
}

const selectPartition = (p) => {
    selectedPartition.value = p
    showPartitionDialog.value = false
}

const generateWithAi = async () => {
  if (!aiForm.topic.trim()) {
    ElMessage.warning('请输入主题')
    return
  }
  
  aiGenerating.value = true
  
  try {
    // 构建请求参数 - 使用新的 API 格式
    const requestData = {
      topic: aiForm.topic,
      category: aiForm.category || undefined,
      style: aiForm.style || undefined,
      requirements: aiForm.destination ? `目的地：${aiForm.destination}` : undefined
    }
    
    const res = await aiGeneratePost(requestData)
    
    if (res.code === 200 && res.data) {
      // 新的 API 返回格式: { title, content, category, executionId, executionTime, nodesExecuted }
      form.title = res.data.title || ''
      form.content = res.data.content || ''
      
      // 如果返回了分类且当前未选择分类，则自动选择
      if (res.data.category && !selectedPartition.value) {
        selectedPartition.value = res.data.category
      }
      
      ElMessage.success('AI 生成成功，可继续编辑')
      showAiDialog.value = false
    } else {
      ElMessage.error(res.msg || 'AI 生成失败')
    }
  } catch (e) {
    console.error('AI 生成失败:', e)
    ElMessage.error('AI 生成失败，请稍后重试')
  } finally {
    aiGenerating.value = false
  }
}

const submitBlog = async () => {
  if (!form.title || !form.content || !selectedPartition.value) {
    ElMessage.warning('请填写标题、内容并选择分区')
    return
  }
  
  if (submitting.value) return
  submitting.value = true
  
  try {
    const formData = new FormData()
    formData.append('title', form.title)
    formData.append('content', form.content)
    formData.append('category', selectedPartition.value)
    if (form.destination) {
      formData.append('destination', form.destination)
    }
    
    fileList.value.forEach(f => {
      formData.append('images', f.file)
    })
    
    const res = await createPost(formData)
    if (res.code === 200) {
        ElMessage.success('发布成功')
        router.push('/')
    } else {
        ElMessage.error(res.msg || '发布失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('发布失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.blog-edit {
  padding: 10px;
  background: #fff;
  min-height: 100vh;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  margin-bottom: 10px;
}
.header-cancel-btn {
  font-size: 16px;
  color: #666;
}
.header-title {
  font-size: 18px;
  font-weight: bold;
}
.header-commit-btn {
  background: #ff2442;
  color: #fff;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
}
.header-commit-btn.disabled {
  background: #ccc;
  cursor: not-allowed;
}

.upload-box {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}
.upload-btn {
  width: 80px;
  height: 80px;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border-radius: 8px;
  color: #999;
  cursor: pointer;
}
.pic-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.pic-box {
  width: 80px;
  height: 80px;
  position: relative;
  border-radius: 8px;
  overflow: hidden;
}
.pic-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.close-icon {
  position: absolute;
  top: 2px;
  right: 2px;
  background: rgba(0,0,0,0.5);
  color: #fff;
  border-radius: 50%;
  padding: 2px;
  cursor: pointer;
}

.blog-title input {
  width: 100%;
  border: none;
  font-size: 20px;
  font-weight: bold;
  padding: 10px 0;
  outline: none;
}
.blog-content textarea {
  width: 100%;
  height: 200px;
  border: none;
  font-size: 16px;
  resize: none;
  outline: none;
  line-height: 1.5;
}

.ai-generate-section {
  margin-top: 10px;
  margin-bottom: 10px;
}
.ai-generate-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.ai-generate-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}
.ai-generate-btn .el-icon {
  font-size: 16px;
}

.divider {
  height: 1px;
  background: #eee;
  margin: 10px 0;
}

.blog-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  font-size: 16px;
  color: #333;
  border-bottom: 1px solid #f5f5f5;
}
.option-left {
  color: #333;
}
.option-input {
  border: none;
  text-align: right;
  font-size: 16px;
  outline: none;
  color: #666;
  width: 200px;
}
.partition-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 20px 0;
}
.partition-item {
  padding: 8px 16px;
  background: #f5f5f5;
  border-radius: 20px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
}
.partition-item.active {
  background: #ff2442;
  color: #fff;
}

.ai-form {
  padding: 10px 0;
}
.ai-form-item {
  margin-bottom: 20px;
}
.ai-form-item label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}
.ai-form-item label .required {
  color: #ff2442;
}
.ai-form-item input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
}
.ai-form-item input:focus {
  border-color: #667eea;
}
.ai-category-list,
.ai-style-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.ai-category-item,
.ai-style-item {
  padding: 6px 12px;
  background: #f5f5f5;
  border-radius: 16px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}
.ai-category-item.active,
.ai-style-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}
.ai-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
