<template>
  <div class="home">
    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="content-container">
        <!-- 轮播图 -->
        <div v-if="!loading" class="carousel-container">
          <el-carousel :interval="5000" type="card" height="200px">
            <el-carousel-item v-for="item in carouselItems" :key="item.id">
              <div class="carousel-item" @click="playVideo(item.video)">
                <img :src="item.image" alt="轮播图" />
                <div class="carousel-content">
                  <h3>{{ item.title }}</h3>
                  <p>{{ item.description }}</p>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>

        <!-- 视频分类导航 -->
        <div class="category-nav">
          <el-tabs v-model="activeCategory" class="category-tabs" @tab-change="handleCategoryChange">
            <el-tab-pane label="全部" :name="0"></el-tab-pane>
            <el-tab-pane 
              v-for="cat in categories" 
              :key="cat.id" 
              :label="cat.name" 
              :name="cat.id"
            ></el-tab-pane>
          </el-tabs>
        </div>

        <!-- 视频列表 -->
        <div v-if="loading" class="loading">
          <el-icon class="is-loading" :size="40"><Loading /></el-icon>
          <p>加载中...</p>
        </div>
        
        <div v-else-if="videos.length === 0" class="empty">
          <el-empty description="暂无视频作品">
            <el-button type="primary" @click="router.push('/creator')">上传第一个视频</el-button>
          </el-empty>
        </div>
        
        <div v-else class="video-list">
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="video in videos" :key="video.id">
              <div class="video-card" @click="playVideo(video)">
                <!-- 视频封面 -->
                <div class="video-cover">
                  <img :src="formatImageUrl(video.coverUrl) || 'http://localhost:8080/backend/image/default_image/defaultImage.png'" alt="视频封面" />
                  <div class="play-overlay">
                    <el-icon :size="24"><VideoPlay /></el-icon>
                  </div>
                </div>
                
                <!-- 视频信息 -->
                <div class="video-info">
                  <div class="video-title">{{ video.title }}</div>
                  <div class="video-meta">
                    <el-avatar :size="20" :src="userInfo.avatar"></el-avatar>
                    <div class="meta-info">
                      <div class="author">{{ video.username }}</div>
                      <div class="stats">
                        <span>{{ formatViewCount(video.viewCount) }}次观看</span>
                        <span class="dot">·</span>
                        <span>{{ formatDate(video.createTime) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, VideoPlay } from '@element-plus/icons-vue'

interface Video {
  id: number;
  title: string;
  description: string;
  videoUrl: string;
  coverUrl: string;
  userId: number;
  username: string;
  viewCount: number;
  categoryId?: number;
  createTime: string;
  duration?: number;
}

interface Category {
  id: number;
  name: string;
  code: string;
  sort: number;
}

interface CarouselItem {
  id: number;
  title: string;
  description: string;
  image: string;
  video: Video;
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
const categories = ref<Category[]>([])
const activeCategory = ref(0)

const userInfo = ref<UserInfo>({
  id: 0,
  username: '',
  avatar: '',
  role: ''
})

const carouselItems = ref<CarouselItem[]>([
  {
    id: 1,
    title: '欢迎来到智慧学堂',
    description: '发现更多精彩视频内容',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=education%20video%20platform%20banner%20with%20students%20learning&image_size=landscape_16_9',
    video: {
      id: 1,
      title: '欢迎来到智慧学堂',
      description: '智慧学堂是一个在线视频学习平台，提供丰富的教育资源',
      videoUrl: '',
      coverUrl: '',
      userId: 1,
      username: 'admin',
      viewCount: 0,
      createTime: new Date().toISOString()
    }
  },
  {
    id: 2,
    title: '热门视频推荐',
    description: '查看本周最受欢迎的视频',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=popular%20video%20recommendation%20banner&image_size=landscape_16_9',
    video: {
      id: 2,
      title: '热门视频推荐',
      description: '本周最受欢迎的视频集合',
      videoUrl: '',
      coverUrl: '',
      userId: 1,
      username: 'admin',
      viewCount: 0,
      createTime: new Date().toISOString()
    }
  },
  {
    id: 3,
    title: '新用户指南',
    description: '快速上手智慧学堂',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=new%20user%20guide%20banner%20for%20video%20platform&image_size=landscape_16_9',
    video: {
      id: 3,
      title: '新用户指南',
      description: '快速了解如何使用智慧学堂',
      videoUrl: '',
      coverUrl: '',
      userId: 1,
      username: 'admin',
      viewCount: 0,
      createTime: new Date().toISOString()
    }
  }
])

const handleSearch = (event: Event) => {
  const customEvent = event as CustomEvent
  loadVideos(customEvent.detail)
}

const loadCategories = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/video/categories')
    const data = await response.json()
    if (data.success) {
      categories.value = data.data
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

onMounted(async () => {
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    userInfo.value = JSON.parse(storedUser)
  }
  
  window.addEventListener('videoSearch', handleSearch)
  await loadCategories()
  loadVideos()
})

const handleCategoryChange = () => {
  loadVideos()
}

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
    if (activeCategory.value && activeCategory.value !== 0) {
      url += `&categoryId=${activeCategory.value}`
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
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

const formatViewCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toString()
}

const formatImageUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('hdfs://')) {
    return `http://localhost:8080/api/image/proxy?url=${encodeURIComponent(url)}`
  }
  if (url.startsWith('/covers/') || url.startsWith('/videos/')) {
    return `http://localhost:8080/api/image/proxy?url=${encodeURIComponent(url)}`
  }
  return url
}
</script>

<style scoped>
.home {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 主要内容区域 */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 分类导航 */
.category-nav {
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 20px;
  padding: 0 20px;
}

.category-tabs {
  border-bottom: none;
}

.category-tabs .el-tabs__content {
  display: none;
}

/* 轮播图 */
.carousel-container {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.carousel-item {
  position: relative;
  width: 100%;
  height: 200px;
  cursor: pointer;
  overflow: hidden;
}

.carousel-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.carousel-item:hover img {
  transform: scale(1.05);
}

.carousel-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
  color: #fff;
}

.carousel-content h3 {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
}

.carousel-content p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

/* 视频列表 */
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

/* 视频封面 */
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

/* 视频信息 */
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
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.meta-info {
  flex: 1;
  min-width: 0;
}

.author {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stats {
  font-size: 11px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 8px;
}

.dot {
  font-size: 10px;
  margin: 0 2px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    padding: 12px;
  }
  
  .video-cover {
    height: 140px;
  }
}
</style>
