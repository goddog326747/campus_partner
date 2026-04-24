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
      <div class="ai-generate-btn" @click="goToAiGenerate">
        <el-icon><MagicStick /></el-icon>
        <span>AI 智能生成</span>
      </div>
    </div>

    <div class="divider"></div>

    <div class="blog-option" @click="showPartitionDialog = true">
      <div class="option-left">选择分区</div>
      <div v-if="selectedPartition" class="option-selected">{{ selectedPartition }}</div>
      <div v-else class="option-placeholder">去选择 <el-icon><ArrowRight /></el-icon></div>
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

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Camera, Close, ArrowRight, MagicStick } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { createPost, getCategories } from '../api/posts'

const router = useRouter()
const fileInput = ref(null)
const fileList = ref([])
const showPartitionDialog = ref(false)
const selectedPartition = ref('')
const submitting = ref(false)

const form = reactive({
  title: '',
  content: '',
  destination: '',
  tags: []
})

// 检查是否有AI生成的内容
const checkAiGeneratedContent = () => {
  const aiContent = localStorage.getItem('aiGeneratedPost')
  if (aiContent) {
    try {
      const data = JSON.parse(aiContent)
      form.title = data.title || ''
      form.content = data.content || ''
      if (data.category) {
        selectedPartition.value = data.category
      }
      // 加载标签
      if (data.tags && Array.isArray(data.tags)) {
        form.tags = data.tags
      }
      // 清除已使用的内容
      localStorage.removeItem('aiGeneratedPost')
      ElMessage.success('已加载 AI 生成的内容')
    } catch (e) {
      console.error('解析 AI 生成内容失败:', e)
    }
  }
}

const partitions = ref([])



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
    checkAiGeneratedContent()
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

const goToAiGenerate = () => {
    router.push('/ai/generate')
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
  padding: 16px;
  background: var(--bg-card);
  min-height: 100vh;
  border-radius: var(--radius-lg);
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 48px;
  margin-bottom: 16px;
}
.header-cancel-btn {
  font-size: 15px;
  color: var(--text-muted);
  cursor: pointer;
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}
.header-cancel-btn:hover {
  color: var(--text-secondary);
  background: var(--bg-page);
}
.header-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}
.header-commit-btn {
  background: var(--gradient-primary);
  color: #fff;
  padding: 8px 20px;
  border-radius: 24px;
  font-size: 14px;
  cursor: pointer;
  font-weight: 500;
  transition: all var(--transition-normal);
}
.header-commit-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(79, 110, 247, 0.3);
}
.header-commit-btn.disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.upload-box {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
}
.upload-btn {
  width: 84px;
  height: 84px;
  background: var(--bg-page);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  cursor: pointer;
  border: 2px dashed var(--border-light);
  transition: all var(--transition-fast);
  font-size: 24px;
}
.upload-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: rgba(79, 110, 247, 0.04);
}
.pic-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.pic-box {
  width: 84px;
  height: 84px;
  position: relative;
  border-radius: var(--radius-sm);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.pic-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.close-icon {
  position: absolute;
  top: 4px;
  right: 4px;
  background: rgba(0,0,0,0.5);
  color: #fff;
  border-radius: 50%;
  padding: 3px;
  cursor: pointer;
  font-size: 12px;
  transition: background var(--transition-fast);
}
.close-icon:hover {
  background: rgba(0,0,0,0.7);
}

.blog-title input {
  width: 100%;
  border: none;
  font-size: 20px;
  font-weight: 700;
  padding: 12px 0;
  outline: none;
  color: var(--text-primary);
}
.blog-content textarea {
  width: 100%;
  height: 200px;
  border: none;
  font-size: 15px;
  resize: none;
  outline: none;
  line-height: 1.7;
  color: var(--text-secondary);
}

.ai-generate-section {
  margin-top: 12px;
  margin-bottom: 12px;
}
.ai-generate-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  background: var(--gradient-primary);
  color: #fff;
  border-radius: 24px;
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-normal);
  font-weight: 500;
}
.ai-generate-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}
.ai-generate-btn .el-icon {
  font-size: 16px;
}

.divider {
  height: 1px;
  background: var(--border-light);
  margin: 12px 0;
}

.blog-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  font-size: 15px;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
}
.option-left {
  color: var(--text-primary);
  font-weight: 500;
}
.option-selected {
  color: var(--primary);
  font-weight: 500;
}
.option-placeholder {
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 4px;
}
.option-input {
  border: none;
  text-align: right;
  font-size: 15px;
  outline: none;
  color: var(--text-secondary);
  width: 200px;
}
.partition-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 20px 0;
}
.partition-item {
  padding: 8px 18px;
  background: var(--bg-page);
  border-radius: 24px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
  font-weight: 500;
}
.partition-item:hover {
  background: rgba(79, 110, 247, 0.08);
  color: var(--primary);
}
.partition-item.active {
  background: var(--gradient-primary);
  color: #fff;
}

</style>
