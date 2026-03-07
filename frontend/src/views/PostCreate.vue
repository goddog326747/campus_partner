<template>
  <div class="blog-edit">
    <div class="header">
      <div class="header-cancel-btn" @click="$router.back()">取消</div>
      <div class="header-title">发笔记</div>
      <div class="header-commit">
        <div class="header-commit-btn" @click="submitBlog">发布</div>
      </div>
    </div>
    
    <div class="upload-box">
      <input type="file" @change="fileSelected" name="file" ref="fileInput" style="display: none" accept="image/*">
      <div class="upload-btn" @click="openFileDialog">
        <el-icon><Camera /></el-icon>
        <div style="font-size: 12px;line-height: 12px">上传照片</div>
      </div>
      <div class="pic-list">
        <div class="pic-box" v-for="(f, i) in fileList" :key="i">
          <img :src="f" alt="">
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

    <div class="divider"></div>

    <div class="blog-option" @click="showPartitionDialog = true">
      <div class="option-left">选择分区</div>
      <div v-if="selectedPartition">{{ selectedPartition }}</div>
      <div v-else>去选择 <el-icon><ArrowRight /></el-icon></div>
    </div>

    <!-- 分区选择弹窗 -->
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
import { Camera, Close, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { createPost, getCategories } from '../api/posts'

const router = useRouter()
const fileInput = ref(null)
const fileList = ref([])
const showPartitionDialog = ref(false)
const selectedPartition = ref('')

const form = reactive({
  title: '',
  content: ''
})

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
})

const openFileDialog = () => {
    fileInput.value.click()
}

const fileSelected = (e) => {
    const file = e.target.files[0]
    if (file) {
        const reader = new FileReader()
        reader.readAsDataURL(file)
        reader.onload = (e) => {
            fileList.value.push(e.target.result)
        }
    }
}

const deletePic = (index) => {
    fileList.value.splice(index, 1)
}

const selectPartition = (p) => {
    selectedPartition.value = p
    showPartitionDialog.value = false
}

const submitBlog = async () => {
  if (!form.title || !form.content || !selectedPartition.value) {
    ElMessage.warning('请填写标题、内容并选择分区')
    return
  }
  const payload = {
    title: form.title,
    content: form.content,
    // images: fileList.value, // TODO: 后端暂未实现图片存储，这里先注释或保留
    category: selectedPartition.value
  }
  try {
    const res = await createPost(payload)
    if (res.code === 200) {
        ElMessage.success('发布成功')
        router.push('/')
    } else {
        ElMessage.error(res.msg || '发布失败')
    }
  } catch {
    ElMessage.error('发布失败，请稍后重试')
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
}
.partition-item.active {
  background: #ff2442;
  color: #fff;
}
</style>
