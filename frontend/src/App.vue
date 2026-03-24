<script setup lang="ts">
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, Bell } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

const isLoggedIn = ref(false)
const userInfo = ref({
  username: '',
  avatar: ''
})
const unreadCount = ref(0)

const activeIndex = computed(() => route.path)

const updateUserStatus = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    isLoggedIn.value = true
    userInfo.value = {
      username: user.username,
      avatar: user.avatar || 'http://localhost:8080/api/avatar/default'
    }
    fetchUnreadCount()
  } else {
    isLoggedIn.value = false
    userInfo.value = {
      username: '',
      avatar: ''
    }
    unreadCount.value = 0
  }
}

const fetchUnreadCount = async () => {
  try {
    const token = localStorage.getItem('token')
    if (token) {
      const [notificationRes, chatRes] = await Promise.all([
        axios.get('http://localhost:8080/api/notification/unread-count', {
          headers: { 'Authorization': `Bearer ${token}` }
        }),
        axios.get('http://localhost:8080/api/chat/unread-count', {
          headers: { 'Authorization': `Bearer ${token}` }
        })
      ])
      
      let total = 0
      if (notificationRes.data.success && notificationRes.data.count !== undefined) {
        total += notificationRes.data.count
      }
      if (chatRes.data.success && chatRes.data.unreadCount !== undefined) {
        total += chatRes.data.unreadCount
      }
      unreadCount.value = total
    }
  } catch (error) {
    console.error('获取未读消息数失败:', error)
  }
}

onMounted(() => {
  updateUserStatus()
  window.addEventListener('loginSuccess', updateUserStatus)
  window.addEventListener('registerSuccess', updateUserStatus)
  window.addEventListener('notificationRead', fetchUnreadCount)
})

onBeforeUnmount(() => {
  window.removeEventListener('loginSuccess', updateUserStatus)
  window.removeEventListener('registerSuccess', updateUserStatus)
  window.removeEventListener('notificationRead', fetchUnreadCount)
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  isLoggedIn.value = false
  userInfo.value = {
    username: '',
    avatar: ''
  }
  unreadCount.value = 0
  router.push('/login')
}

const handleDropdownCommand = (command: string) => {
  if (command === 'logout') {
    handleLogout()
  } else {
    router.push(command)
  }
}

const searchQuery = ref('')
const handleSearch = () => {
  if (searchQuery.value.trim()) {
    window.dispatchEvent(new CustomEvent('videoSearch', { detail: searchQuery.value }))
    router.push('/')
  }
}
</script>

<template>
  <div class="app">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="header-content">
        <div class="logo">
          <router-link to="/">
            <h1>智慧学堂</h1>
          </router-link>
        </div>
        <div class="nav">
          <router-link to="/" :class="{ active: activeIndex === '/' }">首页</router-link>
          <router-link to="/about" :class="{ active: activeIndex === '/about' }">关于</router-link>
          <div class="search-box">
            <el-input
              v-model="searchQuery"
              placeholder="搜索视频..."
              @keyup.enter="handleSearch"
              clearable
            >
              <template #append>
                <el-button :icon="Search" @click="handleSearch" />
              </template>
            </el-input>
          </div>
          <template v-if="isLoggedIn">
            <div class="notification-bell" @click="router.push('/notifications')">
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
                <el-icon :size="20"><Bell /></el-icon>
              </el-badge>
            </div>
            <div class="user">
              <el-dropdown trigger="hover" @command="handleDropdownCommand">
                <div class="user-info">
                  <el-avatar :size="32" :src="userInfo.avatar"></el-avatar>
                  <span class="username">{{ userInfo.username }}</span>
                  <i class="el-icon-arrow-down el-icon--right"></i>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="/creator">创作者中心</el-dropdown-item>
                    <el-dropdown-item command="/user">用户中心</el-dropdown-item>
                    <el-dropdown-item command="/notifications">
                      <span>消息</span>
                      <el-badge v-if="unreadCount > 0" :value="unreadCount" class="notification-badge" />
                    </el-dropdown-item>
                    <el-dropdown-item divided command="logout">登出</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
          <router-link v-else to="/login" class="login-btn">登录</router-link>
        </div>
      </div>
    </div>
    
    <!-- 主内容区域 -->
    <div class="main">
      <router-view />
    </div>
  </div>
</template>



<style scoped>
/* 全屏布局 */
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 顶部导航 */
.header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo h1 {
  font-size: 20px;
  color: #409eff;
  font-weight: 600;
  margin: 0;
}

.logo a {
  text-decoration: none;
}

.nav {
  display: flex;
  align-items: center;
  gap: 24px;
}

.nav a {
  text-decoration: none;
  color: #606266;
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s;
}

.nav a:hover {
  color: #409eff;
  background-color: #ecf5ff;
}

.nav a.active {
  color: #409eff;
  background-color: #ecf5ff;
}

.login-btn {
  background-color: #409eff !important;
  color: #fff !important;
}

.login-btn:hover {
  background-color: #66b1ff !important;
}

.search-box {
  width: 250px;
}

.search-box :deep(.el-input__wrapper) {
  border-radius: 4px 0 0 4px;
}

.search-box :deep(.el-input-group__append) {
  border-radius: 0 4px 4px 0;
}

.user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.user-info:hover {
  background-color: #ecf5ff;
}

.username {
  color: #606266;
  font-size: 14px;
}

.notification-bell {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s;
  color: #606266;
}

.notification-bell:hover {
  background-color: #ecf5ff;
  color: #409eff;
}

.notification-badge {
  margin-left: 8px;
}

/* 主内容区域 */
.main {
  flex: 1;
  padding: 40px 20px;
}

/* 页脚 */
.footer {
  background-color: #fff;
  border-top: 1px solid #e4e7ed;
  padding: 30px 20px;
  text-align: center;
}

.footer p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }
  
  .nav {
    gap: 16px;
  }
  
  .main {
    padding: 20px 16px;
  }
  
  .logo h1 {
    font-size: 18px;
  }
}
</style>