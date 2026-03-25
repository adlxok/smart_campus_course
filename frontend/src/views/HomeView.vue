<template>
  <div class="home">
    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="content-container">
        <!-- 轮播图 -->
        <div v-if="!loading" class="carousel-container">
          <el-carousel :interval="5000" type="card" height="200px">
            <el-carousel-item v-for="item in carouselItems" :key="item.id">
              <div class="carousel-item" @click="handleBannerClick(item)">
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
            <el-tab-pane label="推荐" :name="-1"></el-tab-pane>
            <el-tab-pane 
              v-for="cat in categories" 
              :key="cat.id" 
              :label="cat.name" 
              :name="cat.id"
            ></el-tab-pane>
          </el-tabs>
        </div>

        <!-- 视频列表 -->
        <div v-if="loading && videos.length === 0" class="loading">
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
          
          <!-- 加载更多哨兵元素 -->
          <div class="load-more-sentinel" ref="sentinelRef">
            <el-icon v-if="loadingMore" class="is-loading" :size="24"><Loading /></el-icon>
            <p v-if="loadingMore">加载更多...</p>
          </div>
          
          <!-- 没有更多 -->
          <div class="no-more" v-if="noMore && videos.length > 0">
            <el-divider>已经到底了</el-divider>
          </div>
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
const loadingMore = ref(false)
const videos = ref<Video[]>([])
const categories = ref<Category[]>([])
const activeCategory = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const noMore = ref(false)

const userInfo = ref<UserInfo>({
  id: 0,
  username: '',
  avatar: '',
  role: ''
})

const carouselItems = ref<CarouselItem[]>([])

const loadBanners = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/banner/list')
    const data = await response.json()
    if (data.success && data.data) {
      carouselItems.value = data.data.map(banner => {
        const imageUrl = banner.imageUrl.startsWith('http') 
          ? banner.imageUrl 
          : `http://localhost:8080${banner.imageUrl}`
        return {
          id: banner.id,
          title: banner.title,
          description: banner.description || '',
          image: imageUrl,
          linkUrl: banner.linkUrl || '',
          video: {
            id: banner.id,
            title: banner.title,
            description: banner.description || '',
            videoUrl: '',
            coverUrl: imageUrl,
            userId: 1,
            username: 'admin',
            viewCount: 0,
            createTime: banner.createTime
          }
        }
      })
    }
  } catch (error) {
    console.error('加载轮播图失败:', error)
  }
}

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
  await loadBanners()
  loadVideos()
  
  setTimeout(() => {
    setupIntersectionObserver()
  }, 500)
})

const handleCategoryChange = () => {
  currentPage.value = 1
  videos.value = []
  noMore.value = false
  if (activeCategory.value === -1) {
    loadRecommendations()
  } else {
    loadVideos()
    setTimeout(() => {
      setupIntersectionObserver()
    }, 100)
  }
}

onBeforeUnmount(() => {
  window.removeEventListener('videoSearch', handleSearch)
  if (observer) {
    observer.disconnect()
  }
})

let observer: IntersectionObserver | null = null
const sentinelRef = ref<HTMLElement | null>(null)

const setupIntersectionObserver = () => {
  if (observer) {
    observer.disconnect()
  }
  
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting && !loadingMore.value && !noMore.value && activeCategory.value !== -1) {
          loadMoreVideos()
        }
      })
    },
    {
      root: null,
      rootMargin: '200px',
      threshold: 0
    }
  )
  
  if (sentinelRef.value) {
    observer.observe(sentinelRef.value)
  }
}

const loadMoreVideos = async () => {
  if (loadingMore.value || noMore.value) return
  
  loadingMore.value = true
  try {
    const nextPage = currentPage.value + 1
    let url = `http://localhost:8080/api/video/list?pageNum=${nextPage}&pageSize=${pageSize.value}`
    if (activeCategory.value && activeCategory.value !== 0) {
      url += `&categoryId=${activeCategory.value}`
    }
    
    const response = await fetch(url)
    const data = await response.json()
    
    if (data.success && data.data && data.data.length > 0) {
      videos.value = [...videos.value, ...data.data]
      currentPage.value = nextPage
      if (data.data.length < pageSize.value) {
        noMore.value = true
        ElMessage.info('没有更多视频了')
      }
    } else {
      noMore.value = true
      ElMessage.info('没有更多视频了')
    }
  } catch (error) {
    ElMessage.error('加载更多视频失败')
  } finally {
    loadingMore.value = false
  }
}

const loadRecommendations = async () => {
  loading.value = true
  try {
    if (!userInfo.value.id) {
      videos.value = []
      ElMessage.warning('请先登录以获取个性化推荐')
      return
    }
    
    const response = await fetch(`http://localhost:8080/api/video/recommend?userId=${userInfo.value.id}&limit=10`)
    const data = await response.json()
    console.log(data)
    
    if (data.success && data.data) {
      videos.value = data.data.map((v: any) => ({
        id: v.id,
        title: v.title,
        description: '',
        videoUrl: '',
        coverUrl: v.coverUrl,
        userId: 0,
        username: v.username,
        viewCount: v.viewCount,
        createTime: new Date().toISOString()
      }))
      if (data.message) {
        ElMessage.success(data.message)
      }
      ElMessage.success(`基于您的收藏兴趣推荐了${data.data.length}个视频`);
    } else {
      videos.value = []
      if (data.message) {
        ElMessage.info(data.message)
      }
    }
  } catch (error) {
    ElMessage.error('获取推荐失败')
    videos.value = []
  } finally {
    loading.value = false
  }
}

const loadVideos = async (keyword: string = '') => {
  loading.value = true
  try {
    let url = `http://localhost:8080/api/video/list?pageNum=${currentPage.value}&pageSize=${pageSize.value}`
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
      total.value = data.total || 0
      if (data.data.length < pageSize.value) {
        noMore.value = true
      }
    } else {
      ElMessage.error(data.message || '加载视频列表失败')
    }
  } catch (error) {
    ElMessage.error('加载视频列表失败')
  } finally {
    loading.value = false
  }
}

const playVideo = async (video: Video & { linkUrl?: string }) => {
  if (video.linkUrl) {
    if (video.linkUrl.startsWith('http')) {
      window.open(video.linkUrl, '_blank')
    } else {
      router.push(video.linkUrl)
    }
    return
  }
  if (video.id && video.id > 0) {
    router.push(`/video?id=${video.id}`)
  }
}

const handleBannerClick = (item: CarouselItem) => {
  if (item.linkUrl) {
    if (item.linkUrl.startsWith('http')) {
      window.open(item.linkUrl, '_blank')
    } else {
      router.push(item.linkUrl)
    }
  }
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

.load-more {
  text-align: center;
  padding: 20px 0;
  color: #909399;
}

.load-more .el-icon {
  margin-right: 8px;
}

.no-more {
  text-align: center;
  padding: 20px 0;
}

.no-more .el-divider {
  margin: 0;
}

.no-more .el-divider__text {
  color: #909399;
  font-size: 14px;
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
