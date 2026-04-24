<template>
  <div class="home">
    <div class="banner">
      <div class="banner-bg"></div>
      <div class="banner-content">
        <h2 class="banner-title">欢迎来到搭伙行</h2>
        <p class="banner-desc">寻找志同道合的旅行伙伴，开启一段难忘的旅程。</p>
        <el-button type="primary" size="large" class="banner-btn" @click="$router.push('/partners')">
          <span>立即寻找伙伴</span>
          <el-icon style="margin-left: 6px;"><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>
    
    <div class="features">
      <el-row :gutter="24">
        <el-col :span="8">
          <div class="feature-card fade-in-up" style="animation-delay: 0.1s;">
            <div class="feature-icon" style="background: var(--gradient-cool);">
              <span>🗺️</span>
            </div>
            <h3>发布行程</h3>
            <p>分享你的旅行计划，等待伙伴加入。</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="feature-card fade-in-up" style="animation-delay: 0.25s;">
            <div class="feature-icon" style="background: var(--gradient-warm);">
              <span>🤝</span>
            </div>
            <h3>寻找伙伴</h3>
            <p>浏览他人的行程，找到合适的旅伴。</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="feature-card fade-in-up" style="animation-delay: 0.4s;">
            <div class="feature-icon" style="background: var(--gradient-sunset);">
              <span>🛡️</span>
            </div>
            <h3>安全出行</h3>
            <p>实名认证，评价体系，让旅行更安心。</p>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ArrowRight } from '@element-plus/icons-vue'
</script>

<style scoped>
.banner {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: 32px;
  min-height: 220px;
  display: flex;
  align-items: center;
}
.banner-bg {
  position: absolute;
  inset: 0;
  background: var(--gradient-header);
  opacity: 1;
}
.banner-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(255,255,255,0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.1) 0%, transparent 40%);
}
.banner-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 48px 32px;
  width: 100%;
}
.banner-title {
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 12px 0;
  letter-spacing: 2px;
}
.banner-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 28px 0;
  letter-spacing: 0.5px;
}
.banner-btn {
  padding: 12px 32px !important;
  font-size: 16px !important;
  border-radius: var(--radius-xl) !important;
  background: rgba(255, 255, 255, 0.2) !important;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  color: #fff !important;
  transition: all var(--transition-normal) !important;
}
.banner-btn:hover {
  background: rgba(255, 255, 255, 0.35) !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15) !important;
}

.features {
  margin-top: 8px;
}
.feature-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 32px 24px;
  text-align: center;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
  border: 1px solid var(--border-light);
}
.feature-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
}
.feature-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 28px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.feature-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 10px 0;
}
.feature-card p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.6;
}
</style>
