<template>
  <div class="register-container">
    <div class="register-bg"></div>
    <div class="register-box">
      <div class="register-header">
        <span class="register-logo">🧳</span>
        <h2>注册账号</h2>
        <p class="register-subtitle">加入搭伙行，开启旅行社交</p>
      </div>
      
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password size="large" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" prefix-icon="Lock" show-password size="large" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称（可选）" size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" size="large" style="width: 100%;">注册</el-button>
        </el-form-item>
        <div class="register-links">
          <span>已有账号？<router-link to="/login">立即登录</router-link></span>
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
import { register } from '../api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const res = await register({
      username: form.username,
      password: form.password,
      nickname: form.nickname || form.username
    })
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.msg || '注册失败')
    }
  } catch (e) {
    ElMessage.error('注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 140px);
  position: relative;
  overflow: hidden;
}
.register-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 50%, #667eea 100%);
  z-index: 0;
}
.register-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 80%, rgba(255,255,255,0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.08) 0%, transparent 40%);
}
.register-box {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-xl);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(255, 255, 255, 0.2);
  animation: fadeInUp 0.6s ease;
}
.register-header {
  text-align: center;
  margin-bottom: 32px;
}
.register-logo {
  font-size: 40px;
  display: block;
  margin-bottom: 12px;
}
.register-header h2 {
  margin: 0 0 8px 0;
  color: var(--text-primary);
  font-weight: 700;
  font-size: 24px;
  letter-spacing: 1px;
}
.register-subtitle {
  color: var(--text-muted);
  font-size: 14px;
  margin: 0;
}
.register-links {
  margin-top: 16px;
  font-size: 14px;
  color: var(--text-muted);
  text-align: center;
}
.register-links a {
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
}
.register-links a:hover {
  color: var(--primary-dark);
}
</style>
