<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Message, Bell } from '@element-plus/icons-vue'
import axios from 'axios'

interface Notification {
  id: number
  userId: number
  type: string
  title: string
  content: string
  relatedId: number
  fromUserId: number
  fromUsername: string
  fromUserAvatar: string
  isRead: boolean
  createTime: string
}

const router = useRouter()
const notifications = ref<Notification[]>([])
const unreadCount = ref(0)
const loading = ref(true)
const activeMenu = ref('my')

const myNotifications = computed(() => {
  return notifications.value.filter(n => n.type === 'COMMENT')
})

const systemNotifications = computed(() => {
  return notifications.value.filter(n => n.type === 'FAVORITE' || n.type === 'LIKE' || n.type === 'FOLLOW' || n.type === 'SYSTEM')
})

const currentNotifications = computed(() => {
  return activeMenu.value === 'my' ? myNotifications.value : systemNotifications.value
})

const currentUnreadCount = computed(() => {
  const list = activeMenu.value === 'my' ? myNotifications.value : systemNotifications.value
  return list.filter(n => !n.isRead).length
})

const fetchNotifications = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('http://localhost:8080/api/notification/list', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    if (response.data.success) {
      notifications.value = response.data.notifications
      unreadCount.value = response.data.unreadCount
    }
  } catch (error) {
    console.error('获取通知失败:', error)
  } finally {
    loading.value = false
  }
}

const markAsRead = async (notification: Notification) => {
  if (notification.isRead) {
    goToVideo(notification.relatedId)
    return
  }
  
  try {
    const token = localStorage.getItem('token')
    const response = await axios.post(`http://localhost:8080/api/notification/${notification.id}/read`, {}, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    if (response.data.success) {
      notification.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
      window.dispatchEvent(new CustomEvent('notificationRead'))
    }
    goToVideo(notification.relatedId)
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

const markAllAsRead = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.post('http://localhost:8080/api/notification/read-all', {}, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    if (response.data.success) {
      notifications.value.forEach(n => n.isRead = true)
      unreadCount.value = 0
      window.dispatchEvent(new CustomEvent('notificationRead'))
    }
  } catch (error) {
    console.error('全部标记已读失败:', error)
  }
}

const goToVideo = (videoId: number) => {
  if (videoId) {
    router.push({ path: '/video', query: { id: videoId } })
  }
}

const goToUserProfile = (event: Event, userId: number) => {
  event.stopPropagation()
  if (userId) {
    router.push({ path: '/profile', query: { id: userId } })
  }
}

const getAvatarUrl = (avatar: string) => {
  if (!avatar) {
    return 'http://localhost:8080/api/avatar/default'
  }
  if (avatar.startsWith('http')) {
    return avatar
  }
  return `http://localhost:8080${avatar}`
}

const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  
  return date.toLocaleDateString()
}

const handleMenuSelect = (key: string) => {
  activeMenu.value = key
}

onMounted(() => {
  fetchNotifications()
})
</script>

<template>
  <div class="notifications-page">
    <div class="sidebar">
      <div class="sidebar-header">
        <h3>消息中心</h3>
      </div>
      <div class="sidebar-menu">
        <div 
          class="menu-item" 
          :class="{ active: activeMenu === 'my' }"
          @click="handleMenuSelect('my')"
        >
          <el-icon><Message /></el-icon>
          <span>我的消息</span>
          <el-badge 
            v-if="myNotifications.filter(n => !n.isRead).length > 0" 
            :value="myNotifications.filter(n => !n.isRead).length" 
            :max="99"
            class="menu-badge"
          />
        </div>
        <div 
          class="menu-item" 
          :class="{ active: activeMenu === 'system' }"
          @click="handleMenuSelect('system')"
        >
          <el-icon><Bell /></el-icon>
          <span>系统通知</span>
          <el-badge 
            v-if="systemNotifications.filter(n => !n.isRead).length > 0" 
            :value="systemNotifications.filter(n => !n.isRead).length" 
            :max="99"
            class="menu-badge"
          />
        </div>
      </div>
    </div>
    
    <div class="main-content">
      <div class="content-header">
        <h2>{{ activeMenu === 'my' ? '我的消息' : '系统通知' }}</h2>
        <el-button 
          v-if="currentUnreadCount > 0" 
          type="primary" 
          text 
          @click="markAllAsRead"
        >
          全部已读
        </el-button>
      </div>
      
      <div class="content-body" v-loading="loading">
        <div v-if="currentNotifications.length === 0 && !loading" class="empty-state">
          <el-empty :description="activeMenu === 'my' ? '暂无消息通知' : '暂无系统通知'" />
        </div>
        
        <div v-else class="notification-list">
          <div 
            v-for="notification in currentNotifications" 
            :key="notification.id"
            class="notification-item"
            :class="{ unread: !notification.isRead }"
            @click="markAsRead(notification)"
          >
            <div class="notification-avatar" @click="goToUserProfile($event, notification.fromUserId)">
              <el-avatar :size="48" :src="getAvatarUrl(notification.fromUserAvatar)" />
            </div>
            <div class="notification-main">
              <div class="notification-header">
                <span class="notification-title">{{ notification.title }}</span>
                <span class="notification-time">{{ formatTime(notification.createTime) }}</span>
              </div>
              <div class="notification-content">{{ notification.content }}</div>
            </div>
            <div class="notification-dot" v-if="!notification.isRead"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.notifications-page {
  display: flex;
  max-width: 1200px;
  margin: 0 auto;
  min-height: calc(100vh - 144px);
  background-color: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
}

.sidebar {
  width: 220px;
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
  font-weight: 600;
}

.sidebar-menu {
  padding: 12px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  cursor: pointer;
  transition: all 0.3s;
  color: #606266;
  position: relative;
}

.menu-item:hover {
  background-color: #f5f7fa;
  color: #409eff;
}

.menu-item.active {
  background-color: #ecf5ff;
  color: #409eff;
  border-right: 3px solid #409eff;
}

.menu-item .el-icon {
  margin-right: 10px;
  font-size: 18px;
}

.menu-item span {
  flex: 1;
  font-size: 14px;
}

.menu-badge {
  margin-left: 8px;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fff;
}

.content-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
  font-weight: 600;
}

.content-body {
  flex: 1;
  padding: 20px 24px;
  overflow-y: auto;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.notification-item:hover {
  background-color: #f5f7fa;
  border-color: #c0c4cc;
  transform: translateX(4px);
}

.notification-item.unread {
  background-color: #ecf5ff;
  border-color: #b3d8ff;
}

.notification-avatar {
  margin-right: 16px;
  flex-shrink: 0;
  cursor: pointer;
  transition: transform 0.3s;
}

.notification-avatar:hover {
  transform: scale(1.1);
}

.notification-main {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.notification-time {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
  margin-left: 12px;
}

.notification-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notification-dot {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 10px;
  height: 10px;
  background-color: #f56c6c;
  border-radius: 50%;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .notifications-page {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #e4e7ed;
  }
  
  .sidebar-menu {
    display: flex;
    padding: 0;
    overflow-x: auto;
  }
  
  .menu-item {
    flex: 1;
    justify-content: center;
    padding: 14px;
    border-right: none;
    border-bottom: 3px solid transparent;
  }
  
  .menu-item.active {
    border-right: none;
    border-bottom: 3px solid #409eff;
  }
  
  .content-body {
    padding: 16px;
  }
  
  .notification-item {
    padding: 12px;
  }
  
  .notification-avatar {
    display: none;
  }
}
</style>
