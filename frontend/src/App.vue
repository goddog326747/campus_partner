<template>
  <el-container class="layout-container">
    <el-header>
      <div class="header-content">
        <h1>搭伙行</h1>
        <el-menu mode="horizontal" router :default-active="$route.path" :ellipsis="false">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/partners">寻找搭伙</el-menu-item>
          <el-menu-item index="/post/create">发帖</el-menu-item>
          <el-menu-item v-if="!isLoggedIn" index="/login">登录</el-menu-item>
          <el-sub-menu v-else index="user">
            <template #title>
              <span style="display: flex; align-items: center;">
                <el-avatar :size="24" :src="currentUser?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" style="margin-right: 8px;" />
                {{ currentUser?.nickname || '用户' }}
              </span>
            </template>
            <el-menu-item index="/profile">个人中心</el-menu-item>
            <el-menu-item @click="handleLogout">退出登录</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
    <el-footer>
      <p>&copy; 2025 搭伙行 - 寻找你的旅行伙伴</p>
    </el-footer>
    <AIAssistant />
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import AIAssistant from './components/AIAssistant.vue'

const store = useStore()
const router = useRouter()

const isLoggedIn = computed(() => store.getters.isLoggedIn)
const currentUser = computed(() => store.getters.currentUser)

const handleLogout = () => {
  store.commit('LOGOUT')
  router.push('/login')
}
</script>

<style>
.layout-container {
  min-height: 100vh;
}
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
}
.el-header {
  border-bottom: 1px solid #dcdfe6;
}
.el-main {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 20px;
}
.el-footer {
  text-align: center;
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
