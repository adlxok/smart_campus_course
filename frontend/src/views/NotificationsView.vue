<script setup lang="ts">
import { ref, onMounted, computed, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message, Bell, ChatDotRound } from '@element-plus/icons-vue'
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

interface Conversation {
  id: number
  otherUserId: number
  otherUsername: string
  otherUserAvatar: string
  lastMessage: string
  lastMessageTime: string
  unreadCount: number
}

interface Message {
  id: number
  senderId: number
  receiverId: number
  content: string
  createTime: string
  isMine: boolean
}

interface OtherUser {
  id: number
  username: string
  avatar: string
}

const router = useRouter()
const route = useRoute()
const notifications = ref<Notification[]>([])
const conversations = ref<Conversation[]>([])
const messages = ref<Message[]>([])
const currentConversationId = ref<number | null>(null)
const otherUser = ref<OtherUser | null>(null)
const newMessage = ref('')
const unreadCount = ref(0)
const chatUnreadCount = ref(0)
const loading = ref(true)
const chatLoading = ref(false)
const sending = ref(false)
const activeMenu = ref('my')
const messagesContainer = ref<HTMLElement | null>(null)

const myNotifications = computed(() => {
  return notifications.value.filter(n => n.type === 'FAVORITE' || n.type === 'LIKE' || n.type === 'FOLLOW' || n.type === 'COMMENT')
})

const systemNotifications = computed(() => {
  return notifications.value.filter(n => n.type === 'SYSTEM')
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

const fetchConversations = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('http://localhost:8080/api/chat/conversations', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    if (response.data.success) {
      conversations.value = response.data.conversations
      chatUnreadCount.value = conversations.value.reduce((sum, c) => sum + c.unreadCount, 0)
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
  }
}

const selectConversation = async (conversationId: number) => {
  currentConversationId.value = conversationId
  await fetchMessages(conversationId)
  markConversationAsRead(conversationId)
}

const markConversationAsRead = (conversationId: number) => {
  const conversation = conversations.value.find(c => c.id === conversationId)
  if (conversation && conversation.unreadCount > 0) {
    chatUnreadCount.value -= conversation.unreadCount
    conversation.unreadCount = 0
  }
  window.dispatchEvent(new CustomEvent('notificationRead'))
}

const fetchMessages = async (conversationId: number) => {
  chatLoading.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get(`http://localhost:8080/api/chat/messages/${conversationId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    if (response.data.success) {
      messages.value = response.data.messages
      otherUser.value = response.data.otherUser
      nextTick(() => {
        scrollToBottom()
      })
    }
  } catch (error) {
    console.error('获取消息失败:', error)
  } finally {
    chatLoading.value = false
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim() || !otherUser.value || sending.value) return
  
  sending.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await axios.post('http://localhost:8080/api/chat/send', {
      receiverId: otherUser.value.id,
      content: newMessage.value.trim()
    }, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.data.success) {
      newMessage.value = ''
      if (currentConversationId.value) {
        fetchMessages(currentConversationId.value)
        fetchConversations()
      }
    }
  } catch (error: any) {
    if (error.response?.data?.message) {
      alert(error.response.data.message)
    } else {
      console.error('发送消息失败:', error)
    }
  } finally {
    sending.value = false
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
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

const goToOtherUserProfile = () => {
  if (otherUser.value) {
    router.push({ path: '/profile', query: { id: otherUser.value.id } })
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

const formatMessageTime = (time: string) => {
  const date = new Date(time)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

const handleMenuSelect = (key: string) => {
  activeMenu.value = key
  if (key === 'chat' && conversations.value.length > 0 && !currentConversationId.value) {
    selectConversation(conversations.value[0].id)
  }
}

onMounted(async () => {
  await fetchNotifications()
  await fetchConversations()
  
  const chatIdFromQuery = route.query.chat ? Number(route.query.chat) : null
  if (chatIdFromQuery) {
    activeMenu.value = 'chat'
    const exists = conversations.value.find(c => c.id === chatIdFromQuery)
    if (exists) {
      selectConversation(chatIdFromQuery)
    }
  }
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
          :class="{ active: activeMenu === 'chat' }"
          @click="handleMenuSelect('chat')"
        >
          <el-icon><ChatDotRound /></el-icon>
          <span>私信</span>
          <el-badge 
            v-if="chatUnreadCount > 0" 
            :value="chatUnreadCount" 
            :max="99"
            class="menu-badge"
          />
        </div>
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
    
    <!-- 私聊界面 -->
    <div v-if="activeMenu === 'chat'" class="chat-main">
      <div class="chat-sidebar">
        <div class="chat-sidebar-header">
          <h4>会话列表</h4>
        </div>
        <div class="conversation-list" v-loading="loading">
          <div v-if="conversations.length === 0 && !loading" class="empty-conversations">
            <el-empty description="暂无私信" :image-size="60" />
          </div>
          <div 
            v-for="conversation in conversations" 
            :key="conversation.id"
            class="conversation-item"
            :class="{ active: currentConversationId === conversation.id }"
            @click="selectConversation(conversation.id)"
          >
            <el-badge :value="conversation.unreadCount" :hidden="conversation.unreadCount === 0" :max="99">
              <el-avatar :size="40" :src="getAvatarUrl(conversation.otherUserAvatar)" />
            </el-badge>
            <div class="conversation-info">
              <div class="conversation-header">
                <span class="conversation-name">{{ conversation.otherUsername }}</span>
                <span class="conversation-time">{{ formatTime(conversation.lastMessageTime) }}</span>
              </div>
              <div class="conversation-last-message">{{ conversation.lastMessage }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="chat-content">
        <div v-if="!currentConversationId || !otherUser" class="empty-chat">
          <el-empty description="选择一个会话开始聊天" />
        </div>
        <template v-else>
          <div class="chat-header">
            <div class="header-user" @click="goToOtherUserProfile">
              <el-avatar :size="32" :src="getAvatarUrl(otherUser.avatar)" />
              <span class="username">{{ otherUser.username }}</span>
            </div>
          </div>
          
          <div class="chat-messages" ref="messagesContainer" v-loading="chatLoading">
            <div class="message-list">
              <div 
                v-for="message in messages" 
                :key="message.id"
                class="message-item"
                :class="{ mine: message.isMine }"
              >
                <div v-if="!message.isMine" class="message-avatar">
                  <el-avatar :size="32" :src="getAvatarUrl(otherUser.avatar)" />
                </div>
                <div class="message-content">
                  <div class="message-bubble">{{ message.content }}</div>
                  <div class="message-time">{{ formatMessageTime(message.createTime) }}</div>
                </div>
                <div v-if="message.isMine" class="message-avatar">
                  <el-avatar :size="32" :src="getAvatarUrl('')" />
                </div>
              </div>
            </div>
          </div>
          
          <div class="chat-input">
            <el-input
              v-model="newMessage"
              placeholder="输入消息..."
              @keyup.enter="sendMessage"
              :disabled="sending"
            />
            <el-button type="primary" @click="sendMessage" :loading="sending" class="send-button">发送</el-button>
          </div>
        </template>
      </div>
    </div>
    
    <!-- 通知列表 -->
    <div v-else class="main-content">
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
        <div v-if="currentNotifications.length === 0" class="empty-state">
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
  height: calc(100vh - 144px);
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

/* 私聊界面样式 */
.chat-main {
  flex: 1;
  display: flex;
  background-color: #f5f7fa;
  overflow: hidden;
}

.chat-sidebar {
  width: 260px;
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-sidebar-header {
  padding: 14px 16px;
  border-bottom: 1px solid #e4e7ed;
}

.chat-sidebar-header h4 {
  margin: 0;
  font-size: 14px;
  color: #303133;
  font-weight: 600;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
}

.empty-conversations {
  padding: 30px 16px;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 1px solid #f0f0f0;
}

.conversation-item:hover {
  background-color: #f5f7fa;
}

.conversation-item.active {
  background-color: #ecf5ff;
}

.conversation-item .el-badge {
  margin-right: 10px;
  flex-shrink: 0;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2px;
}

.conversation-name {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.conversation-time {
  font-size: 11px;
  color: #909399;
}

.conversation-last-message {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f0f2f5;
  overflow: hidden;
}

.empty-chat {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.header-user:hover .username {
  color: #409eff;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.message-item.mine {
  justify-content: flex-end;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: 60%;
}

.message-bubble {
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
}

.message-item:not(.mine) .message-bubble {
  background-color: #fff;
  color: #303133;
  border-bottom-left-radius: 4px;
}

.message-item.mine .message-bubble {
  background-color: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message-time {
  font-size: 10px;
  color: #909399;
  margin-top: 2px;
  text-align: center;
}

.chat-input {
  padding: 10px 12px;
  background-color: #fff;
  border-top: 1px solid #e4e7ed;
  display: flex;
  gap: 8px;
  align-items: center;
}

.chat-input .el-input {
  flex: 1;
  --el-input-border-radius: 16px;
}

.chat-input :deep(.el-input__wrapper) {
  border-radius: 16px;
  height: 36px;
}

.send-button {
  border-radius: 16px;
  height: 36px;
  padding: 0 16px;
  font-size: 13px;
}

/* 通知列表样式 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  overflow: hidden;
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
  
  .chat-main {
    flex-direction: column;
  }
  
  .chat-sidebar {
    width: 100%;
    height: 180px;
    border-right: none;
    border-bottom: 1px solid #e4e7ed;
  }
  
  .conversation-list {
    display: flex;
    overflow-x: auto;
    padding: 0 8px;
  }
  
  .conversation-item {
    flex-direction: column;
    align-items: center;
    padding: 8px 10px;
    min-width: 70px;
    border-bottom: none;
  }
  
  .conversation-item .el-badge {
    margin-right: 0;
    margin-bottom: 4px;
  }
  
  .conversation-info {
    text-align: center;
  }
  
  .conversation-header {
    flex-direction: column;
    gap: 2px;
  }
  
  .conversation-last-message {
    display: none;
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
  
  .message-content {
    max-width: 75%;
  }
}
</style>
