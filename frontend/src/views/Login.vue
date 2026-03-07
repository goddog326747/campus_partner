<template>
  <div class="login-container">
    <div class="login-box">
      <h2>欢迎来到搭伙行</h2>
      <el-form :model="form" :rules="rules" ref="loginForm" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名/学号" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">登录</el-button>
        </el-form-item>
        <div class="login-links">
          <span>还没有账号？<a href="#" @click.prevent="ElMessage.info('注册功能开发中，请联系管理员')">立即注册</a></span>
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
        // 调用 Vuex action
        const success = await store.dispatch('login', form)
        if (success) {
          ElMessage.success('登录成功')
          router.push('/')
        } else {
          ElMessage.error('登录失败')
        }
      } catch (error) {
        // 开发阶段模拟登录 (如果后端未启动)
        if (import.meta.env.DEV) {
          console.warn('后端未连接，使用模拟登录')
          store.commit('SET_TOKEN', 'mock-token-123')
          store.commit('SET_USER', { id: 1, nickname: '测试用户', avatar: '' })
          ElMessage.success('模拟登录成功')
          router.push('/')
        } else {
          ElMessage.error('登录服务不可用')
        }
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
  height: 100vh;
  background-color: #f5f7fa;
  background-image: linear-gradient(120deg, #e0c3fc 0%, #8ec5fc 100%);
}

.login-box {
  width: 350px;
  padding: 40px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  text-align: center;
}

h2 {
  margin-bottom: 30px;
  color: #333;
  font-weight: 600;
}

.login-btn {
  width: 100%;
  padding: 12px 0;
  font-size: 16px;
}

.login-links {
  margin-top: 15px;
  font-size: 14px;
  color: #666;
}

.login-links a {
  color: #409eff;
  text-decoration: none;
}
</style>
