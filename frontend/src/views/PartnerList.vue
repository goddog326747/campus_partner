<template>
  <div class="partner-list">
    <div class="page-header">
      <!-- 1. Search Bar at the very top -->
      <div class="search-box">
        <el-input
          v-model="searchQuery"
          placeholder="搜索感兴趣的活动、关键词..."
          prefix-icon="Search"
          clearable
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
    </div>
    
    <!-- Post List -->
    <div class="blog-list" v-loading="loading">
      <el-empty v-if="!partners.length && !loading" description="暂无相关帖子" />
      
      <div class="blog-box" v-for="b in partners" :key="b.id" @click="$router.push('/post/' + b.id)">
        <!-- Image placeholder or random image if not provided -->
        <div class="blog-img" v-if="false"> 
           <!-- Image logic to be added later -->
           <img src="https://via.placeholder.com/150" alt="">
        </div>
        <div class="blog-content">
          <div class="blog-title">{{ b.title }}</div>
          <div class="blog-info">
             <span class="category-tag">{{ b.category }}</span>
             <span class="time">{{ formatTime(b.createTime) }}</span>
          </div>
          <div class="blog-desc">{{ b.content }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { listPosts, getCategories } from '../api/posts'

const searchQuery = ref('')
const activeCategory = ref('')
const categories = ref([])
const partners = ref([])
const loading = ref(false)

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
  const params = { 
      category: activeCategory.value, 
      keyword: searchQuery.value 
  }
  console.log('fetchList called with params:', params)
  try {
    const res = await listPosts(params)
    console.log('listPosts response:', res)
    if (res.code === 200) {
        partners.value = res.data || []
        console.log('partners count:', partners.value.length)
    } else {
        partners.value = []
    }
  } catch (e) {
    partners.value = []
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleTabClick = (tab) => {
    // tab.paneName is the value of the name prop (which is 'cat' in our loop)
    // activeCategory is already updated by v-model, but let's double check
    console.log('Tab clicked, paneName:', tab.paneName, 'activeCategory:', activeCategory.value)
    activeCategory.value = tab.paneName
    fetchList()
}

const formatTime = (timeStr) => {
    if (!timeStr) return ''
    return new Date(timeStr).toLocaleString()
}

onMounted(() => {
    fetchCategories()
    fetchList()
})
</script>

<style scoped>
.partner-list {
  padding-bottom: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}
.page-header {
  margin-bottom: 10px;
  padding: 15px 15px 0 15px;
  background-color: #fff;
}
.search-box {
  margin-bottom: 10px;
}
.category-tabs {
    /* Adjust styles if needed */
}

.blog-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 10px;
}

.blog-box {
  background: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
  cursor: pointer;
  transition: all 0.3s;
}
.blog-box:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px 0 rgba(0,0,0,0.1);
}

.blog-title {
    font-size: 16px;
    font-weight: bold;
    color: #333;
    margin-bottom: 8px;
}
.blog-info {
    display: flex;
    gap: 10px;
    font-size: 12px;
    color: #999;
    margin-bottom: 8px;
    align-items: center;
}
.category-tag {
    background-color: #ecf5ff;
    color: #409eff;
    padding: 2px 6px;
    border-radius: 4px;
}
.blog-desc {
    font-size: 14px;
    color: #666;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    line-height: 1.5;
}
</style>
