<template>
  <div class="favorites-page">
    <div class="page-header">
      <h1>我的收藏</h1>
      <p class="subtitle">共收藏 {{ favorites.length }} 个视频</p>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载中...</p>
    </div>
    
    <div v-else-if="favorites.length === 0" class="empty-container">
      <el-icon :size="80"><Star /></el-icon>
      <p>暂无收藏的视频</p>
      <el-button type="primary" @click="router.push('/')">去发现</el-button>
    </div>
    
    <div v-else class="video-grid">
      <div 
        v-for="video in favorites" 
        :key="video.id" 
        class="video-card"
        @click="goToVideo(video.id)"
      >
        <div class="video-cover">
          <img :src="video.coverUrl || defaultCover" :alt="video.title" />
          <div class="video-overlay">
            <span class="play-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M8 5v14l11-7z"/>
              </svg>
            </span>
          </div>
          <span class="duration">{{ formatDuration(video.duration) }}</span>
        </div>
        <div class="video-info">
          <h3 class="video-title">{{ video.title }}</h3>
          <div class="video-meta">
            <span class="author">
              <el-avatar :size="24" :src="video.avatar">
                {{ video.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              {{ video.username }}
            </span>
            <span class="stats">
              <el-icon><View /></el-icon>
              {{ video.viewCount }} 次播放
            </span>
          </div>
          <div class="favorite-time">
            <el-icon><Clock /></el-icon>
            收藏于 {{ formatDate(video.favoriteTime) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, Loading, View, Clock } from '@element-plus/icons-vue'

const router = useRouter()
const favorites = ref<any[]>([])
const loading = ref(true)
const defaultCover = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTYwIiBoZWlnaHQ9IjkwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxyZWN0IHdpZHRoPSIxNjAiIGhlaWdodD0iOTAiIGZpbGw9IiNlZWUiLz48dGV4dCB4PSI1MCUiIHk9IjUwJSIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZmlsbD0iIzk5OSI+5aSx5pWI5a2QPC90ZXh0Pjwvc3ZnPg=='

interface Video {
  id: number
  title: string
  description: string
  videoUrl: string
  coverUrl: string
  viewCount: number
  userId: number
  username: string
  createTime: string
  favoriteTime: string
}

const loadFavorites = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    const response = await fetch('http://localhost:8080/api/video/favorites', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const data = await response.json()
    
    if (data.success) {
      favorites.value = data.favorites
    } else {
      ElMessage.error(data.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const goToVideo = (videoId: number) => {
  router.push(`/video?id=${videoId}`)
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN')
}

const formatDuration = (seconds: number) => {
  if (!seconds) return '00:00'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorites-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #303133;
  margin: 0 0 8px;
}

.subtitle {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: #909399;
}

.empty-container p {
  margin: 20px 0;
  font-size: 16px;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.video-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.video-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.video-cover {
  position: relative;
  width: 100%;
  padding-top: 56.25%;
  background: #f5f5f5;
}

.video-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.video-card:hover .video-overlay {
  opacity: 1;
}

.play-icon {
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.play-icon svg {
  width: 24px;
  height: 24px;
  color: #409eff;
  margin-left: 4px;
}

.duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.video-info {
  padding: 16px;
}

.video-title {
  font-size: 16px;
  color: #303133;
  margin: 0 0 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.video-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.author {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.stats {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 12px;
}

.favorite-time {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 12px;
}

@media (max-width: 768px) {
  .video-grid {
    grid-template-columns: 1fr;
  }
}
</style>
