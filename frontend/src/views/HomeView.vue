<template>
  <div class="home">
    <div class="home-header">
      <h1>智慧学堂 - 在线视频课程</h1>
      <p class="subtitle">发现更多精彩视频作品</p>
    </div>
    
    <div v-if="loading" class="loading">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载中...</p>
    </div>
    
    <div v-else-if="videos.length === 0" class="empty">
      <el-empty description="暂无视频作品">
        <el-button type="primary" @click="router.push('/upload')">上传第一个视频</el-button>
      </el-empty>
    </div>
    
    <div v-else class="video-list">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="video in videos" :key="video.id">
          <el-card shadow="hover" class="video-card" @click="playVideo(video)">
            <div class="video-cover">
              <img :src="video.coverUrl || 'http://localhost:8080/backend/image/default_image/defaultImage.png'" alt="视频封面" />
              <div class="play-overlay">
                <el-icon :size="40"><VideoPlay /></el-icon>
              </div>
            </div>
            <div class="video-info">
              <h3 class="video-title">{{ video.title }}</h3>
              <p class="video-author">
                <el-avatar :size="24" :src="userInfo.avatar"></el-avatar>
                <span>{{ video.username }}</span>
              </p>
              <div class="video-stats">
                <span><el-icon><View /></el-icon> {{ video.viewCount }}</span>
                <span>{{ formatDate(video.createTime) }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, VideoPlay, View } from '@element-plus/icons-vue'

interface Video {
  id: number;
  title: string;
  description: string;
  videoUrl: string;
  coverUrl: string;
  userId: number;
  username: string;
  viewCount: number;
  createTime: string;
}

interface UserInfo {
  id: number;
  username: string;
  avatar: string;
  role: string;
}

const router = useRouter()

const loading = ref(false)
const videos = ref<Video[]>([])
const searchKeyword = ref('')

const userInfo = ref<UserInfo>({
  id: 0,
  username: '',
  avatar: '',
  role: ''
})

const handleSearch = (event: Event) => {
  const customEvent = event as CustomEvent
  loadVideos(customEvent.detail)
}

onMounted(async () => {
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    userInfo.value = JSON.parse(storedUser)
  }
  
  window.addEventListener('videoSearch', handleSearch)
  loadVideos()
})

onBeforeUnmount(() => {
  window.removeEventListener('videoSearch', handleSearch)
})

const loadVideos = async (keyword: string = '') => {
  loading.value = true
  try {
    let url = 'http://localhost:8080/api/video/list?pageNum=1&pageSize=20'
    if (keyword) {
      url += `&keyword=${encodeURIComponent(keyword)}`
    }
    const response = await fetch(url)
    const data = await response.json()
    
    if (data.success) {
      videos.value = data.data
    } else {
      ElMessage.error(data.message || '加载视频列表失败')
    }
  } catch (error) {
    ElMessage.error('加载视频列表失败')
  } finally {
    loading.value = false
  }
}

const playVideo = async (video: Video) => {
  router.push(`/video?id=${video.id}`)
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.home-header {
  text-align: center;
  margin-bottom: 30px;
}

.home-header h1 {
  margin: 0;
  color: #409eff;
  font-size: 32px;
  font-weight: 600;
}

.home-header .subtitle {
  margin: 10px 0 0;
  color: #909399;
  font-size: 16px;
}

.video-toolbar {
  margin-bottom: 20px;
}

.loading, .empty {
  text-align: center;
  padding: 60px 0;
  color: #909399;
}

.loading .el-icon {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.video-list {
  margin-top: 20px;
}

.video-card {
  cursor: pointer;
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.video-card:hover {
  transform: translateY(-5px);
}

.video-cover {
  position: relative;
  width: 100%;
  height: 150px;
  overflow: hidden;
  background-color: #000;
}

.video-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
}

.video-info {
  padding: 10px;
}

.video-title {
  margin: 0 0 10px;
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-author {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 10px;
  font-size: 12px;
  color: #606266;
}

.video-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.video-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.video-player {
  width: 100%;
  background-color: #000;
}

.video-detail {
  padding: 20px;
}

.video-detail h2 {
  margin: 0 0 10px;
  font-size: 20px;
  color: #303133;
}

.video-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
  color: #909399;
  font-size: 14px;
}

.video-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.video-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}
</style>
