<template>
  <div class="user-profile">
    <div v-if="loading" class="loading">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载中...</p>
    </div>
    
    <template v-else>
      <div class="profile-header">
        <div class="profile-cover"></div>
        <div class="profile-info">
          <div class="avatar-section">
            <el-avatar :size="100" :src="userInfo.avatar || 'http://localhost:8080/backend/image/default_image/defaultImage.png'"></el-avatar>
          </div>
          <div class="user-details">
            <div class="username-section">
              <h2>{{ userInfo.username }}</h2>
              <span v-if="userInfo.signature" class="signature">{{ userInfo.signature }}</span>
            </div>
            <div class="stats-section">
              <div class="stat-item">
                <span class="stat-value">{{ userInfo.videoCount || 0 }}</span>
                <span class="stat-label">视频</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ userInfo.followerCount || 0 }}</span>
                <span class="stat-label">粉丝</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ userInfo.followingCount || 0 }}</span>
                <span class="stat-label">关注</span>
              </div>
            </div>
          </div>
          <div class="action-section" v-if="!isCurrentUser">
            <el-button :type="isFollowing ? 'default' : 'primary'" @click="toggleFollow">
              {{ isFollowing ? '已关注' : '关注' }}
            </el-button>
            <el-button v-if="isFollowing" @click="startChat" :icon="ChatDotRound">
              发私信
            </el-button>
          </div>
        </div>
      </div>
      
      <div class="profile-content">
        <div class="content-header">
          <h3>TA的视频作品</h3>
          <span class="video-count">共 {{ videos.length }} 个</span>
        </div>
        
        <div v-if="videos.length === 0" class="empty">
          <el-empty description="暂无视频作品" />
        </div>
        
        <div v-else class="video-list">
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="video in videos" :key="video.id">
              <div class="video-card" @click="goToVideo(video.id)">
                <div class="video-cover">
                  <img :src="video.coverUrl || 'http://localhost:8080/backend/image/default_image/defaultImage.png'" alt="视频封面" />
                  <div class="play-overlay">
                    <el-icon :size="24"><VideoPlay /></el-icon>
                  </div>
                </div>
                <div class="video-info">
                  <div class="video-title">{{ video.title }}</div>
                  <div class="video-meta">
                    <span>{{ formatViewCount(video.viewCount) }}次观看</span>
                    <span class="dot">·</span>
                    <span>{{ formatDate(video.createTime) }}</span>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, VideoPlay, ChatDotRound } from '@element-plus/icons-vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const userInfo = ref<any>({})
const videos = ref<any[]>([])
const isFollowing = ref(false)

const currentUserId = computed(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    return user.id
  }
  return null
})

const isCurrentUser = computed(() => {
  return currentUserId.value && currentUserId.value === Number(route.query.id)
})

const loadUserProfile = async () => {
  const userId = route.query.id
  if (!userId) {
    ElMessage.error('用户ID不存在')
    return
  }
  
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const headers: Record<string, string> = {}
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    
    const response = await fetch(`http://localhost:8080/api/user/${userId}/videos`, {
      headers
    })
    const data = await response.json()
    
    if (data.success) {
      userInfo.value = data.user
      videos.value = data.videos
    } else {
      ElMessage.error(data.message || '获取用户信息失败')
    }
    
    const statusResponse = await fetch(`http://localhost:8080/api/user/${userId}/follow-status`, {
      headers
    })
    const statusData = await statusResponse.json()
    if (statusData.success) {
      isFollowing.value = statusData.isFollowing
      userInfo.value.followerCount = statusData.followerCount
      userInfo.value.followingCount = statusData.followingCount
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const toggleFollow = async () => {
  const userId = route.query.id
  if (!userId) return
  
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }
    
    const response = await fetch(`http://localhost:8080/api/user/${userId}/follow`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const data = await response.json()
    
    if (data.success) {
      isFollowing.value = data.isFollowing
      userInfo.value.followerCount = data.followerCount
      ElMessage.success(data.isFollowing ? '关注成功' : '已取消关注')
    } else {
      ElMessage.error(data.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const goToVideo = (videoId: number) => {
  router.push(`/video?id=${videoId}`)
}

const startChat = async () => {
  const userId = route.query.id
  if (!userId) return
  
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }
    
    const response = await axios.post(`http://localhost:8080/api/chat/conversation/${userId}`, {}, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.data.success) {
      router.push({ path: '/notifications', query: { chat: response.data.conversationId } })
    } else {
      ElMessage.error(response.data.message || '创建会话失败')
    }
  } catch (error: any) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('操作失败')
    }
  }
}

const formatViewCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toString()
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped>
.user-profile {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.loading {
  text-align: center;
  padding: 100px 0;
  color: #909399;
}

.loading .el-icon {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.profile-header {
  background-color: #fff;
  position: relative;
}

.profile-cover {
  height: 120px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.profile-info {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 20px;
  display: flex;
  align-items: flex-end;
  margin-top: -50px;
}

.avatar-section {
  flex-shrink: 0;
}

.avatar-section .el-avatar {
  border: 4px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.user-details {
  flex: 1;
  margin-left: 20px;
  padding-bottom: 10px;
}

.username-section h2 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #303133;
}

.signature {
  font-size: 14px;
  color: #909399;
}

.stats-section {
  display: flex;
  gap: 30px;
  margin-top: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.action-section {
  padding-bottom: 10px;
}

.profile-content {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.content-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.content-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.video-count {
  font-size: 14px;
  color: #909399;
}

.empty {
  background-color: #fff;
  border-radius: 8px;
  padding: 60px 0;
}

.video-list {
  margin-top: 20px;
}

.video-card {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  margin-bottom: 20px;
}

.video-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.video-cover {
  position: relative;
  width: 100%;
  height: 160px;
  overflow: hidden;
  background-color: #000;
}

.video-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.video-card:hover .video-cover img {
  transform: scale(1.05);
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  opacity: 0;
  transition: opacity 0.3s;
}

.video-card:hover .play-overlay {
  opacity: 1;
}

.play-overlay .el-icon {
  color: #fff;
  background-color: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  padding: 8px;
}

.video-info {
  padding: 12px;
}

.video-title {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  height: 40px;
}

.video-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 8px;
}

.dot {
  font-size: 10px;
}

@media (max-width: 768px) {
  .profile-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
    margin-top: -50px;
  }
  
  .user-details {
    margin-left: 0;
    margin-top: 16px;
  }
  
  .action-section {
    margin-top: 16px;
  }
  
  .stats-section {
    justify-content: center;
  }
}
</style>
