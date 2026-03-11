<template>
  <div class="user-center">
    <div class="center-header">
      <h1>用户中心</h1>
    </div>
    
    <div class="center-content">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="menu-card" @click="router.push('/settings')">
            <div class="menu-icon">
              <el-icon :size="40"><User /></el-icon>
            </div>
            <h3>个人设置</h3>
            <p>修改个人信息、头像、密码</p>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="menu-card" @click="router.push('/')">
            <div class="menu-icon">
              <el-icon :size="40"><VideoCamera /></el-icon>
            </div>
            <h3>观看历史</h3>
            <p>查看您观看过的视频</p>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="menu-card" @click="router.push('/favorites')">
            <div class="menu-icon">
              <el-icon :size="40"><Star /></el-icon>
            </div>
            <h3>我的收藏</h3>
            <p>收藏您喜欢的视频</p>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { User, VideoCamera, Star } from '@element-plus/icons-vue'

const router = useRouter()
</script>

<style scoped>
.user-center {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.center-header {
  text-align: center;
  margin-bottom: 40px;
}

.center-header h1 {
  margin: 0;
  color: #409eff;
  font-size: 28px;
  font-weight: 600;
}

.center-content {
  margin-top: 20px;
}

.menu-card {
  cursor: pointer;
  text-align: center;
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.menu-card:hover {
  transform: translateY(-5px);
}

.menu-icon {
  color: #409eff;
  margin-bottom: 16px;
}

.menu-card h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #303133;
}

.menu-card p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}
</style>
