<template>
  <div class="analysis-screen">
    <canvas ref="particleCanvas" class="particle-canvas"></canvas>
    
    <div class="bg-decoration">
      <div class="grid-bg"></div>
      <div class="scan-line"></div>
      <div class="decoration-corner corner-tl"></div>
      <div class="decoration-corner corner-tr"></div>
      <div class="decoration-corner corner-bl"></div>
      <div class="decoration-corner corner-br"></div>
    </div>
    
    <div class="screen-header">
      <div class="header-left">
        <div class="logo-box">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="#00f7ff" stroke-width="1.5"/>
              <path d="M2 17L12 22L22 17" stroke="#00f7ff" stroke-width="1.5"/>
              <path d="M2 12L12 17L22 12" stroke="#00f7ff" stroke-width="1.5"/>
            </svg>
          </div>
        </div>
        <div class="title-wrapper">
          <h1>B站视频数据分析大屏</h1>
          <div class="subtitle">BILIBILI VIDEO DATA ANALYSIS SCREEN</div>
        </div>
      </div>
      
      <div class="header-center">
        <div class="digital-clock">
          <div class="clock-box">
            <span class="clock-separator">:</span>
            <span class="clock-num" :key="'h-'+hours">{{ hours }}</span>
            <span class="clock-separator">:</span>
            <span class="clock-num" :key="'m-'+minutes">{{ minutes }}</span>
            <span class="clock-separator">:</span>
            <span class="clock-num" :key="'s-'+seconds">{{ seconds }}</span>
          </div>
          <div class="date-info">
            <span class="date">{{ currentDate }}</span>
            <span class="weekday">{{ currentWeekday }}</span>
          </div>
        </div>
      </div>
      
      <div class="header-right">
        <div class="status-indicator">
          <span class="status-dot"></span>
          <span class="status-text">系统正常</span>
        </div>
        <el-button type="primary" size="small" @click="goBack" class="exit-btn">
          <span class="btn-icon">✕</span>
          <span>退出大屏</span>
        </el-button>
      </div>
    </div>
    
    <div class="screen-content">
      <div class="left-panel">
        <div class="chart-card">
          <div class="card-header-decoration"></div>
          <div class="card-title">
            <div class="title-line"></div>
            <span class="title-text">
              <span class="title-icon">📁</span>
              分类分布
            </span>
          </div>
          <div ref="categoryChart" class="chart-container"></div>
        </div>
        
        <div class="chart-card">
          <div class="card-header-decoration"></div>
          <div class="card-title">
            <div class="title-line"></div>
            <span class="title-text">
              <span class="title-icon">📈</span>
              每日入库趋势
            </span>
          </div>
          <div ref="trendChart" class="chart-container"></div>
        </div>
        
        <div class="chart-card">
          <div class="card-header-decoration"></div>
          <div class="card-title">
            <div class="title-line"></div>
            <span class="title-text">
              <span class="title-icon">🎯</span>
              视频质量评分
            </span>
          </div>
          <div ref="radarChart" class="chart-container"></div>
        </div>
      </div>
      
      <div class="center-panel">
        <div class="stats-grid">
          <div class="stat-card" v-for="(stat, index) in statsCards" :key="index">
            <div class="stat-border-top"></div>
            <div class="stat-border-right"></div>
            <div class="stat-border-bottom"></div>
            <div class="stat-border-left"></div>
            <div class="stat-icon-wrapper">
              <div class="stat-icon-bg"></div>
              <div class="stat-icon">{{ stat.icon }}</div>
            </div>
            <div class="stat-content">
              <div class="stat-value">
                <span class="value-number">{{ formatNumber(stat.value) }}</span>
                <span class="value-unit">{{ stat.unit }}</span>
              </div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
            <div class="stat-glow"></div>
          </div>
        </div>
        
        <div class="center-charts">
          <div class="center-top-row">
            <div class="chart-card">
              <div class="card-header-decoration"></div>
              <div class="card-title">
                <div class="title-line"></div>
                <span class="title-text">
                  <span class="title-icon">📊</span>
                  互动数据对比
                </span>
              </div>
              <div ref="interactionChart" class="chart-container"></div>
            </div>
            
            <div class="chart-card">
              <div class="card-header-decoration"></div>
              <div class="card-title">
                <div class="title-line"></div>
                <span class="title-text">
                  <span class="title-icon">🏅</span>
                  分类互动数据对比
                </span>
              </div>
              <div ref="categoryLikesChart" class="chart-container"></div>
            </div>
          </div>
          
          <div class="chart-card top-videos-card">
            <div class="card-header-decoration"></div>
            <div class="card-title">
              <div class="title-line"></div>
              <span class="title-text">
                <span class="title-icon">🏆</span>
                热门视频 TOP 10
              </span>
              <el-radio-group v-model="topSortBy" size="small" @change="fetchTopVideos" class="sort-radio">
                <el-radio-button label="view">播放量</el-radio-button>
                <el-radio-button label="like">点赞</el-radio-button>
                <el-radio-button label="coin">投币</el-radio-button>
              </el-radio-group>
            </div>
            <div class="top-videos-list">
              <div v-for="(video, index) in topVideos" :key="video.bvid" class="video-item">
                <span class="rank" :class="'rank-' + (index + 1)">
                  <span v-if="index < 3" class="medal">{{ ['🥇', '🥈', '🥉'][index] }}</span>
                  <span v-else>{{ index + 1 }}</span>
                </span>
                <span class="title">{{ video.title }}</span>
                <div class="video-stats">
                  <span class="stat-item">👁️ {{ formatNumber(video.viewCount) }}</span>
                  <span class="stat-item">👍 {{ formatNumber(video.likeCount) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="right-panel">
        <div class="chart-card">
          <div class="card-header-decoration"></div>
          <div class="card-title">
            <div class="title-line"></div>
            <span class="title-text">
              <span class="title-icon">💎</span>
              数据完整度
            </span>
          </div>
          <div class="gauge-container">
            <div ref="gaugeChart" class="gauge-chart"></div>
            <div class="gauge-value">
              <div class="value-number">{{ dataCompleteness.completeness }}%</div>
              <div class="value-label">数据完整度</div>
            </div>
          </div>
        </div>
        
        <div class="chart-card tag-cloud-card">
          <div class="card-header-decoration"></div>
          <div class="card-title">
            <div class="title-line"></div>
            <span class="title-text">
              <span class="title-icon">☁️</span>
              热门标签云
            </span>
            <el-select v-model="selectedCategory" size="small" @change="fetchTagCloud" class="category-select" placeholder="选择分类" :teleported="false">
              <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
            </el-select>
          </div>
          <div ref="wordCloudChart" class="chart-container word-cloud"></div>
        </div>
        
        <div class="chart-card interaction-card">
          <div class="card-header-decoration"></div>
          <div class="card-title">
            <div class="title-line"></div>
            <span class="title-text">
              <span class="title-icon">⚡</span>
              互动率分析
            </span>
            <el-select v-model="interactionCategory" size="small" @change="fetchInteractionOverview" class="category-select" placeholder="选择分类" :teleported="false">
              <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
            </el-select>
          </div>
          <div ref="funnelChart" class="chart-container"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import 'echarts-wordcloud'
import TWEEN from '@tweenjs/tween.js'
import api from '../utils/api'

const router = useRouter()

const hours = ref('00')
const minutes = ref('00')
const seconds = ref('00')
const currentDate = ref('')
const currentWeekday = ref('')

const overview = ref({
  totalVideos: 0,
  totalViewCount: 0,
  totalLikeCount: 0,
  totalCoinCount: 0,
  totalFavoriteCount: 0,
  totalDanmakuCount: 0,
  totalShareCount: 0,
  totalReplyCount: 0
})

const topVideos = ref([])
const topSortBy = ref('view')
const tagCloud = ref([])
const categories = ref([])
const selectedCategory = ref('全部')
const interactionCategory = ref('全部')
const dataCompleteness = ref({
  totalVideos: 0,
  duplicateBvids: 0,
  nullBvids: 0,
  nullTitles: 0,
  nullCategories: 0,
  completeness: 100
})

const animatedValues = ref([0, 0, 0, 0, 0, 0, 0, 0])
const statRefs = ref([])
const setStatRef = (el, index) => {
  if (el) {
    statRefs.value[index] = el
  }
}

const statsCards = computed(() => [
  { icon: '📹', label: '视频总数', value: overview.value.totalVideos, unit: '' },
  { icon: '👁️', label: '总播放量', value: overview.value.totalViewCount, unit: '' },
  { icon: '👍', label: '总点赞数', value: overview.value.totalLikeCount, unit: '' },
  { icon: '🪙', label: '总投币数', value: overview.value.totalCoinCount, unit: '' },
  { icon: '⭐', label: '总收藏数', value: overview.value.totalFavoriteCount, unit: '' },
  { icon: '💬', label: '总弹幕数', value: overview.value.totalDanmakuCount, unit: '' },
  { icon: '🔄', label: '总分享数', value: overview.value.totalShareCount, unit: '' },
  { icon: '📝', label: '总评论数', value: overview.value.totalReplyCount, unit: '' }
])

const categoryChart = ref(null)
const trendChart = ref(null)
const interactionChart = ref(null)
const radarChart = ref(null)
const gaugeChart = ref(null)
const wordCloudChart = ref(null)
const funnelChart = ref(null)
const categoryLikesChart = ref(null)
const particleCanvas = ref(null)

let chartInstances = {}
let timeInterval = null
let particleAnimationId = null
let particles = []
let tweenAnimations = []

const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 100000000) return (num / 100000000).toFixed(2) + '亿'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  return num.toLocaleString()
}

const formatAnimatedNumber = (num) => {
  if (!num) return '0'
  if (num >= 100000000) return (num / 100000000).toFixed(2) + '亿'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  return Math.floor(num).toLocaleString()
}

const updateTime = () => {
  const now = new Date()
  hours.value = String(now.getHours()).padStart(2, '0')
  minutes.value = String(now.getMinutes()).padStart(2, '0')
  seconds.value = String(now.getSeconds()).padStart(2, '0')
  currentDate.value = now.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  currentWeekday.value = weekdays[now.getDay()]
}

const goBack = () => {
  router.push('/dashboard')
}

const initParticles = () => {
  const canvas = particleCanvas.value
  if (!canvas) return
  
  const ctx = canvas.getContext('2d')
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  
  particles = []
  const particleCount = Math.floor((canvas.width * canvas.height) / 15000)
  
  for (let i = 0; i < particleCount; i++) {
    particles.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      vx: (Math.random() - 0.5) * 0.5,
      vy: (Math.random() - 0.5) * 0.5,
      radius: Math.random() * 2 + 0.5,
      alpha: Math.random() * 0.5 + 0.2,
      color: Math.random() > 0.5 ? '#00f7ff' : '#8b5cf6'
    })
  }
  
  const animateParticles = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    
    particles.forEach((p, i) => {
      p.x += p.vx
      p.y += p.vy
      
      if (p.x < 0 || p.x > canvas.width) p.vx *= -1
      if (p.y < 0 || p.y > canvas.height) p.vy *= -1
      
      particles.slice(i + 1).forEach(p2 => {
        const dx = p.x - p2.x
        const dy = p.y - p2.y
        const dist = Math.sqrt(dx * dx + dy * dy)
        
        if (dist < 150) {
          ctx.beginPath()
          ctx.strokeStyle = `rgba(0, 247, 255, ${0.15 * (1 - dist / 150)})`
          ctx.lineWidth = 0.5
          ctx.moveTo(p.x, p.y)
          ctx.lineTo(p2.x, p2.y)
          ctx.stroke()
        }
      })
      
      ctx.beginPath()
      ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2)
      ctx.fillStyle = p.color
      ctx.globalAlpha = p.alpha
      ctx.fill()
      ctx.globalAlpha = 1
    })
    
    TWEEN.update()
    particleAnimationId = requestAnimationFrame(animateParticles)
  }
  
  animateParticles()
}

const animateNumbers = () => {
  tweenAnimations.forEach(tween => tween.stop())
  tweenAnimations = []
  
  statsCards.value.forEach((stat, index) => {
    const targetValue = stat.value || 0
    const startValue = animatedValues.value[index] || 0
    
    const tween = new TWEEN.Tween({ value: startValue })
      .to({ value: targetValue }, 2000)
      .easing(TWEEN.Easing.Exponential.Out)
      .onUpdate(obj => {
        animatedValues.value[index] = formatAnimatedNumber(obj.value)
      })
      .start()
    
    tweenAnimations.push(tween)
  })
}

const fetchOverview = async () => {
  try {
    const res = await api.get('/admin/analysis/overview')
    if (res.data.success) {
      const data = res.data
      overview.value = {
        totalVideos: data.totalVideos || 0,
        totalViewCount: data.totalViewCount || 0,
        totalLikeCount: data.totalLikeCount || 0,
        totalCoinCount: data.totalCoinCount || 0,
        totalFavoriteCount: data.totalFavoriteCount || 0,
        totalDanmakuCount: data.totalDanmakuCount || 0,
        totalShareCount: data.totalShareCount || 0,
        totalReplyCount: data.totalReplyCount || 0
      }
      await nextTick()
      animateNumbers()
      renderFunnelChart()
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchInteractionOverview = async () => {
  try {
    const params = interactionCategory.value !== '全部' ? { category: interactionCategory.value } : {}
    const res = await api.get('/admin/analysis/overview', { params })
    if (res.data.success) {
      const data = res.data
      renderFunnelChartWithData({
        totalViewCount: data.totalViewCount || 0,
        totalLikeCount: data.totalLikeCount || 0,
        totalCoinCount: data.totalCoinCount || 0,
        totalFavoriteCount: data.totalFavoriteCount || 0,
        totalDanmakuCount: data.totalDanmakuCount || 0,
        totalShareCount: data.totalShareCount || 0,
        totalReplyCount: data.totalReplyCount || 0
      })
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchTopVideos = async () => {
  try {
    const res = await api.get('/admin/analysis/top-videos', {
      params: { sortBy: topSortBy.value }
    })
    if (res.data.success) {
      topVideos.value = res.data.data
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchCategoryDistribution = async () => {
  try {
    const res = await api.get('/admin/analysis/category-distribution')
    console.log('分类分布API返回数据:', res.data)
    if (res.data.success) {
      console.log('分类数据数组:', res.data.data)
      const categoryList = ['全部', ...res.data.data.map(item => item.category)]
      console.log('处理后的分类列表:', categoryList)
      categories.value = categoryList
      renderCategoryChart(res.data.data)
    }
  } catch (e) {
    console.error('获取分类分布失败:', e)
  }
}

const fetchDailyTrend = async () => {
  try {
    const res = await api.get('/admin/analysis/daily-trend')
    if (res.data.success) {
      renderTrendChart(res.data.data)
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchInteractionStats = async () => {
  try {
    const res = await api.get('/admin/analysis/interaction-stats')
    if (res.data.success && res.data.data[0]) {
      renderInteractionChart(res.data.data[0])
      renderRadarChart(res.data.data[0])
      renderGaugeChart(res.data.data[0])
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchTagCloud = async (category = null) => {
  try {
    const params = category && category !== '全部' ? { category } : {}
    const res = await api.get('/admin/analysis/tag-cloud', { params })
    if (res.data.success) {
      tagCloud.value = res.data.data.slice(0, 80)
      renderWordCloudChart(res.data.data.slice(0, 80))
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchCategoryLikes = async () => {
  try {
    const res = await api.get('/admin/analysis/category-likes')
    if (res.data.success) {
      renderCategoryLikesChart(res.data.data)
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchDataCompleteness = async () => {
  try {
    const res = await api.get('/admin/analysis/data-completeness')
    if (res.data.success) {
      dataCompleteness.value = res.data
      renderGaugeChart()
    }
  } catch (e) {
    console.error(e)
  }
}

const renderCategoryLikesChart = (data) => {
  if (!chartInstances.categoryLikes) {
    chartInstances.categoryLikes = echarts.init(categoryLikesChart.value)
  }
  
  const colors = [
    '#00f7ff', '#8b5cf6', '#10b981', '#f59e0b', '#ef4444', 
    '#06b6d4', '#ec4899', '#6366f1', '#3b82f6', '#14b8a6'
  ]
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(10, 22, 40, 0.95)',
      borderColor: '#00f7ff',
      textStyle: { color: '#fff' },
      formatter: '{b}: {c}'
    },
    grid: { left: '3%', right: '10%', bottom: '3%', top: '3%', containLabel: true },
    xAxis: {
      type: 'value',
      axisLabel: { color: 'rgba(255, 255, 255, 0.5)', fontSize: 10 },
      axisLine: { lineStyle: { color: 'rgba(0, 247, 255, 0.3)' } },
      splitLine: { lineStyle: { color: 'rgba(0, 247, 255, 0.1)' } }
    },
    yAxis: {
      type: 'category',
      data: data.map(item => item.category).reverse(),
      axisLabel: { color: 'rgba(255, 255, 255, 0.7)', fontSize: 10 },
      axisLine: { lineStyle: { color: 'rgba(0, 247, 255, 0.3)' } }
    },
    series: [{
      type: 'bar',
      data: data.map((item, index) => ({
        value: item.totalLikes,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: colors[index % colors.length] },
            { offset: 1, color: colors[(index + 1) % colors.length] }
          ]),
          shadowColor: colors[index % colors.length],
          shadowBlur: 10
        }
      })).reverse(),
      barWidth: '60%',
      label: { 
        show: true, 
        position: 'right', 
        color: '#fff', 
        fontSize: 9,
        formatter: (params) => {
          const value = params.value
          if (value >= 100000000) return (value / 100000000).toFixed(1) + '亿'
          if (value >= 10000) return (value / 10000).toFixed(1) + '万'
          return value
        }
      },
      itemStyle: { borderRadius: [0, 4, 4, 0] },
      animationEasing: 'elasticOut',
      animationDelay: function (idx) {
        return idx * 50
      }
    }]
  }
  
  chartInstances.categoryLikes.setOption(option)
}

const renderCategoryChart = (data) => {
  if (!chartInstances.category) {
    chartInstances.category = echarts.init(categoryChart.value)
  }
  
  const colors = ['#00f7ff', '#8b5cf6', '#10b981', '#f59e0b', '#ef4444', '#06b6d4', '#ec4899', '#6366f1']
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      backgroundColor: 'rgba(10, 22, 40, 0.95)',
      borderColor: '#00f7ff',
      textStyle: { color: '#fff' }
    },
    legend: {
      type: 'scroll',
      orient: 'vertical',
      right: 5,
      top: 'center',
      textStyle: { color: 'rgba(255,255,255,0.7)', fontSize: 10 },
      pageTextStyle: { color: 'rgba(255,255,255,0.7)' }
    },
    series: [{
      type: 'pie',
      radius: ['35%', '65%'],
      center: ['35%', '50%'],
      roseType: 'radius',
      data: data.slice(0, 8).map((item, index) => ({
        name: item.category || '未分类',
        value: item.count,
        itemStyle: {
          color: colors[index],
          shadowColor: colors[index],
          shadowBlur: 15
        }
      })),
      label: { show: false },
      emphasis: {
        itemStyle: {
          shadowBlur: 30,
          shadowColor: 'rgba(0, 247, 255, 0.5)'
        },
        scale: true,
        scaleSize: 10
      },
      animationType: 'scale',
      animationEasing: 'elasticOut',
      animationDelay: function (idx) {
        return Math.random() * 200
      }
    }]
  }
  
  chartInstances.category.setOption(option)
}

const renderTrendChart = (data) => {
  if (!chartInstances.trend) {
    chartInstances.trend = echarts.init(trendChart.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(10, 22, 40, 0.95)',
      borderColor: '#00f7ff',
      textStyle: { color: '#fff' }
    },
    grid: { left: '8%', right: '5%', bottom: '15%', top: '10%' },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map(item => item.date.slice(5)),
      axisLabel: { color: 'rgba(255, 255, 255, 0.5)', fontSize: 9, rotate: 45 },
      axisLine: { lineStyle: { color: 'rgba(0, 247, 255, 0.3)' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: 'rgba(255, 255, 255, 0.5)', fontSize: 9 },
      splitLine: { lineStyle: { color: 'rgba(0, 247, 255, 0.1)' } }
    },
    series: [{
      data: data.map(item => item.count),
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0, 247, 255, 0.5)' },
          { offset: 1, color: 'rgba(0, 247, 255, 0.05)' }
        ])
      },
      lineStyle: { 
        color: '#00f7ff', 
        width: 3, 
        shadowColor: '#00f7ff', 
        shadowBlur: 15 
      },
      itemStyle: { 
        color: '#00f7ff', 
        borderColor: '#fff', 
        borderWidth: 2,
        shadowColor: '#00f7ff',
        shadowBlur: 10
      },
      animationEasing: 'elasticOut'
    }]
  }
  
  chartInstances.trend.setOption(option)
}

const renderInteractionChart = (data) => {
  if (!chartInstances.interaction) {
    chartInstances.interaction = echarts.init(interactionChart.value)
  }
  
  const colors = [
    { from: '#ef4444', to: '#f87171' },
    { from: '#f59e0b', to: '#fbbf24' },
    { from: '#10b981', to: '#34d399' },
    { from: '#00f7ff', to: '#67e8f9' },
    { from: '#8b5cf6', to: '#a78bfa' },
    { from: '#ec4899', to: '#f472b6' }
  ]
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(10, 22, 40, 0.95)',
      borderColor: '#00f7ff',
      textStyle: { color: '#fff' }
    },
    grid: { left: '8%', right: '5%', bottom: '10%', top: '15%' },
    xAxis: {
      type: 'category',
      data: ['点赞', '投币', '收藏', '弹幕', '分享', '评论'],
      axisLabel: { color: 'rgba(255, 255, 255, 0.7)', fontSize: 11 },
      axisLine: { lineStyle: { color: 'rgba(0, 247, 255, 0.3)' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: 'rgba(255, 255, 255, 0.5)' },
      splitLine: { lineStyle: { color: 'rgba(0, 247, 255, 0.1)' } }
    },
    series: [{
      data: [
        { value: Math.round(data.avgLike || 0), itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: colors[0].from }, { offset: 1, color: colors[0].to }]), shadowColor: colors[0].from, shadowBlur: 10 } },
        { value: Math.round(data.avgCoin || 0), itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: colors[1].from }, { offset: 1, color: colors[1].to }]), shadowColor: colors[1].from, shadowBlur: 10 } },
        { value: Math.round(data.avgFavorite || 0), itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: colors[2].from }, { offset: 1, color: colors[2].to }]), shadowColor: colors[2].from, shadowBlur: 10 } },
        { value: Math.round(data.avgDanmaku || 0), itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: colors[3].from }, { offset: 1, color: colors[3].to }]), shadowColor: colors[3].from, shadowBlur: 10 } },
        { value: Math.round(data.avgShare || 0), itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: colors[4].from }, { offset: 1, color: colors[4].to }]), shadowColor: colors[4].from, shadowBlur: 10 } },
        { value: Math.round(data.avgReply || 0), itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: colors[5].from }, { offset: 1, color: colors[5].to }]), shadowColor: colors[5].from, shadowBlur: 10 } }
      ],
      type: 'bar',
      barWidth: '50%',
      label: { show: true, position: 'top', color: '#fff', fontSize: 10 },
      itemStyle: { borderRadius: [8, 8, 0, 0] },
      animationEasing: 'elasticOut',
      animationDelay: function (idx) {
        return idx * 100
      }
    }]
  }
  
  chartInstances.interaction.setOption(option)
}

const renderRadarChart = (data) => {
  if (!chartInstances.radar) {
    chartInstances.radar = echarts.init(radarChart.value)
  }
  
  const maxVal = Math.max(data.avgLike || 1, data.avgCoin || 1, data.avgFavorite || 1, data.avgDanmaku || 1, data.avgShare || 1, data.avgReply || 1)
  
  const option = {
    tooltip: { backgroundColor: 'rgba(10, 22, 40, 0.95)', borderColor: '#00f7ff', textStyle: { color: '#fff' } },
    radar: {
      indicator: [
        { name: '点赞', max: maxVal * 1.2 },
        { name: '投币', max: maxVal * 1.2 },
        { name: '收藏', max: maxVal * 1.2 },
        { name: '弹幕', max: maxVal * 1.2 },
        { name: '分享', max: maxVal * 1.2 },
        { name: '评论', max: maxVal * 1.2 }
      ],
      center: ['50%', '55%'],
      radius: '60%',
      axisName: { color: 'rgba(255, 255, 255, 0.7)', fontSize: 10 },
      splitArea: { areaStyle: { color: ['rgba(0, 247, 255, 0.1)', 'rgba(139, 92, 246, 0.05)'] } },
      axisLine: { lineStyle: { color: 'rgba(0, 247, 255, 0.3)' } },
      splitLine: { lineStyle: { color: 'rgba(0, 247, 255, 0.2)' } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: [data.avgLike || 0, data.avgCoin || 0, data.avgFavorite || 0, data.avgDanmaku || 0, data.avgShare || 0, data.avgReply || 0],
        name: '互动数据',
        areaStyle: { 
          color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
            { offset: 0, color: 'rgba(0, 247, 255, 0.5)' },
            { offset: 1, color: 'rgba(139, 92, 246, 0.2)' }
          ])
        },
        lineStyle: { color: '#00f7ff', width: 2, shadowColor: '#00f7ff', shadowBlur: 10 },
        itemStyle: { color: '#00f7ff', borderColor: '#fff', borderWidth: 2, shadowColor: '#00f7ff', shadowBlur: 10 }
      }],
      animationEasing: 'elasticOut'
    }]
  }
  
  chartInstances.radar.setOption(option)
}

const renderGaugeChart = () => {
  if (!chartInstances.gauge) {
    chartInstances.gauge = echarts.init(gaugeChart.value)
  }
  
  const percent = dataCompleteness.value.completeness || 0
  
  const option = {
    series: [{
      type: 'gauge',
      center: ['50%', '50%'],
      radius: '80%',
      startAngle: 190,
      endAngle: -10,
      min: 0,
      max: 100,
      splitNumber: 8,
      itemStyle: { 
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#00f7ff' }, 
          { offset: 0.5, color: '#8b5cf6' },
          { offset: 1, color: '#10b981' }
        ]),
        shadowColor: '#00f7ff',
        shadowBlur: 15
      },
      progress: { show: true, width: 16 },
      pointer: { show: false },
      axisLine: { lineStyle: { width: 16, color: [[1, 'rgba(255, 255, 255, 0.08)']] } },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      anchor: { show: false },
      title: { show: false },
      detail: { show: false },
      data: [{ value: Math.round(percent) }],
      animationEasing: 'elasticOut'
    },
    {
      type: 'gauge',
      center: ['50%', '50%'],
      radius: '60%',
      startAngle: 190,
      endAngle: -10,
      min: 0,
      max: 100,
      itemStyle: { color: '#00f7ff', shadowColor: '#00f7ff', shadowBlur: 10 },
      progress: { show: true, width: 6 },
      pointer: { show: false },
      axisLine: { lineStyle: { width: 6, color: [[1, 'rgba(255, 255, 255, 0.05)']] } },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      detail: { show: false },
      data: [{ value: Math.round(percent) }],
      animationEasing: 'elasticOut'
    }]
  }
  
  chartInstances.gauge.setOption(option)
}

const renderWordCloudChart = (data) => {
  if (!chartInstances.wordCloud) {
    chartInstances.wordCloud = echarts.init(wordCloudChart.value)
  }
  
  const colors = ['#00f7ff', '#8b5cf6', '#10b981', '#f59e0b', '#ef4444', '#06b6d4', '#ec4899', '#6366f1']
  const maxValue = Math.max(...data.map(d => d.value))
  
  const option = {
    tooltip: { backgroundColor: 'rgba(10, 22, 40, 0.95)', borderColor: '#00f7ff', textStyle: { color: '#fff' } },
    series: [{
      type: 'wordCloud',
      shape: 'diamond',
      left: 'center',
      top: 'center',
      width: '90%',
      height: '90%',
      sizeRange: [12, 40],
      rotationRange: [-60, 60],
      rotationStep: 15,
      gridSize: 8,
      drawOutOfBound: false,
      textStyle: {
        fontFamily: 'Microsoft YaHei, sans-serif',
        fontWeight: 'bold',
        color: function () {
          return colors[Math.floor(Math.random() * colors.length)]
        }
      },
      emphasis: {
        textStyle: {
          shadowBlur: 20,
          shadowColor: '#00f7ff'
        }
      },
      data: data.map(item => ({
        name: item.name,
        value: item.value
      }))
    }]
  }
  
  chartInstances.wordCloud.setOption(option)
}

const renderFunnelChart = (data) => {
  if (!chartInstances.funnel) {
    chartInstances.funnel = echarts.init(funnelChart.value)
  }
  
  const totalView = overview.value.totalViewCount || 1
  const totalLike = overview.value.totalLikeCount || 0
  const totalCoin = overview.value.totalCoinCount || 0
  const totalFavorite = overview.value.totalFavoriteCount || 0
  const totalDanmaku = overview.value.totalDanmakuCount || 0
  const totalShare = overview.value.totalShareCount || 0
  const totalReply = overview.value.totalReplyCount || 0
  
  const getPercentage = (value) => {
    return Math.round((value / totalView) * 10000) / 100
  }
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}%',
      backgroundColor: 'rgba(10, 22, 40, 0.95)',
      borderColor: '#00f7ff',
      textStyle: { color: '#fff' }
    },
    legend: {
      type: 'scroll',
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { color: 'rgba(255, 255, 255, 0.7)', fontSize: 9 },
      pageTextStyle: { color: 'rgba(255, 255, 255, 0.7)' },
      selectedMode: 'multiple'
    },
    series: [{
      type: 'funnel',
      left: '10%',
      top: '10%',
      bottom: '10%',
      width: '60%',
      min: 0,
      max: 100,
      minSize: '20%',
      maxSize: '100%',
      sort: 'descending',
      gap: 3,
      label: {
        show: true,
        position: 'inside',
        color: '#fff',
        fontSize: 10,
        fontWeight: 'bold',
        formatter: '{b}: {c}%'
      },
      labelLine: { show: false },
      itemStyle: { 
        borderWidth: 0,
        shadowColor: 'rgba(0, 247, 255, 0.3)',
        shadowBlur: 10
      },
      emphasis: {
        label: { fontSize: 12 },
        itemStyle: {
          shadowColor: 'rgba(0, 247, 255, 0.6)',
          shadowBlur: 20
        }
      },
      data: [
        { value: 100, name: '播放量', itemStyle: { color: '#00f7ff' } },
        { value: getPercentage(totalDanmaku), name: '弹幕量', itemStyle: { color: '#10b981' } },
        { value: getPercentage(totalLike), name: '点赞数', itemStyle: { color: '#f59e0b' } },
        { value: getPercentage(totalCoin), name: '投币数', itemStyle: { color: '#ef4444' } },
        { value: getPercentage(totalFavorite), name: '收藏数', itemStyle: { color: '#8b5cf6' } },
        { value: getPercentage(totalShare), name: '分享数', itemStyle: { color: '#06b6d4' } },
        { value: getPercentage(totalReply), name: '评论数', itemStyle: { color: '#ec4899' } }
      ],
      animationEasing: 'elasticOut'
    }]
  }
  
  chartInstances.funnel.setOption(option)
}

const renderFunnelChartWithData = (data) => {
  if (!chartInstances.funnel) {
    chartInstances.funnel = echarts.init(funnelChart.value)
  }
  
  const totalView = data.totalViewCount || 1
  const totalLike = data.totalLikeCount || 0
  const totalCoin = data.totalCoinCount || 0
  const totalFavorite = data.totalFavoriteCount || 0
  const totalDanmaku = data.totalDanmakuCount || 0
  const totalShare = data.totalShareCount || 0
  const totalReply = data.totalReplyCount || 0
  
  const getPercentage = (value) => {
    return Math.round((value / totalView) * 10000) / 100
  }
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}%',
      backgroundColor: 'rgba(10, 22, 40, 0.95)',
      borderColor: '#00f7ff',
      textStyle: { color: '#fff' }
    },
    legend: {
      type: 'scroll',
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { color: 'rgba(255, 255, 255, 0.7)', fontSize: 9 },
      pageTextStyle: { color: 'rgba(255, 255, 255, 0.7)' },
      selectedMode: 'multiple'
    },
    series: [{
      type: 'funnel',
      left: '10%',
      top: '10%',
      bottom: '10%',
      width: '60%',
      min: 0,
      max: 100,
      minSize: '20%',
      maxSize: '100%',
      sort: 'descending',
      gap: 3,
      label: {
        show: true,
        position: 'inside',
        color: '#fff',
        fontSize: 10,
        fontWeight: 'bold',
        formatter: '{b}: {c}%'
      },
      labelLine: { show: false },
      itemStyle: { 
        borderWidth: 0,
        shadowColor: 'rgba(0, 247, 255, 0.3)',
        shadowBlur: 10
      },
      emphasis: {
        label: { fontSize: 12 },
        itemStyle: {
          shadowColor: 'rgba(0, 247, 255, 0.6)',
          shadowBlur: 20
        }
      },
      data: [
        { value: 100, name: '播放量', itemStyle: { color: '#00f7ff' } },
        { value: getPercentage(totalDanmaku), name: '弹幕量', itemStyle: { color: '#10b981' } },
        { value: getPercentage(totalLike), name: '点赞数', itemStyle: { color: '#f59e0b' } },
        { value: getPercentage(totalCoin), name: '投币数', itemStyle: { color: '#ef4444' } },
        { value: getPercentage(totalFavorite), name: '收藏数', itemStyle: { color: '#8b5cf6' } },
        { value: getPercentage(totalShare), name: '分享数', itemStyle: { color: '#06b6d4' } },
        { value: getPercentage(totalReply), name: '评论数', itemStyle: { color: '#ec4899' } }
      ],
      animationEasing: 'elasticOut'
    }]
  }
  
  chartInstances.funnel.setOption(option)
}

const handleResize = () => {
  const canvas = particleCanvas.value
  if (canvas) {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }
  Object.values(chartInstances).forEach(instance => instance?.resize())
}

const handleWindowResize = () => {
  handleResize()
}

onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  
  nextTick(() => {
    initParticles()
    
    fetchOverview()
    fetchInteractionStats()
    fetchTopVideos()
    fetchCategoryDistribution()
    fetchDailyTrend()
    fetchTagCloud()
    fetchCategoryLikes()
    fetchDataCompleteness()
  })
  
  window.addEventListener('resize', handleWindowResize)
})

onUnmounted(() => {
  if (timeInterval) clearInterval(timeInterval)
  if (particleAnimationId) cancelAnimationFrame(particleAnimationId)
  tweenAnimations.forEach(tween => tween.stop())
  window.removeEventListener('resize', handleWindowResize)
  Object.values(chartInstances).forEach(instance => instance?.dispose())
})
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes pulse {
  0%, 100% { opacity: 0.6; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.05); }
}

@keyframes glow {
  0%, 100% { box-shadow: 0 0 10px rgba(0, 247, 255, 0.4), 0 0 20px rgba(0, 247, 255, 0.2); }
  50% { box-shadow: 0 0 25px rgba(0, 247, 255, 0.8), 0 0 40px rgba(0, 247, 255, 0.4); }
}

@keyframes scan {
  0% { top: -10%; }
  100% { top: 110%; }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

.particle-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}

.analysis-screen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #0a0e27 0%, #0f172a 50%, #0a0e27 100%);
  color: #fff;
  overflow: hidden;
  z-index: 9999;
}

.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 2;
}

.grid-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(0, 247, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 247, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
}

.scan-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(0, 247, 255, 0.5), transparent);
  animation: scan 4s linear infinite;
}

.decoration-corner {
  position: absolute;
  width: 30px;
  height: 30px;
  border: 2px solid #00f7ff;
}

.corner-tl {
  top: 20px;
  left: 20px;
  border-right: none;
  border-bottom: none;
}

.corner-tr {
  top: 20px;
  right: 20px;
  border-left: none;
  border-bottom: none;
}

.corner-bl {
  bottom: 20px;
  left: 20px;
  border-right: none;
  border-top: none;
}

.corner-br {
  bottom: 20px;
  right: 20px;
  border-left: none;
  border-top: none;
}

.screen-header {
  height: clamp(60px, 10vh, 90px);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 clamp(15px, 2.5vw, 40px);
  background: linear-gradient(180deg, rgba(10, 14, 39, 0.95) 0%, rgba(10, 14, 39, 0.6) 100%);
  border-bottom: 2px solid rgba(0, 247, 255, 0.3);
  position: relative;
  z-index: 10;
}

.screen-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40%;
  height: 3px;
  background: linear-gradient(90deg, transparent, #00f7ff, #8b5cf6, #00f7ff, transparent);
}

.header-left {
  display: flex;
  align-items: center;
  gap: clamp(12px, 2vw, 20px);
}

.logo-box {
  width: clamp(45px, 5vw, 60px);
  height: clamp(45px, 5vw, 60px);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(0, 247, 255, 0.5);
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(0, 247, 255, 0.1), rgba(139, 92, 246, 0.1));
  animation: glow 3s ease-in-out infinite;
}

.logo-icon {
  width: 60%;
  height: 60%;
  animation: rotate 20s linear infinite;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.title-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.screen-header h1 {
  font-size: clamp(18px, 2.5vw, 28px);
  font-weight: 800;
  background: linear-gradient(90deg, #00f7ff, #8b5cf6, #10b981, #00f7ff);
  background-size: 300% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: gradient 4s ease infinite;
  letter-spacing: clamp(3px, 0.5vw, 6px);
  white-space: nowrap;
  text-shadow: 0 0 30px rgba(0, 247, 255, 0.3);
}

@keyframes gradient {
  0% { background-position: 0% center; }
  50% { background-position: 100% center; }
  100% { background-position: 0% center; }
}

.subtitle {
  font-size: clamp(9px, 0.9vw, 12px);
  color: rgba(255, 255, 255, 0.5);
  letter-spacing: 3px;
  text-transform: uppercase;
}

.header-center {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.digital-clock {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.clock-box {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 20px;
  background: linear-gradient(135deg, rgba(0, 247, 255, 0.15), rgba(139, 92, 246, 0.15));
  border: 1px solid rgba(0, 247, 255, 0.3);
  border-radius: 10px;
}

.clock-num {
  font-size: clamp(20px, 2.5vw, 32px);
  font-weight: 700;
  font-family: 'Courier New', monospace;
  color: #00f7ff;
  text-shadow: 0 0 10px rgba(0, 247, 255, 0.8);
  min-width: 1.2em;
  text-align: center;
}

.clock-separator {
  font-size: clamp(20px, 2.5vw, 32px);
  font-weight: 700;
  color: #00f7ff;
  animation: pulse 1s ease-in-out infinite;
}

.date-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.date {
  font-size: clamp(11px, 1.1vw, 14px);
  color: rgba(255, 255, 255, 0.7);
}

.weekday {
  font-size: clamp(10px, 1vw, 13px);
  color: #8b5cf6;
  padding: 3px 12px;
  background: rgba(139, 92, 246, 0.2);
  border-radius: 12px;
  border: 1px solid rgba(139, 92, 246, 0.3);
}

.header-right {
  display: flex;
  align-items: center;
  gap: clamp(15px, 2.5vw, 30px);
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.3);
  border-radius: 20px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 10px #10b981;
  animation: pulse 1.5s ease-in-out infinite;
}

.status-text {
  font-size: clamp(11px, 1vw, 13px);
  color: #10b981;
}

.exit-btn {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  border: none;
  border-radius: 20px;
  padding: clamp(6px, 0.8vw, 10px) clamp(15px, 2vw, 25px);
  font-weight: 600;
  font-size: clamp(11px, 1vw, 14px);
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 4px 15px rgba(239, 68, 68, 0.3);
  transition: all 0.3s ease;
}

.exit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(239, 68, 68, 0.5);
}

.btn-icon {
  font-size: 14px;
}

.screen-content {
  display: flex;
  height: calc(100vh - clamp(60px, 10vh, 90px));
  padding: clamp(12px, 1.8vw, 24px);
  gap: clamp(12px, 1.8vw, 24px);
  position: relative;
  z-index: 5;
  min-height: 0;
}

.left-panel, .right-panel {
  width: clamp(240px, 22%, 380px);
  display: flex;
  flex-direction: column;
  gap: clamp(10px, 1.5vw, 18px);
  min-width: 0;
  flex-shrink: 0;
}

.right-panel .chart-card:nth-child(1) {
  flex: 0 0 16%;
}

.right-panel .chart-card:nth-child(2) {
  flex: 1;
}

.right-panel .chart-card:nth-child(3) {
  flex: 0 0 32%;
}

.center-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: clamp(10px, 1.5vw, 18px);
}

.chart-card {
  flex: 1;
  min-width: 0;
  min-height: 0;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.85) 0%, rgba(15, 23, 42, 0.5) 100%);
  border-radius: clamp(10px, 1.2vw, 16px);
  border: 1px solid rgba(0, 247, 255, 0.2);
  padding: clamp(12px, 1.5vw, 18px);
  display: flex;
  flex-direction: column;
  animation: fadeIn 0.6s ease-out;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.chart-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #00f7ff, #8b5cf6, transparent);
}

.chart-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 50% 0%, rgba(0, 247, 255, 0.08), transparent 60%);
  pointer-events: none;
}

.card-header-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 247, 255, 0.5), transparent);
}

.card-title {
  font-size: clamp(13px, 1.4vw, 17px);
  font-weight: 700;
  margin-bottom: clamp(8px, 1vw, 12px);
  display: flex;
  align-items: center;
  gap: clamp(6px, 0.8vw, 10px);
  color: #fff;
  flex-wrap: wrap;
  position: relative;
  z-index: 1;
}

.title-line {
  width: 4px;
  height: 18px;
  background: linear-gradient(180deg, #00f7ff, #8b5cf6);
  border-radius: 2px;
}

.title-text {
  display: flex;
  align-items: center;
  gap: 6px;
}

.title-icon {
  font-size: clamp(15px, 1.6vw, 20px);
  filter: drop-shadow(0 0 5px rgba(0, 247, 255, 0.5));
}

.sort-radio {
  margin-left: auto;
}

.sort-radio :deep(.el-radio-button__inner) {
  background: rgba(0, 247, 255, 0.1);
  border-color: rgba(0, 247, 255, 0.3);
  color: rgba(255, 255, 255, 0.7);
  font-size: clamp(10px, 0.9vw, 12px);
  padding: 4px clamp(8px, 1vw, 12px);
  transition: all 0.3s ease;
}

.sort-radio :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #00f7ff, #8b5cf6);
  border-color: #00f7ff;
  color: #fff;
  box-shadow: 0 0 15px rgba(0, 247, 255, 0.4);
}

.category-select {
  margin-left: auto;
  width: 100px;
  position: relative;
  z-index: 100;
}

.category-select :deep(.el-input__wrapper) {
  background: rgba(0, 247, 255, 0.1);
  border-color: rgba(0, 247, 255, 0.3);
  box-shadow: none;
}

.category-select :deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.9);
  font-size: 12px;
}

.category-select :deep(.el-input__suffix) {
  color: rgba(0, 247, 255, 0.7);
}

.category-select :deep(.el-select__popper) {
  z-index: 9999 !important;
  background: rgba(10, 22, 40, 0.98);
  border: 1px solid rgba(0, 247, 255, 0.3);
}

.category-select :deep(.el-select-dropdown__item) {
  color: rgba(255, 255, 255, 0.8);
}

.category-select :deep(.el-select-dropdown__item:hover) {
  background: rgba(0, 247, 255, 0.2);
}

.category-select :deep(.el-select-dropdown__item.selected) {
  color: #00f7ff;
  background: rgba(0, 247, 255, 0.15);
}

.chart-container {
  flex: 1;
  min-height: clamp(90px, 14vh, 170px);
  position: relative;
  z-index: 1;
}

.gauge-container {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px;
  position: relative;
  z-index: 1;
}

.gauge-chart {
  flex: 0 0 60%;
  height: 100%;
  min-height: 90px;
}

.gauge-value {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.gauge-value .value-number {
  font-size: clamp(22px, 2.8vw, 32px);
  font-weight: 800;
  background: linear-gradient(135deg, #00f7ff, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 6px;
  text-shadow: 0 0 20px rgba(0, 247, 255, 0.5);
}

.gauge-value .value-label {
  font-size: clamp(11px, 1.1vw, 13px);
  color: rgba(255, 255, 255, 0.6);
}

.word-cloud {
  min-height: clamp(220px, 25vh, 380px);
}

.tag-cloud-card {
  overflow: visible !important;
}

.tag-cloud-card .card-title {
  z-index: 100;
  position: relative;
}

.interaction-card {
  overflow: visible !important;
}

.interaction-card .card-title {
  z-index: 100;
  position: relative;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: clamp(10px, 1.5vw, 18px);
}

.stat-card {
  background: linear-gradient(135deg, rgba(0, 247, 255, 0.12) 0%, rgba(139, 92, 246, 0.06) 100%);
  border-radius: clamp(10px, 1.2vw, 16px);
  padding: clamp(12px, 1.5vw, 20px);
  display: flex;
  align-items: center;
  gap: clamp(10px, 1.2vw, 16px);
  border: 1px solid rgba(0, 247, 255, 0.2);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  border-color: rgba(0, 247, 255, 0.5);
  box-shadow: 0 10px 40px rgba(0, 247, 255, 0.2);
}

.stat-border-top,
.stat-border-right,
.stat-border-bottom,
.stat-border-left {
  position: absolute;
  background: #00f7ff;
  transition: all 0.3s ease;
}

.stat-border-top {
  top: 0;
  left: 0;
  height: 2px;
  width: 0;
}

.stat-border-right {
  top: 0;
  right: 0;
  width: 2px;
  height: 0;
}

.stat-border-bottom {
  bottom: 0;
  right: 0;
  height: 2px;
  width: 0;
}

.stat-border-left {
  bottom: 0;
  left: 0;
  width: 2px;
  height: 0;
}

.stat-card:hover .stat-border-top,
.stat-card:hover .stat-border-bottom {
  width: 100%;
}

.stat-card:hover .stat-border-right,
.stat-card:hover .stat-border-left {
  height: 100%;
}

.stat-icon-wrapper {
  position: relative;
  width: clamp(45px, 5vw, 60px);
  height: clamp(45px, 5vw, 60px);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: clamp(10px, 1.2vw, 16px);
  background: linear-gradient(135deg, #00f7ff, #8b5cf6);
  opacity: 0.2;
  animation: pulse 2s ease-in-out infinite;
}

.stat-icon {
  position: relative;
  font-size: clamp(22px, 2.5vw, 30px);
  z-index: 1;
}

.stat-content {
  flex: 1;
  min-width: 0;
  position: relative;
  z-index: 1;
}

.stat-value {
  display: flex;
  align-items: baseline;
  gap: 5px;
}

.stat-card .value-number {
  font-size: clamp(18px, 2.2vw, 28px);
  font-weight: 800;
  background: linear-gradient(135deg, #00f7ff, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.2;
}

.value-unit {
  font-size: clamp(10px, 0.9vw, 12px);
  color: rgba(255, 255, 255, 0.6);
}

.stat-label {
  font-size: clamp(11px, 1.1vw, 14px);
  color: rgba(255, 255, 255, 0.6);
  margin-top: 5px;
}

.stat-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(0, 247, 255, 0.15), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.stat-card:hover .stat-glow {
  opacity: 1;
}

.stat-card:nth-child(2) .stat-icon-bg { background: linear-gradient(135deg, #10b981, #34d399); }
.stat-card:nth-child(3) .stat-icon-bg { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.stat-card:nth-child(4) .stat-icon-bg { background: linear-gradient(135deg, #ef4444, #f87171); }
.stat-card:nth-child(5) .stat-icon-bg { background: linear-gradient(135deg, #8b5cf6, #a78bfa); }
.stat-card:nth-child(6) .stat-icon-bg { background: linear-gradient(135deg, #06b6d4, #22d3ee); }
.stat-card:nth-child(7) .stat-icon-bg { background: linear-gradient(135deg, #ec4899, #f472b6); }
.stat-card:nth-child(8) .stat-icon-bg { background: linear-gradient(135deg, #6366f1, #818cf8); }

.center-charts {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: clamp(10px, 1.5vw, 18px);
  min-height: 0;
}

.center-top-row {
  display: flex;
  gap: clamp(10px, 1.5vw, 18px);
  flex: 0 0 42%;
  min-height: 0;
}

.center-top-row .chart-card {
  flex: 1;
  min-width: 0;
}

.center-top-row .chart-card .chart-container {
  min-height: 200px;
}

.top-videos-card {
  flex: 1;
  min-height: 0;
}

.top-videos-list {
  flex: 1;
  overflow-y: auto;
}

.top-videos-list::-webkit-scrollbar {
  width: 5px;
}

.top-videos-list::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #00f7ff, #8b5cf6);
  border-radius: 3px;
}

.video-item {
  display: flex;
  align-items: center;
  padding: clamp(8px, 1vw, 12px) clamp(10px, 1.2vw, 15px);
  border-radius: 10px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid transparent;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.video-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(0, 247, 255, 0.1), transparent);
  transition: left 0.5s ease;
}

.video-item:hover::before {
  left: 100%;
}

.video-item:hover {
  background: rgba(0, 247, 255, 0.08);
  border-color: rgba(0, 247, 255, 0.3);
  transform: translateX(8px);
}

.rank {
  width: clamp(24px, 2.5vw, 30px);
  height: clamp(24px, 2.5vw, 30px);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: clamp(11px, 1.1vw, 13px);
  font-weight: 700;
  margin-right: clamp(10px, 1.2vw, 15px);
  background: rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.rank-1 { 
  background: linear-gradient(135deg, #ffd700, #ffaa00);
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.5);
}
.rank-2 { 
  background: linear-gradient(135deg, #c0c0c0, #a0a0a0);
  box-shadow: 0 0 15px rgba(192, 192, 192, 0.5);
}
.rank-3 { 
  background: linear-gradient(135deg, #cd7f32, #b87333);
  box-shadow: 0 0 15px rgba(205, 127, 50, 0.5);
}

.medal {
  font-size: clamp(13px, 1.5vw, 17px);
}

.video-item .title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: clamp(11px, 1.1vw, 14px);
  color: rgba(255, 255, 255, 0.9);
  position: relative;
  z-index: 1;
}

.video-stats {
  display: flex;
  gap: clamp(10px, 1.2vw, 15px);
  margin-left: clamp(10px, 1.2vw, 15px);
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.video-item .stat-item {
  font-size: clamp(10px, 0.9vw, 12px);
  color: rgba(255, 255, 255, 0.5);
}

@media (max-width: 1400px) {
  .stats-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .center-top-row {
    flex-basis: 47%;
  }
  
  .left-panel, .right-panel {
    width: clamp(210px, 24%, 340px);
  }
}

@media (max-width: 1200px) {
  .left-panel, .right-panel {
    width: clamp(190px, 26%, 300px);
  }
  
  .stats-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .header-center {
    display: none;
  }
  
  .center-top-row {
    flex-basis: 50%;
  }
}

@media (max-width: 992px) {
  .screen-content {
    flex-direction: column;
    overflow-y: auto;
  }
  
  .left-panel, .right-panel {
    width: 100%;
    flex-direction: row;
    flex-wrap: wrap;
  }
  
  .left-panel .chart-card, .right-panel .chart-card {
    flex: 1 1 calc(50% - 10px);
    min-width: 200px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .center-top-row {
    flex-direction: column;
    flex-basis: auto;
  }
  
  .center-charts {
    min-height: 500px;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .left-panel .chart-card, .right-panel .chart-card {
    flex: 1 1 100%;
  }
  
  .screen-header h1 {
    font-size: 16px;
  }
  
  .digital-clock {
    display: none;
  }
  
  .center-charts {
    min-height: 600px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .left-panel .chart-card, .right-panel .chart-card {
    flex: 1 1 100%;
  }
  
  .exit-btn {
    padding: 5px 12px;
    font-size: 11px;
  }
  
  .screen-content {
    padding: 12px;
  }
  
  .center-charts {
    min-height: 700px;
  }
}
</style>