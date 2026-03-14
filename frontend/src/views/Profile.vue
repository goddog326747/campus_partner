<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <div class="avatar-section">
            <el-avatar :size="100" :src="userInfo.avatar || defaultAvatar" />
            <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
            >
              <el-button type="primary" size="small" style="margin-top: 10px;">更换头像</el-button>
            </el-upload>
          </div>
          
          <el-form :model="userInfo" label-width="80px" class="profile-form">
            <el-form-item label="用户名">
              <el-input v-model="userInfo.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="userInfo.nickname" />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="userInfo.gender">
                <el-radio :label="0">保密</el-radio>
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="生日">
              <el-date-picker
                v-model="userInfo.birthday"
                type="date"
                placeholder="选择生日"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item label="所在地">
              <el-input v-model="userInfo.location" placeholder="请输入所在地" />
            </el-form-item>
            <el-form-item label="学校">
              <el-input v-model="userInfo.school" placeholder="请输入学校" />
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input
                v-model="userInfo.bio"
                type="textarea"
                :rows="3"
                placeholder="介绍一下自己吧~"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="联系方式" name="contact">
          <el-form :model="userInfo" label-width="80px" class="profile-form">
            <el-form-item label="手机号">
              <el-input v-model="userInfo.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userInfo.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="微信号">
              <el-input v-model="userInfo.wechat" placeholder="请输入微信号" />
            </el-form-item>
            <el-form-item label="QQ号">
              <el-input v-model="userInfo.qq" placeholder="请输入QQ号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="隐私设置" name="privacy">
          <el-form :model="userInfo" label-width="120px" class="profile-form">
            <el-form-item label="资料可见性">
              <el-radio-group v-model="userInfo.privacyProfile">
                <el-radio :label="0">公开</el-radio>
                <el-radio :label="1">仅关注可见</el-radio>
                <el-radio :label="2">完全私密</el-radio>
              </el-radio-group>
              <div class="privacy-tip">设置谁可以看到你的基本资料（性别、生日、所在地、学校）</div>
            </el-form-item>
            <el-form-item label="联系方式可见性">
              <el-radio-group v-model="userInfo.privacyContact">
                <el-radio :label="0">公开</el-radio>
                <el-radio :label="1">仅关注可见</el-radio>
                <el-radio :label="2">完全私密</el-radio>
              </el-radio-group>
              <div class="privacy-tip">设置谁可以看到你的联系方式（手机、邮箱、微信、QQ）</div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saving">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="账号安全" name="security">
          <el-form :model="passwordForm" label-width="100px" class="profile-form">
            <el-form-item label="当前密码">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword" :loading="changingPassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="高校认证" name="verify">
          <div class="verify-section">
            <div class="verify-status" v-if="userInfo.verified === 2">
              <el-result icon="success" title="已认证" sub-title="您已完成高校认证">
                <template #extra>
                  <el-tag type="success" size="large">
                    <el-icon style="margin-right: 4px;"><CircleCheckFilled /></el-icon>
                    {{ userInfo.school }} 已认证
                  </el-tag>
                </template>
              </el-result>
            </div>
            
            <div class="verify-status" v-else-if="userInfo.verified === 1">
              <el-result icon="warning" title="认证中" sub-title="您的认证申请正在审核中，请耐心等待">
                <template #extra>
                  <el-tag type="warning" size="large">
                    <el-icon style="margin-right: 4px;"><Clock /></el-icon>
                    审核中
                  </el-tag>
                </template>
              </el-result>
            </div>
            
            <div class="verify-form" v-else>
              <el-alert
                title="高校认证"
                type="info"
                description="通过学校邮箱认证后，您的账号将显示认证标识，增加可信度"
                :closable="false"
                show-icon
                style="margin-bottom: 20px;"
              />
              
              <el-form :model="verifyForm" label-width="100px" class="profile-form">
                <el-form-item label="学校">
                  <el-input v-model="verifyForm.school" placeholder="请输入您的学校名称" />
                </el-form-item>
                <el-form-item label="学校邮箱">
                  <el-input v-model="verifyForm.schoolEmail" placeholder="请输入学校邮箱，如：xxx@tsinghua.edu.cn">
                    <template #append>
                      <el-button @click="sendVerifyCode" :disabled="!verifyForm.schoolEmail || sendingCode">
                        {{ sendingCode ? '发送中...' : (countdown > 0 ? `${countdown}s` : '发送验证码') }}
                      </el-button>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item label="验证码">
                  <el-input v-model="verifyForm.code" placeholder="请输入邮箱收到的验证码" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitVerify" :loading="submitting">提交认证</el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled, Clock } from '@element-plus/icons-vue'
import { getUserProfile, updateProfile, updatePassword, updateAvatar } from '../api/auth'

const store = useStore()
const activeTab = ref('basic')
const saving = ref(false)
const changingPassword = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const userInfo = reactive({
  id: null,
  username: '',
  nickname: '',
  avatar: '',
  gender: 0,
  birthday: null,
  bio: '',
  location: '',
  school: '',
  schoolEmail: '',
  verified: 0,
  phone: '',
  email: '',
  wechat: '',
  qq: '',
  privacyProfile: 0,
  privacyContact: 1
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const verifyForm = reactive({
  school: '',
  schoolEmail: '',
  code: ''
})

const sendingCode = ref(false)
const countdown = ref(0)
const submitting = ref(false)

const fetchProfile = async () => {
  try {
    const res = await getUserProfile()
    if (res.code === 200) {
      Object.assign(userInfo, res.data)
      verifyForm.school = res.data.school || ''
      verifyForm.schoolEmail = res.data.schoolEmail || ''
    }
  } catch (e) {
    console.error(e)
  }
}

const saveProfile = async () => {
  saving.value = true
  try {
    const res = await updateProfile(userInfo)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      store.commit('SET_USER', res.data)
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  
  changingPassword.value = true
  try {
    const res = await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  } catch (e) {
    ElMessage.error('修改失败')
  } finally {
    changingPassword.value = false
  }
}

const beforeAvatarUpload = async (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  
  const reader = new FileReader()
  reader.onload = async (e) => {
    const avatar = e.target.result
    try {
      const res = await updateAvatar({ avatar })
      if (res.code === 200) {
        userInfo.avatar = avatar
        store.commit('SET_USER', { ...store.getters.currentUser, avatar })
        ElMessage.success('头像更新成功')
      }
    } catch (err) {
      ElMessage.error('头像上传失败')
    }
  }
  reader.readAsDataURL(file)
  return false
}

const sendVerifyCode = async () => {
  if (!verifyForm.schoolEmail) {
    ElMessage.warning('请输入学校邮箱')
    return
  }
  
  const emailRegex = /^[\w.-]+@[\w.-]+\.(edu\.cn|ac\.[a-z]{2,}|edu)$/
  if (!emailRegex.test(verifyForm.schoolEmail)) {
    ElMessage.warning('请输入正确的学校邮箱（如：xxx@xxx.edu.cn）')
    return
  }
  
  sendingCode.value = true
  try {
    // TODO: 调用后端发送验证码接口
    ElMessage.success('验证码已发送，请查收邮件')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    sendingCode.value = false
  }
}

const submitVerify = async () => {
  if (!verifyForm.school || !verifyForm.schoolEmail || !verifyForm.code) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  submitting.value = true
  try {
    // TODO: 调用后端认证接口
    ElMessage.success('认证申请已提交，请等待审核')
    userInfo.verified = 1
    userInfo.school = verifyForm.school
    userInfo.schoolEmail = verifyForm.schoolEmail
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.profile-card {
  margin-top: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.profile-form {
  max-width: 500px;
  margin-top: 20px;
}

.privacy-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.verify-section {
  padding: 20px 0;
}

.verify-form {
  max-width: 500px;
}
</style>
