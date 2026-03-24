<template>
  <div class="creator-center">
    <div class="center-container">
      <div class="sidebar">
        <div class="sidebar-header">
          <h2>创作者中心</h2>
        </div>
        <div class="sidebar-menu">
          <el-menu :default-active="activeMenu" class="menu" @select="handleMenuSelect">
            <el-menu-item index="home" class="menu-item">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="upload" class="menu-item">
              <el-icon><Upload /></el-icon>
              <span>发布视频</span>
            </el-menu-item>
            <el-menu-item index="videos" class="menu-item">
              <el-icon><VideoPlay /></el-icon>
              <span>我的视频</span>
            </el-menu-item>
            <el-menu-item index="analytics" class="menu-item">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据分析</span>
            </el-menu-item>
          </el-menu>
        </div>
      </div>
      
      <div class="main-content">
        <div v-if="activeMenu === 'home'" class="content-panel">
          <div class="welcome-section">
            <h3>欢迎回来，{{ userInfo.username }}</h3>
            <p>开始创作，分享您的精彩内容</p>
          </div>
          
          <div class="stats-overview">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-content">
                    <el-icon :size="32" color="#409eff"><VideoPlay /></el-icon>
                    <div class="stat-info">
                      <div class="stat-value">{{ stats.videoCount }}</div>
                      <div class="stat-label">视频数</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-content">
                    <el-icon :size="32" color="#67c23a"><View /></el-icon>
                    <div class="stat-info">
                      <div class="stat-value">{{ stats.totalViews }}</div>
                      <div class="stat-label">总播放</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-content">
                    <el-icon :size="32" color="#f56c6c"><Star /></el-icon>
                    <div class="stat-info">
                      <div class="stat-value">{{ stats.totalLikes }}</div>
                      <div class="stat-label">总点赞</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-content">
                    <el-icon :size="32" color="#e6a23c"><User /></el-icon>
                    <div class="stat-info">
                      <div class="stat-value">{{ stats.fansCount }}</div>
                      <div class="stat-label">粉丝数</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
          
          <div class="quick-actions">
            <h4>快捷操作</h4>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card shadow="hover" class="action-card" @click="activeMenu = 'upload'">
                  <el-icon :size="40" color="#409eff"><Upload /></el-icon>
                  <h3>发布视频</h3>
                  <p>上传并发布您的视频作品</p>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card shadow="hover" class="action-card" @click="activeMenu = 'videos'">
                  <el-icon :size="40" color="#67c23a"><VideoPlay /></el-icon>
                  <h3>我的视频</h3>
                  <p>管理您上传的视频</p>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card shadow="hover" class="action-card" @click="activeMenu = 'analytics'">
                  <el-icon :size="40" color="#e6a23c"><DataAnalysis /></el-icon>
                  <h3>数据分析</h3>
                  <p>查看视频播放数据统计</p>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </div>
        
        <div v-else-if="activeMenu === 'upload'" class="content-panel">
          <div class="panel-header">
            <h3>发布视频</h3>
          </div>
          <div class="upload-section">
            <el-form :model="uploadForm" label-width="100px" class="upload-form">
              <el-form-item label="视频标题">
                <el-input v-model="uploadForm.title" placeholder="请输入视频标题" maxlength="50" show-word-limit />
              </el-form-item>
              <el-form-item label="视频描述">
                <el-input v-model="uploadForm.description" type="textarea" :rows="4" placeholder="请输入视频描述" maxlength="200" show-word-limit />
              </el-form-item>
              <el-form-item label="视频分类">
                <el-select v-model="uploadForm.categoryId" placeholder="请选择视频分类" style="width: 100%;">
                  <el-option 
                    v-for="cat in categories" 
                    :key="cat.id" 
                    :label="cat.name" 
                    :value="cat.id"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="视频标签">
                <div class="tags-input-container">
                  <el-tag
                    v-for="tag in uploadForm.tags"
                    :key="tag"
                    closable
                    @close="removeTag(tag)"
                    class="tag-item"
                  >
                    {{ tag }}
                  </el-tag>
                  <el-input
                    v-model="tagInput"
                    placeholder="输入标签后按回车添加"
                    size="small"
                    class="tag-input"
                    @keyup.enter="addTag"
                    @blur="addTag"
                  />
                </div>
                <div class="tags-hint">最多添加10个标签，每个标签不超过10个字</div>
              </el-form-item>
              <el-form-item label="视频文件">
                <el-upload
                  ref="videoUploadRef"
                  class="video-uploader"
                  :auto-upload="false"
                  :show-file-list="true"
                  accept="video/*"
                  :on-change="handleVideoChange"
                  :limit="1"
                >
                  <el-button type="primary">
                    <el-icon><Upload /></el-icon>
                    选择视频
                  </el-button>
                </el-upload>
              </el-form-item>
              <el-form-item label="视频封面">
                <el-upload
                  class="cover-uploader"
                  :auto-upload="false"
                  :show-file-list="false"
                  accept="image/*"
                  :on-change="handleCoverChange"
                >
                  <img v-if="coverPreview" :src="coverPreview" class="cover-preview" />
                  <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
                </el-upload>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitVideo" :loading="uploading">发布视频</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
        
        <div v-else-if="activeMenu === 'videos'" class="content-panel">
          <div class="panel-header">
            <h3>我的视频</h3>
            <el-button type="primary" @click="activeMenu = 'upload'">
              <el-icon><Plus /></el-icon>
              发布新视频
            </el-button>
          </div>
          <div class="videos-section">
            <el-empty v-if="myVideos.length === 0" description="暂无上传视频" />
            <el-row :gutter="20" v-else>
              <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="video in myVideos" :key="video.id">
                <el-card shadow="hover" class="video-card" @click="playVideo(video)">
                  <div class="video-cover">
                    <img :src="formatImageUrl(video.coverUrl) || defaultCover" alt="视频封面" />
                    <div class="play-overlay">
                      <el-icon :size="30"><VideoPlay /></el-icon>
                    </div>
                  </div>
                  <div class="video-info">
                    <h4>{{ video.title }}</h4>
                    <div class="video-stats">
                      <span><el-icon><View /></el-icon> {{ video.viewCount }}</span>
                      <span>{{ formatDate(video.createTime) }}</span>
                    </div>
                    <el-button type="danger" size="small" @click.stop="deleteVideo(video.id)">删除</el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </div>
        
        <div v-else-if="activeMenu === 'analytics'" class="content-panel">
          <div class="panel-header">
            <h3>数据分析</h3>
            <el-radio-group v-model="analyticsDays" @change="loadAnalyticsData" size="small">
              <el-radio-button :value="7">近7天</el-radio-button>
              <el-radio-button :value="30">近30天</el-radio-button>
              <el-radio-button :value="90">近90天</el-radio-button>
            </el-radio-group>
          </div>
          
          <div class="analytics-overview">
            <el-row :gutter="20">
              <el-col :span="4">
                <el-card shadow="hover" class="analytics-stat-card">
                  <div class="analytics-stat-value">{{ formatNumber(analyticsOverview.videoCount) }}</div>
                  <div class="analytics-stat-label">视频数</div>
                </el-card>
              </el-col>
              <el-col :span="4">
                <el-card shadow="hover" class="analytics-stat-card">
                  <div class="analytics-stat-value">{{ formatNumber(analyticsOverview.totalViews) }}</div>
                  <div class="analytics-stat-label">总播放</div>
                </el-card>
              </el-col>
              <el-col :span="4">
                <el-card shadow="hover" class="analytics-stat-card">
                  <div class="analytics-stat-value">{{ formatNumber(analyticsOverview.totalLikes) }}</div>
                  <div class="analytics-stat-label">总点赞</div>
                </el-card>
              </el-col>
              <el-col :span="4">
                <el-card shadow="hover" class="analytics-stat-card">
                  <div class="analytics-stat-value">{{ formatNumber(analyticsOverview.totalFavorites) }}</div>
                  <div class="analytics-stat-label">总收藏</div>
                </el-card>
              </el-col>
              <el-col :span="4">
                <el-card shadow="hover" class="analytics-stat-card">
                  <div class="analytics-stat-value">{{ formatNumber(analyticsOverview.totalComments) }}</div>
                  <div class="analytics-stat-label">总评论</div>
                </el-card>
              </el-col>
              <el-col :span="4">
                <el-card shadow="hover" class="analytics-stat-card">
                  <div class="analytics-stat-value">{{ formatNumber(analyticsOverview.fansCount) }}</div>
                  <div class="analytics-stat-label">粉丝数</div>
                </el-card>
              </el-col>
            </el-row>
          </div>
          
          <el-row :gutter="20" class="analytics-charts">
            <el-col :span="16">
              <el-card shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>趋势分析</span>
                    <el-radio-group v-model="trendType" size="small">
                      <el-radio-button value="views">播放量</el-radio-button>
                      <el-radio-button value="likes">点赞</el-radio-button>
                      <el-radio-button value="favorites">收藏</el-radio-button>
                      <el-radio-button value="comments">评论</el-radio-button>
                    </el-radio-group>
                  </div>
                </template>
                <div ref="trendChartRef" class="chart-container"></div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover">
                <template #header>
                  <span>分类分布</span>
                </template>
                <div ref="categoryChartRef" class="chart-container"></div>
              </el-card>
            </el-col>
          </el-row>
          
          <el-card shadow="hover" class="top-videos-card">
            <template #header>
              <span>热门视频 TOP 10</span>
            </template>
            <el-table :data="topVideos" style="width: 100%">
              <el-table-column type="index" label="排名" width="60" />
              <el-table-column prop="title" label="视频标题" min-width="200">
                <template #default="{ row }">
                  <div class="video-title-cell clickable" @click="goToVideo(row.id)">{{ row.title }}</div>
                </template>
              </el-table-column>
              <el-table-column prop="view_count" label="播放量" width="100" sortable>
                <template #default="{ row }">
                  {{ formatNumber(row.view_count) }}
                </template>
              </el-table-column>
              <el-table-column prop="likes" label="点赞" width="80" sortable>
                <template #default="{ row }">
                  {{ formatNumber(row.likes) }}
                </template>
              </el-table-column>
              <el-table-column prop="favorites" label="收藏" width="80" sortable>
                <template #default="{ row }">
                  {{ formatNumber(row.favorites) }}
                </template>
              </el-table-column>
              <el-table-column prop="comments" label="评论" width="80" sortable>
                <template #default="{ row }">
                  {{ formatNumber(row.comments) }}
                </template>
              </el-table-column>
              <el-table-column prop="create_time" label="发布时间" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.create_time) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  House, Upload, VideoPlay, DataAnalysis, View, Star, User, Plus 
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

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

interface Category {
  id: number;
  name: string;
  code: string;
}

const router = useRouter()

const activeMenu = ref('home')

const userInfo = ref({
  username: ''
})

const stats = reactive({
  videoCount: 0,
  totalViews: 0,
  totalLikes: 0,
  fansCount: 0
})

const myVideos = ref<Video[]>([])
const categories = ref<Category[]>([])
const defaultCover = 'http://localhost:8080/backend/image/default_image/defaultImage.png'

const uploadForm = reactive({
  title: '',
  description: '',
  categoryId: null as number | null,
  tags: [] as string[]
})

const tagInput = ref('')
const videoFile = ref<File | null>(null)
const coverFile = ref<File | null>(null)
const coverPreview = ref('')
const uploading = ref(false)

const analyticsDays = ref(7)
const trendType = ref('views')
const analyticsOverview = reactive({
  videoCount: 0,
  totalViews: 0,
  totalLikes: 0,
  totalFavorites: 0,
  totalComments: 0,
  fansCount: 0
})
const trendData = ref<any[]>([])
const categoryDistribution = ref<any[]>([])
const topVideos = ref<any[]>([])
const trendChartRef = ref<HTMLElement | null>(null)
const categoryChartRef = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null
let categoryChart: echarts.ECharts | null = null

const handleMenuSelect = (index: string) => {
  activeMenu.value = index
  if (index === 'videos') {
    loadMyVideos()
  } else if (index === 'analytics') {
    loadAnalyticsData()
  }
}

const loadCategories = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/video/categories')
    const data = await response.json()
    if (data.success) {
      categories.value = data.data.filter((cat: Category) => cat.code !== 'recommend')
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const addTag = () => {
  const tag = tagInput.value.trim()
  if (!tag) {
    tagInput.value = ''
    return
  }
  if (tag.length > 10) {
    ElMessage.warning('标签长度不能超过10个字')
    return
  }
  if (uploadForm.tags.length >= 10) {
    ElMessage.warning('最多只能添加10个标签')
    return
  }
  if (uploadForm.tags.includes(tag)) {
    ElMessage.warning('标签已存在')
    return
  }
  uploadForm.tags.push(tag)
  tagInput.value = ''
}

const removeTag = (tag: string) => {
  uploadForm.tags = uploadForm.tags.filter(t => t !== tag)
}

const handleVideoChange = (file: any) => {
  videoFile.value = file.raw
}

const handleCoverChange = (file: any) => {
  coverFile.value = file.raw
  coverPreview.value = URL.createObjectURL(file.raw)
}

const submitVideo = async () => {
  if (!uploadForm.title) {
    ElMessage.error('请输入视频标题')
    return
  }
  if (!videoFile.value) {
    ElMessage.error('请选择视频文件')
    return
  }
  
  uploading.value = true
  
  try {
    const token = localStorage.getItem('token')
    const formData = new FormData()
    formData.append('title', uploadForm.title)
    formData.append('description', uploadForm.description)
    if (uploadForm.categoryId) {
      formData.append('categoryId', uploadForm.categoryId.toString())
    }
    formData.append('video', videoFile.value)
    if (coverFile.value) {
      formData.append('cover', coverFile.value)
    }
    
    const response = await fetch('http://localhost:8080/api/video/upload', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    })
    
    const data = await response.json()
    if (data.success) {
      if (uploadForm.tags.length > 0 && data.data && data.data.id) {
        await fetch(`http://localhost:8080/api/tag/video/${data.data.id}/tags`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(uploadForm.tags)
        })
      }
      ElMessage.success('视频发布成功')
      uploadForm.title = ''
      uploadForm.description = ''
      uploadForm.categoryId = null
      uploadForm.tags = []
      videoFile.value = null
      coverFile.value = null
      coverPreview.value = ''
      activeMenu.value = 'videos'
    } else {
      ElMessage.error(data.message || '发布失败')
    }
  } catch (error) {
    ElMessage.error('发布失败，请重试')
  } finally {
    uploading.value = false
  }
}

const loadMyVideos = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('http://localhost:8080/api/video/my', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const data = await response.json()
    
    if (data.success) {
      myVideos.value = data.data
      stats.videoCount = data.data.length
      stats.totalViews = data.data.reduce((sum: number, v: Video) => sum + v.viewCount, 0)
    } else {
      ElMessage.error(data.message || '获取视频列表失败')
    }
  } catch (error) {
    ElMessage.error('获取视频列表失败')
  }
}

const playVideo = (video: Video) => {
  router.push(`/video?id=${video.id}`)
}

const deleteVideo = async (videoId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个视频吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const token = localStorage.getItem('token')
    const response = await fetch(`http://localhost:8080/api/video/${videoId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const data = await response.json()
    
    if (data.success) {
      ElMessage.success('删除成功')
      loadMyVideos()
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
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

const goToVideo = (videoId: number) => {
  router.push(`/video?id=${videoId}`)
}

const formatNumber = (num: number) => {
  if (num === undefined || num === null) return '0'
  if (num >= 100000000) {
    return (num / 100000000).toFixed(1) + '亿'
  }
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
}

const loadAnalyticsData = async () => {
  const token = localStorage.getItem('token')
  if (!token) return
  
  try {
    const [overviewRes, trendRes, topRes, categoryRes] = await Promise.all([
      fetch('http://localhost:8080/api/creator/analytics/overview', {
        headers: { 'Authorization': `Bearer ${token}` }
      }),
      fetch(`http://localhost:8080/api/creator/analytics/trend?days=${analyticsDays.value}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      }),
      fetch('http://localhost:8080/api/creator/analytics/top-videos?limit=10', {
        headers: { 'Authorization': `Bearer ${token}` }
      }),
      fetch('http://localhost:8080/api/creator/analytics/category-distribution', {
        headers: { 'Authorization': `Bearer ${token}` }
      })
    ])
    
    const overviewData = await overviewRes.json()
    const trendDataRes = await trendRes.json()
    const topData = await topRes.json()
    const categoryData = await categoryRes.json()
    
    if (overviewData.success) {
      Object.assign(analyticsOverview, overviewData.data)
    }
    
    if (trendDataRes.success) {
      trendData.value = trendDataRes.data
      await nextTick()
      renderTrendChart()
    }
    
    if (topData.success) {
      topVideos.value = topData.data
    }
    
    if (categoryData.success) {
      categoryDistribution.value = categoryData.data
      await nextTick()
      renderCategoryChart()
    }
  } catch (error) {
    console.error('加载分析数据失败', error)
  }
}

const renderTrendChart = () => {
  if (!trendChartRef.value) return
  
  if (trendChart) {
    trendChart.dispose()
  }
  
  trendChart = echarts.init(trendChartRef.value)
  
  const dates = trendData.value.map(item => item.date)
  const data = trendData.value.map(item => item[trendType.value] || 0)
  
  const typeNames: Record<string, string> = {
    views: '播放量',
    likes: '点赞数',
    favorites: '收藏数',
    comments: '评论数'
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45,
        formatter: (value: string) => {
          const date = new Date(value)
          return `${date.getMonth() + 1}/${date.getDate()}`
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value: number) => {
          if (value >= 10000) {
            return (value / 10000).toFixed(1) + '万'
          }
          return value
        }
      }
    },
    series: [{
      name: typeNames[trendType.value],
      type: 'line',
      smooth: true,
      data: data,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
        ])
      },
      lineStyle: {
        color: '#409eff',
        width: 2
      },
      itemStyle: {
        color: '#409eff'
      }
    }]
  }
  
  trendChart.setOption(option)
}

const renderCategoryChart = () => {
  if (!categoryChartRef.value) return
  
  if (categoryChart) {
    categoryChart.dispose()
  }
  
  categoryChart = echarts.init(categoryChartRef.value)
  
  const data = categoryDistribution.value.map(item => ({
    name: item.category || '未分类',
    value: item.count
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '0%',
      top: 'center'
    },
    series: [{
      type: 'pie',
      radius: ['30%', '50%'],
      center: ['30%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: data
    }]
  }
  
  categoryChart.setOption(option)
}

watch(trendType, () => {
  renderTrendChart()
})

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    userInfo.value.username = user.username
  }
  loadCategories()
  loadMyVideos()
})
</script>

<style scoped>
.creator-center {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.center-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 200px;
  background-color: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  padding: 20px 0;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 0 20px 20px;
  border-bottom: 1px solid #e4e7ed;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
  font-weight: 600;
}

.sidebar-menu {
  margin-top: 20px;
}

.menu {
  border-right: none;
}

.menu-item {
  height: 50px;
  line-height: 50px;
  margin: 0 10px;
  border-radius: 4px;
}

.menu-item:hover {
  background-color: #ecf5ff !important;
}

.menu-item.is-active {
  background-color: #ecf5ff !important;
  color: #409eff !important;
}

.menu-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background-color: #409eff;
}

.main-content {
  flex: 1;
  padding: 30px;
  background-color: #f5f7fa;
  overflow-y: auto;
}

.content-panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.panel-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
  font-weight: 600;
}

.welcome-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.welcome-section h3 {
  margin: 0 0 10px;
  font-size: 24px;
  color: #303133;
}

.welcome-section p {
  margin: 0;
  color: #909399;
}

.stats-overview {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
  overflow: hidden;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
  overflow: hidden;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
  overflow: hidden;
}

.stat-info {
  flex: 1;
  overflow: hidden;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.quick-actions h4 {
  margin: 0 0 15px;
  font-size: 16px;
  color: #303133;
}

.action-card {
  cursor: pointer;
  text-align: center;
  padding: 30px 20px;
  transition: all 0.3s;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-card h3 {
  margin: 15px 0 10px;
  font-size: 16px;
  color: #303133;
}

.action-card p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.upload-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.upload-form {
  max-width: 600px;
}

.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 100px;
}

.cover-uploader:hover {
  border-color: #409eff;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

.cover-preview {
  width: 178px;
  height: 100px;
  object-fit: cover;
}

.tags-input-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 8px;
  min-height: 40px;
}

.tags-input-container:focus-within {
  border-color: #409eff;
}

.tag-item {
  margin: 0;
}

.tag-input {
  width: 150px;
  border: none;
  box-shadow: none;
}

.tag-input :deep(.el-input__wrapper) {
  box-shadow: none;
  padding: 0;
}

.tags-hint {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.videos-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.video-card {
  cursor: pointer;
  margin-bottom: 20px;
}

.video-cover {
  position: relative;
  width: 100%;
  height: 120px;
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

.video-info h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.video-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.analytics-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 50px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.analytics-overview {
  margin-bottom: 20px;
}

.analytics-stat-card {
  text-align: center;
  padding: 10px 0;
}

.analytics-stat-card :deep(.el-card__body) {
  padding: 20px;
}

.analytics-stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.analytics-stat-label {
  font-size: 14px;
  color: #909399;
}

.analytics-charts {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.top-videos-card {
  margin-top: 20px;
}

.video-title-cell {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 300px;
}

.video-title-cell.clickable {
  cursor: pointer;
  color: #409eff;
}

.video-title-cell.clickable:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .center-container {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    padding: 10px 0;
  }
  
  .sidebar-header {
    padding: 0 15px 15px;
  }
  
  .menu-item {
    height: 40px;
    line-height: 40px;
  }
  
  .main-content {
    padding: 20px;
  }
}
</style>
