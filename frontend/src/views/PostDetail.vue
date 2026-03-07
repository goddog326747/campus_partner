<template>
  <div class="post-detail">
    <div class="header">
      <div class="back-btn" @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="author-info">
        <img :src="post.icon" alt="" class="avatar">
        <span class="name">{{ post.author }}</span>
      </div>
      <div class="more-btn">
        <el-icon><More /></el-icon>
      </div>
    </div>

    <div class="content-container">
      <div class="post-images">
         <el-carousel trigger="click" height="300px" v-if="post.images && post.images.length">
            <el-carousel-item v-for="item in post.images" :key="item">
               <img :src="item" alt="" class="carousel-img">
            </el-carousel-item>
         </el-carousel>
         <img v-else :src="post.img" class="main-img" />
      </div>

      <div class="post-body">
        <h1 class="title">{{ post.title }}</h1>
        <div class="meta">
          <span class="tag">{{ post.category }}</span>
          <span class="location"><el-icon><Location /></el-icon> {{ post.destination }}</span>
          <span class="date">{{ post.date }}</span>
        </div>
        <div class="desc">
          <p>这里是帖子详情内容的模拟展示。{{ post.title }}，欢迎大家一起来玩！我们计划在{{ post.destination }}集合，预计花费人均500元。无论是新手还是老手都欢迎！</p>
          <p>感兴趣的朋友可以在下方评论区留言或者直接私信我哦~</p>
        </div>
      </div>
    </div>

    <div class="comments-section">
      <div class="section-title">评论 ({{ comments.length }})</div>
      <div class="comment-list">
        <div class="comment-item" v-for="c in comments" :key="c.id">
          <div class="c-avatar">
            <img :src="c.avatar" alt="">
          </div>
          <div class="c-content">
            <div class="c-user">{{ c.user }}</div>
            <div class="c-text">{{ c.text }}</div>
            <div class="c-time">{{ c.time }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="footer-input">
      <input type="text" v-model="newComment" placeholder="说点什么..." @keyup.enter="addComment">
      <button @click="addComment" :disabled="!newComment.trim()">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft, More, Location } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getPost, listComments, addComment as apiAddComment } from '../api/posts'

const route = useRoute()
const post = ref({})
const comments = ref([])
const newComment = ref('')

const fetchPost = async (id) => {
  try {
    const { data } = await getPost(id)
    post.value = data
  } catch {
    post.value = {}
  }
}

const fetchComments = async (id) => {
  try {
    const { data } = await listComments(id)
    comments.value = data?.records || data || []
  } catch {
    comments.value = []
  }
}

onMounted(async () => {
  const id = route.params.id
  await fetchPost(id)
  await fetchComments(id)
})

const addComment = async () => {
  if (!newComment.value.trim()) return
  try {
    await apiAddComment(route.params.id, { text: newComment.value })
    comments.value.unshift({
      id: Date.now(),
      user: '我',
      avatar: 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png',
      text: newComment.value,
      time: '刚刚'
    })
    newComment.value = ''
    ElMessage.success('评论成功')
  } catch {
    ElMessage.error('评论失败，请稍后重试')
  }
}
</script>

<style scoped>
.post-detail {
  padding-bottom: 60px;
  background-color: #fff;
  min-height: 100vh;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 100;
  border-bottom: 1px solid #f0f0f0;
}
.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.author-info .avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
}
.author-info .name {
  font-size: 14px;
  font-weight: 500;
}
.post-images {
  width: 100%;
}
.carousel-img, .main-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-body {
  padding: 15px;
}
.title {
  font-size: 20px;
  margin: 10px 0;
  line-height: 1.4;
}
.meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #999;
  margin-bottom: 15px;
  align-items: center;
}
.tag {
  background: #f0f9eb;
  color: #67c23a;
  padding: 2px 6px;
  border-radius: 4px;
}
.desc {
  font-size: 15px;
  line-height: 1.6;
  color: #333;
}
.desc p {
  margin-bottom: 10px;
}

.comments-section {
  border-top: 8px solid #f5f5f5;
  padding: 15px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 15px;
}
.comment-item {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.c-avatar img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}
.c-content {
  flex: 1;
}
.c-user {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
}
.c-text {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  line-height: 1.4;
}
.c-time {
  font-size: 11px;
  color: #999;
}

.footer-input {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  background: #fff;
  padding: 10px 15px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 10px;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
}
.footer-input input {
  flex: 1;
  background: #f5f5f5;
  border: none;
  border-radius: 20px;
  padding: 8px 15px;
  outline: none;
}
.footer-input button {
  background: transparent;
  border: none;
  color: #409eff;
  font-weight: 600;
  cursor: pointer;
}
.footer-input button:disabled {
  color: #ccc;
}
</style>
