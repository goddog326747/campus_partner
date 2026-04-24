<template>
  <div class="post-detail">
    <div class="header">
      <div class="back-btn" @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="author-info" @click="goToUser(post.userId)">
        <img :src="post.avatar || 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png'" alt="" class="avatar">
        <div class="author-meta">
          <span class="name">{{ post.username || post.author }}</span>
          <div class="author-tags">
            <el-tag v-if="post.userVerified === 2" type="success" size="small" effect="plain" class="verified-tag">已认证</el-tag>
            <el-tag v-else-if="post.userVerified === 1" type="warning" size="small" effect="plain" class="verified-tag">认证中</el-tag>
            <span v-if="post.userSchool" class="school-tag">{{ post.userSchool }}</span>
          </div>
        </div>
      </div>
      <div class="more-btn">
        <el-icon><More /></el-icon>
      </div>
    </div>

    <div class="content-container">
      <div class="post-images" v-if="post.images && post.images.length > 0">
         <el-carousel trigger="click" height="300px">
            <el-carousel-item v-for="item in post.images" :key="item">
               <img :src="item" alt="" class="carousel-img">
            </el-carousel-item>
         </el-carousel>
      </div>

      <div class="post-body">
        <h1 class="title">{{ post.title }}</h1>
        <div class="meta">
          <span class="tag">{{ post.category }}</span>
          <span class="location"><el-icon><Location /></el-icon> {{ post.destination }}</span>
          <span class="date">{{ post.date }}</span>
        </div>
        <div class="desc">
          <p>{{ post.content }}</p>
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
    const res = await getPost(id)
    const data = res.data || {}
    
    // 处理图片字段 - 如果是JSON字符串则解析为数组
    if (data.images && typeof data.images === 'string') {
      try {
        data.images = JSON.parse(data.images)
      } catch (e) {
        data.images = null
      }
    }
    // 确保images是数组
    if (!Array.isArray(data.images)) {
      data.images = null
    }
    
    post.value = data
  } catch {
    post.value = {}
  }
}

const fetchComments = async (id, page = 1) => {
  try {
    const res = await listComments(id, { pageNum: page, pageSize: pageSize.value })
    let records = []
    let total = 0
    
    // 处理后端返回的数据格式 - 可能是数组或分页对象
    const data = res?.data
    if (Array.isArray(data)) {
      // 后端直接返回数组
      records = data
      total = data.length
    } else if (data && typeof data === 'object') {
      // 后端返回分页对象
      records = data.records || []
      total = data.total || 0
    }
    
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
    totalComments.value = total
    currentPage.value = page
  } catch {
    comments.value = []
    totalComments.value = 0
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
  background-color: var(--bg-card);
  min-height: 100vh;
  border-radius: var(--radius-lg);
  overflow: hidden;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 18px;
  position: sticky;
  top: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  z-index: 100;
  border-bottom: 1px solid var(--border-light);
}
.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  cursor: pointer;
  transition: background var(--transition-fast);
  font-size: 18px;
  color: var(--text-secondary);
}
.back-btn:hover {
  background: var(--bg-page);
}
.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
.author-info .avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid var(--border-light);
}
.author-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.author-info .name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}
.author-tags {
  display: flex;
  align-items: center;
  gap: 6px;
}
.verified-tag {
  font-size: 10px;
  padding: 0 6px;
  height: 18px;
  line-height: 16px;
}
.school-tag {
  font-size: 11px;
  color: var(--text-muted);
}
.more-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  cursor: pointer;
  transition: background var(--transition-fast);
  color: var(--text-muted);
}
.more-btn:hover {
  background: var(--bg-page);
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
  padding: 20px 18px;
}
.title {
  font-size: 22px;
  margin: 10px 0;
  line-height: 1.4;
  font-weight: 700;
}
.meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 18px;
  align-items: center;
}
.tag {
  background: linear-gradient(135deg, rgba(79, 110, 247, 0.1), rgba(118, 75, 162, 0.1));
  color: var(--primary);
  padding: 3px 10px;
  border-radius: 20px;
  font-weight: 500;
}
.desc {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-secondary);
}
.desc p {
  margin-bottom: 12px;
}

.comments-section {
  border-top: 8px solid var(--bg-page);
  padding: 20px 18px;
}
.section-title {
  font-size: 17px;
  font-weight: 700;
  margin-bottom: 18px;
  color: var(--text-primary);
}
.comment-item {
  display: flex;
  gap: 12px;
  margin-bottom: 22px;
}
.c-avatar img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid var(--border-light);
}
.c-content {
  flex: 1;
}
.c-user {
  font-size: 14px;
  color: var(--text-primary);
  margin-bottom: 4px;
  font-weight: 600;
}
.c-text {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 6px;
  line-height: 1.5;
}
.c-time {
  font-size: 11px;
  color: var(--text-muted);
}
.c-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.c-actions {
  display: flex;
  gap: 18px;
  margin-top: 8px;
}
.like-action, .reply-action, .delete-action {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
  padding: 4px 8px;
  border-radius: 20px;
}
.like-action:hover {
  color: #f5a623;
  background: rgba(245, 166, 35, 0.08);
}
.reply-action:hover {
  color: var(--primary);
  background: rgba(79, 110, 247, 0.08);
}
.delete-action:hover {
  color: var(--accent);
  background: rgba(255, 107, 107, 0.08);
}
.no-comments {
  text-align: center;
  color: var(--text-muted);
  padding: 48px 0;
  font-size: 14px;
}

.reply-input-box {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  padding: 12px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
}
.reply-input-box input {
  flex: 1;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 20px;
  padding: 8px 14px;
  outline: none;
  font-size: 13px;
  transition: border-color var(--transition-fast);
}
.reply-input-box input:focus {
  border-color: var(--primary);
}
.reply-input-box button {
  background: var(--gradient-primary);
  border: none;
  color: #fff;
  padding: 8px 14px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: opacity var(--transition-fast);
}
.reply-input-box button:hover {
  opacity: 0.9;
}
.reply-input-box button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
.reply-input-box .cancel-btn {
  background: var(--bg-card);
  color: var(--text-muted);
  border: 1px solid var(--border-light);
}

.reply-section {
  margin-top: 12px;
  padding-left: 12px;
  border-left: 2px solid rgba(79, 110, 247, 0.2);
}
.reply-toggle {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--primary);
  cursor: pointer;
  padding: 8px 0;
  font-weight: 500;
  transition: opacity var(--transition-fast);
}
.reply-toggle:hover {
  opacity: 0.8;
}
.reply-list {
  margin-top: 8px;
}
.reply-item {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}
.r-avatar img {
  width: 26px;
  height: 26px;
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
  margin-bottom: 4px;
}
.r-user {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 600;
}
.r-time {
  font-size: 10px;
  color: var(--text-muted);
}
.r-text {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}
.r-actions {
  display: flex;
  gap: 14px;
  margin-top: 6px;
}
.r-actions .like-action,
.r-actions .reply-action,
.r-actions .delete-action {
  font-size: 11px;
  padding: 2px 6px;
}

.load-more {
  text-align: center;
  padding: 18px;
  color: var(--primary);
  font-size: 14px;
  cursor: pointer;
  font-weight: 500;
  transition: opacity var(--transition-fast);
}
.load-more:hover {
  opacity: 0.8;
}

.footer-input {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  padding: 12px 18px;
  border-top: 1px solid var(--border-light);
  display: flex;
  gap: 12px;
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.04);
}
.footer-input input {
  flex: 1;
  background: var(--bg-page);
  border: none;
  border-radius: 24px;
  padding: 10px 18px;
  outline: none;
  font-size: 14px;
  transition: box-shadow var(--transition-fast);
}
.footer-input input:focus {
  box-shadow: 0 0 0 2px rgba(79, 110, 247, 0.2);
}
.footer-input button {
  background: var(--gradient-primary);
  border: none;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  border-radius: 24px;
  padding: 10px 20px;
  font-size: 14px;
  transition: opacity var(--transition-fast);
}
.footer-input button:hover {
  opacity: 0.9;
}
.footer-input button:disabled {
  background: #ccc;
  opacity: 1;
}
</style>
