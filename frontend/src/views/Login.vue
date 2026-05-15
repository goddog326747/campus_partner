<template>
  <div class="login-container">
    <div class="login-bg"></div>
    <div class="login-box">
      <div class="login-header">
        <span class="login-logo">🧳</span>
        <h2>欢迎来到搭伙行</h2>
        <p class="login-subtitle">开启你的旅行社交之旅</p>
      </div>
      <el-form :model="form" :rules="rules" ref="loginForm" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名/学号" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin" size="large">登录</el-button>
        </el-form-item>
        <div class="login-links">
          <span>还没有账号？<router-link to="/register">立即注册</router-link></span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'

const store = useStore()
const router = useRouter()
const loginForm = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = () => {
  loginForm.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await store.dispatch('login', form)
        if (success) {
          ElMessage.success('登录成功')
          router.push('/')
        } else {
          ElMessage.error('登录失败')
        }
      } catch (error) {
        console.error('登录失败:', error)
        ElMessage.error('登录失败，请检查网络连接或稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 140px);
  position: relative;
  overflow: hidden;
}
.login-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  z-index: 0;
}
.login-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 30% 20%, rgba(255,255,255,0.12) 0%, transparent 50%),
    radial-gradient(circle at 70% 80%, rgba(255,255,255,0.08) 0%, transparent 40%);
}
.login-box {
  position: relative;
  z-index: 1;
  width: 400px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-xl);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(255, 255, 255, 0.2);
  animation: fadeInUp 0.6s ease;
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
}
.login-logo {
  font-size: 40px;
  display: block;
  margin-bottom: 12px;
}
.login-header h2 {
  margin: 0 0 8px 0;
  color: var(--text-primary);
  font-weight: 700;
  font-size: 24px;
  letter-spacing: 1px;
}
.login-subtitle {
  color: var(--text-muted);
  font-size: 14px;
  margin: 0;
}
.login-btn {
  width: 100%;
  padding: 14px 0 !important;
  font-size: 16px !important;
  border-radius: var(--radius-sm) !important;
  letter-spacing: 2px;
}
.login-links {
  margin-top: 16px;
  font-size: 14px;
  color: var(--text-muted);
  text-align: center;
}
.login-links a {
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
}
.login-links a:hover {
  color: var(--primary-dark);
}
</style>
