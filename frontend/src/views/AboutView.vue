<template>
  <div class="about">
    <el-card shadow="hover" class="about-card">
      <template #header>
        <div class="about-header">
          <h2>关于本系统</h2>
        </div>
      </template>
      
      <div class="about-content">
        <p class="description">这是一个使用 Vue 3 和 Spring Boot 构建的现代化全栈应用系统。</p>
        
        <el-divider content-position="center">技术栈</el-divider>
        
        <el-row :gutter="30" class="tech-stack">
          <el-col :span="12">
            <el-card shadow="hover" class="tech-card">
              <template #header>
                <div class="tech-header">
                  <el-icon class="tech-icon" :size="24"><Monitor /></el-icon>
                  <span>前端技术</span>
                </div>
              </template>
              <ul class="tech-list">
                <li><strong>Vue 3</strong> - 渐进式JavaScript框架</li>
                <li><strong>TypeScript</strong> - JavaScript超集</li>
                <li><strong>Vite</strong> - 下一代前端构建工具</li>
                <li><strong>Element Plus</strong> - Vue 3 UI组件库</li>
                <li><strong>Vue Router</strong> - 路由管理器</li>
              </ul>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="tech-card">
              <template #header>
                <div class="tech-header">
                  <el-icon class="tech-icon" :size="24"><Cpu /></el-icon>
                  <span>后端技术</span>
                </div>
              </template>
              <ul class="tech-list">
                <li><strong>Spring Boot 2.7.18</strong> - Java开发框架</li>
                <li><strong>Java 8</strong> - 编程语言</li>
                <li><strong>Spring Security</strong> - 安全框架</li>
                <li><strong>JWT</strong> - JSON Web Token</li>
                <li><strong>MyBatis-Plus</strong> - 数据访问</li>
                <li><strong>MySQL</strong> - 关系型数据库</li>
              </ul>
            </el-card>
          </el-col>
        </el-row>
        
        <el-divider content-position="center">系统功能</el-divider>
        
        <div class="features">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="feature-item">
                <el-icon :size="32" color="#409eff"><User /></el-icon>
                <h4>用户管理</h4>
                <p>用户注册、登录、个人信息管理</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="feature-item">
                <el-icon :size="32" color="#409eff"><Lock /></el-icon>
                <h4>安全保障</h4>
                <p>密码加密存储、JWT身份验证</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="feature-item">
                <el-icon :size="32" color="#409eff"><Setting /></el-icon>
                <h4>个人设置</h4>
                <p>修改密码、更新头像</p>
              </div>
            </el-col>
          </el-row>
        </div>
        
        <div class="action-buttons">
          <el-button type="primary" @click="router.push('/')">返回首页</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Monitor, Cpu, User, Lock, Setting } from '@element-plus/icons-vue'

const router = useRouter()
</script>

<style scoped>
.about {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 70vh;
}

.about-card {
  width: 100%;
  max-width: 900px;
  border-radius: 8px;
}

.about-header {
  text-align: center;
}

.about-header h2 {
  margin: 0;
  color: #409eff;
  font-size: 24px;
  font-weight: 600;
}

.about-content {
  padding: 20px;
}

.description {
  text-align: center;
  font-size: 16px;
  color: #606266;
  margin: 20px 0;
}

.tech-stack {
  margin: 20px 0;
}

.tech-card {
  height: 100%;
  border-radius: 8px;
}

.tech-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
}

.tech-icon {
  color: #409eff;
}

.tech-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tech-list li {
  padding: 8px 0;
  color: #606266;
  font-size: 14px;
}

.tech-list li strong {
  color: #303133;
}

.features {
  margin: 30px 0;
}

.feature-item {
  text-align: center;
  padding: 20px;
}

.feature-item h4 {
  margin: 10px 0;
  color: #303133;
}

.feature-item p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>