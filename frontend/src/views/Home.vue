<template>
  <div class="home">
    <el-card class="banner">
      <h2>欢迎来到搭伙行</h2>
      <p>寻找志同道合的旅行伙伴，开启一段难忘的旅程。</p>
      <el-button type="primary" @click="$router.push('/partners')">立即寻找伙伴</el-button>
    </el-card>
    
    <div class="features">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card>
            <h3>发布行程</h3>
            <p>分享你的旅行计划，等待伙伴加入。</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <h3>寻找伙伴</h3>
            <p>浏览他人的行程，找到合适的旅伴。</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <h3>安全出行</h3>
            <p>实名认证，评价体系，让旅行更安心。</p>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
.banner {
  text-align: center;
  padding: 40px;
  margin-bottom: 20px;
  background-color: #ecf5ff;
}
.features {
  margin-top: 20px;
}
</style>
