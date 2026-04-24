<template>
  <div class="user-detail-container">
    <div class="user-card" v-loading="loading">
      <div class="user-header">
        <div class="avatar-wrapper">
          <el-avatar :size="100" :src="userInfo.avatar || defaultAvatar" />
        </div>
        <div class="user-info">
          <h2>
            {{ userInfo.nickname || '用户' }}
            <el-tooltip v-if="userInfo.verified === 2" content="已认证" placement="top">
              <el-icon class="verified-icon verified"><CircleCheckFilled /></el-icon>
            </el-tooltip>
            <el-tooltip v-else-if="userInfo.verified === 1" content="认证中" placement="top">
              <el-icon class="verified-icon pending"><Clock /></el-icon>
            </el-tooltip>
          </h2>
          <p class="bio" v-if="userInfo.bio">{{ userInfo.bio }}</p>
          <div class="meta-info">
            <span v-if="userInfo.location"><el-icon><Location /></el-icon> {{ userInfo.location }}</span>
            <span v-if="userInfo.school">
              <el-icon><School /></el-icon> {{ userInfo.school }}
              <el-icon v-if="userInfo.verified === 2" class="school-verified"><CircleCheckFilled /></el-icon>
            </span>
          </div>
        </div>
      </div>
      
      <el-divider />
      
      <div class="detail-section">
        <h3>基本信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="性别">
            {{ genderText }}
          </el-descriptions-item>
          <el-descriptions-item label="生日" v-if="userInfo.birthday">
            {{ userInfo.birthday }}
          </el-descriptions-item>
          <el-descriptions-item label="所在地" v-if="userInfo.location">
            {{ userInfo.location }}
          </el-descriptions-item>
          <el-descriptions-item label="学校" v-if="userInfo.school">
            {{ userInfo.school }}
            <el-tag v-if="userInfo.verified === 2" type="success" size="small" style="margin-left: 8px;">
              <el-icon style="margin-right: 2px;"><CircleCheckFilled /></el-icon>
              已认证
            </el-tag>
            <el-tag v-else-if="userInfo.verified === 1" type="warning" size="small" style="margin-left: 8px;">
              认证中
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <div class="detail-section" v-if="hasContactInfo">
        <h3>联系方式</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="手机号" v-if="userInfo.phone">
            {{ userInfo.phone }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱" v-if="userInfo.email">
            {{ userInfo.email }}
          </el-descriptions-item>
          <el-descriptions-item label="微信" v-if="userInfo.wechat">
            {{ userInfo.wechat }}
          </el-descriptions-item>
          <el-descriptions-item label="QQ" v-if="userInfo.qq">
            {{ userInfo.qq }}
          </el-descriptions-item>
        </el-descriptions>
        <p class="privacy-tip" v-if="!userInfo.phone && !userInfo.email && !userInfo.wechat && !userInfo.qq">
          <el-icon><Lock /></el-icon>
          用户未公开联系方式
        </p>
      </div>
    </div>
    
    <div class="posts-card">
      <div class="posts-card-header">
        <span>TA发布的帖子</span>
      </div>
      <div v-if="posts.length === 0" class="empty-posts">
        暂无发布的帖子
      </div>
      <div v-else class="post-list">
        <div v-for="post in posts" :key="post.id" class="post-item" @click="goToPost(post.id)">
          <h4>{{ post.title }}</h4>
          <p>{{ post.content.substring(0, 100) }}...</p>
          <span class="post-time">{{ formatDate(post.createTime) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, School, Lock, CircleCheckFilled, Clock } from '@element-plus/icons-vue'
import { getUserById } from '../api/user'
import { getPostsByUser } from '../api/posts'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const userInfo = ref({})
const posts = ref([])

const genderText = computed(() => {
  const genders = ['保密', '男', '女']
  return genders[userInfo.value.gender || 0]
})

const hasContactInfo = computed(() => {
  return userInfo.value.phone || userInfo.value.email || 
         userInfo.value.wechat || userInfo.value.qq
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const fetchUserInfo = async () => {
  try {
    const res = await getUserById(route.params.id)
    if (res.code === 200) {
      userInfo.value = res.data
    } else {
      ElMessage.error(res.msg || '获取用户信息失败')
    }
  } catch (e) {
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const fetchUserPosts = async () => {
  try {
    const res = await getPostsByUser(route.params.id)
    if (res.code === 200) {
      posts.value = res.data || []
    }
  } catch (e) {
    console.error('获取用户帖子失败', e)
  }
}

const goToPost = (postId) => {
  router.push(`/post/${postId}`)
}

onMounted(() => {
  fetchUserInfo()
  fetchUserPosts()
})
</script>

<style scoped>
.user-detail-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0;
}

.user-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 32px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
}

.user-header {
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.avatar-wrapper {
  position: relative;
}

.user-info h2 {
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 22px;
  font-weight: 700;
}

.verified-icon {
  font-size: 20px;
  vertical-align: middle;
}

.verified-icon.verified {
  color: #67c23a;
}

.verified-icon.pending {
  color: #e6a23c;
}

.school-verified {
  color: #67c23a;
  font-size: 14px;
  margin-left: 4px;
  vertical-align: middle;
}

.user-info .bio {
  color: var(--text-secondary);
  margin: 0 0 12px 0;
  font-size: 14px;
  line-height: 1.6;
}

.meta-info {
  display: flex;
  gap: 18px;
  color: var(--text-muted);
  font-size: 14px;
}

.meta-info span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.detail-section {
  margin-top: 24px;
}

.detail-section h3 {
  margin-bottom: 16px;
  color: var(--text-primary);
  font-weight: 700;
  font-size: 16px;
}

.privacy-tip {
  color: var(--text-muted);
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 16px 0;
}

.posts-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px 32px;
  margin-top: 20px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
}

.posts-card-header {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.post-item {
  padding: 16px;
  border-radius: var(--radius-sm);
  margin-bottom: 8px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.post-item:hover {
  background: var(--bg-page);
}

.post-item h4 {
  margin: 0 0 8px 0;
  color: var(--text-primary);
  font-weight: 600;
}

.post-item p {
  margin: 0 0 8px 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.5;
}

.post-time {
  font-size: 12px;
  color: var(--text-muted);
}

.empty-posts {
  text-align: center;
  color: var(--text-muted);
  padding: 48px 0;
  font-size: 14px;
}
</style>
