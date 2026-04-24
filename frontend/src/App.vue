<template>
  <el-container class="layout-container">
    <el-header class="app-header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-icon">🧳</span>
          <h1 class="logo-text">搭伙行</h1>
        </div>
        <el-menu mode="horizontal" router :default-active="$route.path" :ellipsis="false" class="nav-menu">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/partners">寻找搭伙</el-menu-item>
          <el-menu-item index="/post/create">发帖</el-menu-item>
          <el-menu-item v-if="!isLoggedIn" index="/login">登录</el-menu-item>
          <el-sub-menu v-else index="user">
            <template #title>
              <span style="display: flex; align-items: center;">
                <el-avatar :size="28" :src="currentUser?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" style="margin-right: 8px; border: 2px solid rgba(255,255,255,0.3);" />
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
    <el-footer class="app-footer">
      <div class="footer-content">
        <p>&copy; 2025 搭伙行 - 寻找你的旅行伙伴</p>
      </div>
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
  background-color: var(--bg-page);
}
.app-header {
  background: var(--gradient-header);
  border-bottom: none;
  box-shadow: 0 2px 20px rgba(102, 126, 234, 0.2);
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(12px);
}
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: transform var(--transition-fast);
}
.logo:hover {
  transform: scale(1.02);
}
.logo-icon {
  font-size: 28px;
}
.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin: 0;
  letter-spacing: 1px;
}
.nav-menu {
  background: transparent !important;
}
.nav-menu .el-menu-item {
  color: rgba(255, 255, 255, 0.85) !important;
  border-bottom: none !important;
  font-weight: 500;
  font-size: 15px;
  padding: 0 20px;
  transition: all var(--transition-fast) !important;
}
.nav-menu .el-menu-item:hover {
  color: #fff !important;
  background-color: rgba(255, 255, 255, 0.1) !important;
}
.nav-menu .el-menu-item.is-active {
  color: #fff !important;
  background-color: rgba(255, 255, 255, 0.15) !important;
}
.nav-menu .el-sub-menu__title {
  color: rgba(255, 255, 255, 0.85) !important;
  border-bottom: none !important;
}
.nav-menu .el-sub-menu__title:hover {
  color: #fff !important;
  background-color: rgba(255, 255, 255, 0.1) !important;
}
.el-main {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 24px 20px;
}
.app-footer {
  text-align: center;
  background: linear-gradient(180deg, var(--bg-page) 0%, #e8eaf0 100%);
  padding: 24px 20px;
  border-top: 1px solid var(--border-light);
}
.footer-content p {
  color: var(--text-muted);
  font-size: 14px;
  margin: 0;
  letter-spacing: 0.5px;
}
</style>
