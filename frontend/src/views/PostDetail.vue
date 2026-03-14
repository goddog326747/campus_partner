<template>
  <div class="post-detail">
    <div class="header">
      <div class="back-btn" @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="author-info" @click="goToUser(post.userId)">
        <img :src="post.icon" alt="" class="avatar">
        <span class="name">{{ post.author || post.username }}</span>
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
      <div class="section-title">评论 ({{ totalComments }})</div>
      <div class="comment-list">
        <div class="comment-item" v-for="c in comments" :key="c.id">
          <div class="c-avatar" @click="goToUser(c.userId)">
            <img :src="c.avatar || 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png'" alt="">
          </div>
          <div class="c-content">
            <div class="c-header">
              <div class="c-user">{{ c.username }}</div>
              <div class="c-time">{{ formatTime(c.createTime) }}</div>
            </div>
            <div class="c-text">{{ c.content }}</div>
            <div class="c-actions">
              <div class="like-action" @click="handleLike(c)">
                <el-icon><Star /></el-icon>
                <span>{{ c.likeCount || 0 }}</span>
              </div>
              <div class="reply-action" @click="showReplyInput(c)">
                <el-icon><ChatDotRound /></el-icon>
                <span>回复</span>
              </div>
              <div class="delete-action" v-if="canDelete(c)" @click="handleDeleteComment(c.id)">
                <el-icon><Delete /></el-icon>
              </div>
            </div>
            
            <div v-if="replyingTo === c.id" class="reply-input-box">
              <input type="text" v-model="replyContent" :placeholder="`回复 ${c.username}...`" @keyup.enter="submitReply(c)">
              <button @click="submitReply(c)" :disabled="!replyContent.trim()">发送</button>
              <button class="cancel-btn" @click="cancelReply">取消</button>
            </div>
            
            <div v-if="c.replyCount > 0" class="reply-section">
              <div class="reply-toggle" @click="toggleReplies(c)">
                <el-icon><ArrowDown v-if="!c.showReplies" /><ArrowUp v-else /></el-icon>
                <span>{{ c.showReplies ? '收起回复' : `展开${c.replyCount}条回复` }}</span>
              </div>
              <div v-if="c.showReplies" class="reply-list">
                <div class="reply-item" v-for="r in c.replies" :key="r.id">
                  <div class="r-avatar" @click="goToUser(r.userId)">
                    <img :src="r.avatar || 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png'" alt="">
                  </div>
                  <div class="r-content">
                    <div class="r-header">
                      <span class="r-user">{{ r.username }}</span>
                      <span class="r-time">{{ formatTime(r.createTime) }}</span>
                    </div>
                    <div class="r-text">{{ r.content }}</div>
                    <div class="r-actions">
                      <div class="like-action" @click="handleLike(r)">
                        <el-icon><Star /></el-icon>
                        <span>{{ r.likeCount || 0 }}</span>
                      </div>
                      <div class="reply-action" @click="showReplyInput(r, c)">
                        <el-icon><ChatDotRound /></el-icon>
                        <span>回复</span>
                      </div>
                      <div class="delete-action" v-if="canDelete(r)" @click="handleDeleteReply(c, r.id)">
                        <el-icon><Delete /></el-icon>
                      </div>
                    </div>
                    
                    <div v-if="replyingTo === r.id" class="reply-input-box">
                      <input type="text" v-model="replyContent" :placeholder="`回复 ${r.username}...`" @keyup.enter="submitReply(c, r)">
                      <button @click="submitReply(c, r)" :disabled="!replyContent.trim()">发送</button>
                      <button class="cancel-btn" @click="cancelReply">取消</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="comments.length === 0" class="no-comments">
          暂无评论，快来发表第一条评论吧~
        </div>
        <div v-if="hasMoreComments" class="load-more" @click="loadMoreComments">
          <span v-if="!loadingMore">加载更多评论</span>
          <span v-else>加载中...</span>
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
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ArrowLeft, More, Location, Star, Delete, ChatDotRound, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPost, listComments, createComment, deleteComment as apiDeleteComment, toggleLike, listReplies } from '../api/posts'

const route = useRoute()
const router = useRouter()
const store = useStore()
const post = ref({})
const comments = ref([])
const newComment = ref('')
const currentPage = ref(1)
const totalComments = ref(0)
const pageSize = ref(10)
const loadingMore = ref(false)
const replyingTo = ref(null)
const replyContent = ref('')

const isLoggedIn = computed(() => store.getters.isLoggedIn)
const currentUser = computed(() => store.getters.currentUser)

const hasMoreComments = computed(() => {
  return comments.value.length < totalComments.value
})

const fetchPost = async (id) => {
  try {
    const { data } = await getPost(id)
    post.value = data
  } catch {
    post.value = {}
  }
}

const fetchComments = async (id, page = 1) => {
  try {
    const res = await listComments(id, { pageNum: page, pageSize: pageSize.value })
    const data = res?.data || res
    const records = data?.records || []
    
    for (const comment of records) {
      comment.replyCount = comment.replyCount || 0
      comment.replies = []
      comment.showReplies = false
    }
    
    if (page === 1) {
      comments.value = records
    } else {
      comments.value = [...comments.value, ...records]
    }
    totalComments.value = data?.total || 0
    currentPage.value = page
  } catch {
    comments.value = []
  }
}

const loadMoreComments = async () => {
  if (loadingMore.value) return
  loadingMore.value = true
  try {
    await fetchComments(route.params.id, currentPage.value + 1)
  } finally {
    loadingMore.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return date.toLocaleDateString()
}

const canDelete = (comment) => {
  if (!isLoggedIn.value || !currentUser.value) return false
  return comment.userId === currentUser.value.id
}

const requireLogin = (action) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

const handleDeleteComment = async (commentId) => {
  if (!requireLogin('删除评论')) return
  
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await apiDeleteComment(commentId)
    comments.value = comments.value.filter(c => c.id !== commentId)
    totalComments.value--
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleDeleteReply = async (parentComment, replyId) => {
  if (!requireLogin('删除回复')) return
  
  try {
    await ElMessageBox.confirm('确定要删除这条回复吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await apiDeleteComment(replyId)
    parentComment.replies = parentComment.replies.filter(r => r.id !== replyId)
    parentComment.replyCount--
    totalComments.value--
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleLike = async (comment) => {
  if (!requireLogin('点赞')) return
  
  try {
    const res = await toggleLike(comment.id)
    comment.likeCount = res?.data || res
    ElMessage.success('点赞成功')
  } catch {
    ElMessage.error('操作失败')
  }
}

const showReplyInput = (comment, parentComment = null) => {
  if (!requireLogin('回复')) return
  
  replyingTo.value = comment.id
  replyContent.value = ''
}

const cancelReply = () => {
  replyingTo.value = null
  replyContent.value = ''
}

const submitReply = async (parentComment, replyTo = null) => {
  if (!replyContent.value.trim()) return
  if (!requireLogin('回复')) return
  
  try {
    await createComment({
      postId: route.params.id,
      content: replyContent.value,
      parentId: parentComment.id
    })
    
    const res = await listReplies(parentComment.id)
    const repliesData = res?.data || res || []
    parentComment.replies = repliesData
    parentComment.replyCount = repliesData.length
    parentComment.showReplies = true
    
    replyContent.value = ''
    replyingTo.value = null
    totalComments.value++
    ElMessage.success('回复成功')
  } catch {
    ElMessage.error('回复失败，请稍后重试')
  }
}

const toggleReplies = async (comment) => {
  if (!comment.showReplies) {
    try {
      const res = await listReplies(comment.id)
      comment.replies = res?.data || res || []
    } catch {
      comment.replies = []
    }
  }
  comment.showReplies = !comment.showReplies
}

onMounted(async () => {
  const id = route.params.id
  await fetchPost(id)
  await fetchComments(id)
})

const addComment = async () => {
  if (!newComment.value.trim()) return
  if (!requireLogin('评论')) return
  
  try {
    await createComment({
      postId: route.params.id,
      content: newComment.value
    })
    await fetchComments(route.params.id, 1)
    newComment.value = ''
    ElMessage.success('评论成功')
  } catch {
    ElMessage.error('评论失败，请稍后重试')
  }
}

const goToUser = (userId) => {
  if (userId) {
    router.push(`/user/${userId}`)
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
  cursor: pointer;
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
  cursor: pointer;
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
.c-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.c-actions {
  display: flex;
  gap: 15px;
  margin-top: 6px;
}
.like-action, .reply-action, .delete-action {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
  cursor: pointer;
  transition: color 0.2s;
}
.like-action:hover {
  color: #f5a623;
}
.reply-action:hover {
  color: #409eff;
}
.delete-action:hover {
  color: #ff4d4f;
}
.no-comments {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 14px;
}

.reply-input-box {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 8px;
}
.reply-input-box input {
  flex: 1;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 16px;
  padding: 6px 12px;
  outline: none;
  font-size: 13px;
}
.reply-input-box input:focus {
  border-color: #409eff;
}
.reply-input-box button {
  background: #409eff;
  border: none;
  color: #fff;
  padding: 6px 12px;
  border-radius: 16px;
  cursor: pointer;
  font-size: 12px;
}
.reply-input-box button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
.reply-input-box .cancel-btn {
  background: #f0f0f0;
  color: #666;
}

.reply-section {
  margin-top: 10px;
  padding-left: 10px;
  border-left: 2px solid #e0e0e0;
}
.reply-toggle {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #409eff;
  cursor: pointer;
  padding: 6px 0;
}
.reply-toggle:hover {
  color: #66b1ff;
}
.reply-list {
  margin-top: 8px;
}
.reply-item {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.r-avatar img {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
}
.r-content {
  flex: 1;
}
.r-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}
.r-user {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}
.r-time {
  font-size: 10px;
  color: #bbb;
}
.r-text {
  font-size: 13px;
  color: #333;
  line-height: 1.4;
}
.r-actions {
  display: flex;
  gap: 12px;
  margin-top: 4px;
}
.r-actions .like-action,
.r-actions .reply-action,
.r-actions .delete-action {
  font-size: 11px;
}

.load-more {
  text-align: center;
  padding: 15px;
  color: #409eff;
  font-size: 14px;
  cursor: pointer;
}
.load-more:hover {
  color: #66b1ff;
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
