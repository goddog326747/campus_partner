<template>
  <div class="partner-list">
    <div class="page-header">
      <div class="search-box">
        <el-input
          v-model="searchQuery"
          placeholder="搜索感兴趣的活动、关键词..."
          prefix-icon="Search"
          clearable
          size="large"
          @clear="fetchList"
          @keyup.enter="fetchList"
        >
            <template #append>
                <el-button :icon="Search" @click="fetchList" />
            </template>
        </el-input>
      </div>

      <div class="category-tabs">
        <el-tabs v-model="activeCategory" @tab-click="handleTabClick">
          <el-tab-pane label="全部" name="" />
          <el-tab-pane 
            v-for="cat in categories" 
            :key="cat" 
            :label="cat" 
            :name="cat" 
          />
        </el-tabs>
      </div>
      
      <div class="filter-tags" v-if="isLoggedIn">
        <span class="filter-label">筛选：</span>
        <el-check-tag 
          v-if="currentUser?.location" 
          :checked="filters.location" 
          @change="toggleFilter('location')"
          class="filter-tag"
        >
          匹配同城
        </el-check-tag>
        <el-check-tag 
          v-if="currentUser?.school" 
          :checked="filters.school" 
          @change="toggleFilter('school')"
          class="filter-tag"
        >
          同校校友
        </el-check-tag>
        <el-check-tag 
          :checked="filters.verified" 
          @change="toggleFilter('verified')"
          class="filter-tag"
        >
          高校认证
        </el-check-tag>
        <el-check-tag
          v-if="currentUser?.gender"
          :checked="filters.sameGender"
          @change="toggleFilter('sameGender')"
          class="filter-tag"
        >
          同性友人
        </el-check-tag>
      </div>
    </div>
    
    <div class="blog-list" v-loading="loading">
      <el-empty v-if="!posts.length && !loading" description="暂无相关帖子" />
      
      <div class="blog-box" v-for="(b, index) in posts" :key="b.id" @click="$router.push('/post/' + b.id)" :style="{ animationDelay: `${index * 0.05}s` }">
        <div class="blog-img" v-if="getPostImages(b).length > 0">
          <img :src="getPostImages(b)[0]" alt="">
        </div>
        <div class="blog-content">
          <div class="blog-title">{{ b.title }}</div>
          <div class="blog-info">
             <span class="category-tag">{{ b.category }}</span>
             <span class="time">{{ formatTime(b.createTime) }}</span>
             <span v-if="b.destination" class="location-tag">
               <el-icon><Location /></el-icon>{{ b.destination }}
             </span>
          </div>
          <div class="blog-desc">{{ b.content }}</div>
          <div class="user-info" v-if="b.username">
            <el-avatar :size="26" :src="b.avatar" />
            <span class="username">{{ b.username }}</span>
            <el-tag v-if="b.userVerified" type="success" size="small" effect="plain" class="verified-tag">已认证</el-tag>
            <span v-if="b.userLocation" class="user-meta">{{ b.userLocation }}</span>
            <span v-if="b.userSchool" class="user-meta">{{ b.userSchool }}</span>
          </div>
        </div>
      </div>
      
      <div class="pagination" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useStore } from 'vuex'
import { Search, Location } from '@element-plus/icons-vue'
import { listPosts, listPostsWithFilter, getCategories } from '../api/posts'

const store = useStore()

const searchQuery = ref('')
const activeCategory = ref('')
const categories = ref([])
const posts = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filters = ref({
  location: false,
  school: false,
  verified: false,
  sameGender: false
})

const isLoggedIn = computed(() => store.getters.isLoggedIn)
const currentUser = computed(() => store.getters.currentUser)

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

const fetchList = async () => {
  loading.value = true
  
  const hasFilters = filters.value.location || filters.value.school || filters.value.verified || filters.value.sameGender
  
  if (hasFilters && isLoggedIn.value) {
    const params = { 
        category: activeCategory.value, 
        keyword: searchQuery.value,
        pageNum: pageNum.value,
        pageSize: pageSize.value
    }
    
    if (filters.value.location && currentUser.value?.location) {
      params.location = currentUser.value.location
    }
    if (filters.value.school && currentUser.value?.school) {
      params.school = currentUser.value.school
    }
    if (filters.value.verified) {
      params.verified = true
    }
    if (filters.value.sameGender && currentUser.value?.gender) {
      params.gender = currentUser.value.gender
    }
    
    try {
      const res = await listPostsWithFilter(params)
      if (res.code === 200) {
          posts.value = res.data?.records || []
          total.value = res.data?.total || 0
      } else {
          posts.value = []
          total.value = 0
      }
    } catch (e) {
      posts.value = []
      total.value = 0
      console.error(e)
    } finally {
      loading.value = false
    }
  } else {
    const params = {
        category: activeCategory.value,
        keyword: searchQuery.value,
        pageNum: pageNum.value,
        pageSize: pageSize.value
    }
    try {
      const res = await listPosts(params)
      if (res.code === 200) {
          posts.value = res.data?.records || []
          total.value = res.data?.total || 0
      } else {
          posts.value = []
          total.value = 0
      }
    } catch (e) {
      posts.value = []
      total.value = 0
      console.error(e)
    } finally {
      loading.value = false
    }
  }
}

const toggleFilter = (filterKey) => {
  filters.value[filterKey] = !filters.value[filterKey]
  pageNum.value = 1
  fetchList()
}

const handleTabClick = (tab) => {
    activeCategory.value = tab.paneName
    pageNum.value = 1
    fetchList()
}

const handlePageChange = (page) => {
  pageNum.value = page
  fetchList()
}

const formatTime = (timeStr) => {
    if (!timeStr) return ''
    return new Date(timeStr).toLocaleString()
}

const getPostImages = (post) => {
  if (!post.images) return []
  try {
    return JSON.parse(post.images)
  } catch {
    return []
  }
}

watch(filters, () => {
  pageNum.value = 1
}, { deep: true })

onMounted(() => {
    fetchCategories()
    fetchList()
})
</script>

<style scoped>
.partner-list {
  padding-bottom: 20px;
  min-height: 100vh;
}
.page-header {
  margin-bottom: 16px;
  padding: 20px 24px 0 24px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}
.search-box {
  margin-bottom: 12px;
}

.filter-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 0 14px 0;
}
.filter-label {
  font-size: 13px;
  color: var(--text-muted);
  flex-shrink: 0;
  font-weight: 500;
}
.filter-tag {
  flex: 1;
  justify-content: center;
  padding: 7px 0 !important;
  font-size: 12px;
  border-radius: var(--radius-sm) !important;
}

.blog-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 4px 0;
}

.blog-box {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 18px;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all var(--transition-normal);
  display: flex;
  gap: 14px;
  border: 1px solid var(--border-light);
  animation: fadeInUp 0.4s ease both;
}
.blog-box:hover {
    transform: translateY(-3px);
    box-shadow: var(--shadow-lg);
    border-color: rgba(79, 110, 247, 0.15);
}

.blog-img {
  width: 110px;
  height: 110px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;
}
.blog-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}
.blog-box:hover .blog-img img {
  transform: scale(1.05);
}

.blog-content {
  flex: 1;
  min-width: 0;
}

.blog-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 8px;
    line-height: 1.4;
}
.blog-info {
    display: flex;
    gap: 10px;
    font-size: 12px;
    color: var(--text-muted);
    margin-bottom: 8px;
    align-items: center;
    flex-wrap: wrap;
}
.category-tag {
    background: linear-gradient(135deg, rgba(79, 110, 247, 0.1), rgba(118, 75, 162, 0.1));
    color: var(--primary);
    padding: 3px 10px;
    border-radius: 20px;
    font-weight: 500;
    font-size: 11px;
}
.location-tag {
  display: flex;
  align-items: center;
  gap: 3px;
  color: var(--accent);
  font-weight: 500;
}
.blog-desc {
    font-size: 14px;
    color: var(--text-secondary);
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    line-height: 1.6;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  font-size: 12px;
  color: var(--text-muted);
}
.username {
  color: var(--text-primary);
  font-weight: 500;
}
.verified-tag {
  border-radius: 20px !important;
}
.user-meta {
  padding: 2px 8px;
  background: var(--bg-page);
  border-radius: 20px;
  font-size: 11px;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}
</style>
