<template>
  <div class="analysis-screen">
    <div class="bg-decoration">
      <div class="decoration-line line-1"></div>
      <div class="decoration-line line-2"></div>
      <div class="decoration-line line-3"></div>
      <div class="decoration-line line-4"></div>
    </div>
    
    <div class="screen-header">
      <div class="header-left">
        <div class="logo-icon">📊</div>
        <h1>B站视频数据分析大屏</h1>
      </div>
      <div class="header-center">
        <div class="date-info">
          <span class="date">{{ currentDate }}</span>
          <span class="weekday">{{ currentWeekday }}</span>
        </div>
      </div>
      <div class="header-right">
        <div class="time-display">
          <span class="time">{{ currentTime }}</span>
        </div>
        <el-button type="primary" size="small" @click="goBack" class="exit-btn">
          <span>退出大屏</span>
        </el-button>
      </div>
    </div>
    
    <div class="screen-content">
      <div class="left-panel">
        <div class="chart-card">
          <div class="card-title">
            <span class="title-icon">📁</span>
            <span>分类分布</span>
          </div>
          <div ref="categoryChart" class="chart-container"></div>
        </div>
        <div class="chart-card">
          <div class="card-title">
            <span class="title-icon">📈</span>
            <span>每日入库趋势</span>
          </div>
          <div ref="trendChart" class="chart-container"></div>
        </div>
        <div class="chart-card">
          <div class="card-title">
            <span class="title-icon">🎯</span>
            <span>视频质量评分</span>
          </div>
          <div ref="radarChart" class="chart-container"></div>
        </div>
      </div>
      
      <div class="center-panel">
        <div class="stats-grid">
          <div class="stat-card" v-for="(stat, index) in statsCards" :key="index">
            <div class="stat-icon">{{ stat.icon }}</div>
            <div class="stat-content">
              <div class="stat-value">
                <span class="value-number">{{ stat.value }}</span>
                <span class="value-unit">{{ stat.unit }}</span>
              </div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
            <div class="stat-decoration"></div>
          </div>
        </div>
        
        <div class="center-charts">
          <div class="center-top-row">
            <div class="chart-card">
              <div class="card-title">
                <span class="title-icon">📊</span>
                <span>互动数据对比</span>
              </div>
              <div ref="interactionChart" class="chart-container"></div>
            </div>
            
            <div class="chart-card">
              <div class="card-title">
                <span class="title-icon">�</span>
                <span>分类互动数据对比</span>
              </div>
              <div ref="categoryLikesChart" class="chart-container"></div>
            </div>
          </div>
          
          <div class="chart-card top-videos-card">
            <div class="card-title">
              <span class="title-icon">🏆</span>
              <span>热门视频 TOP 10</span>
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
          <div class="card-title">
            <span class="title-icon">💎</span>
            <span>数据完整度</span>
          </div>
          <div class="gauge-container">
            <div ref="gaugeChart" class="gauge-chart"></div>
            <div class="gauge-value">
              <div class="value-number">{{ dataCompleteness.completeness }}%</div>
              <div class="value-label">数据完整度</div>
            </div>
          </div>
        </div>
        <div class="chart-card">
          <div class="card-title">
            <span class="title-icon">�</span>
            <span>热门标签云</span>
          </div>
          <div ref="wordCloudChart" class="chart-container word-cloud"></div>
        </div>
        <div class="chart-card">
          <div class="card-title">
            <span class="title-icon">⚡</span>
            <span>互动率分析</span>
          </div>
          <div ref="funnelChart" class="chart-container"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import 'echarts-wordcloud'
import api from '../utils/api'

const router = useRouter()
const currentTime = ref('')
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
const dataCompleteness = ref({
  totalVideos: 0,
  duplicateBvids: 0,
  nullBvids: 0,
  nullTitles: 0,
  nullCategories: 0,
  completeness: 100
})

const statsCards = computed(() => [
  { icon: '📹', label: '视频总数', value: formatNumber(overview.value.totalVideos), unit: '' },
  { icon: '👁️', label: '总播放量', value: formatNumber(overview.value.totalViewCount), unit: '' },
  { icon: '👍', label: '总点赞数', value: formatNumber(overview.value.totalLikeCount), unit: '' },
  { icon: '🪙', label: '总投币数', value: formatNumber(overview.value.totalCoinCount), unit: '' },
  { icon: '⭐', label: '总收藏数', value: formatNumber(overview.value.totalFavoriteCount), unit: '' },
  { icon: '💬', label: '总弹幕数', value: formatNumber(overview.value.totalDanmakuCount), unit: '' },
  { icon: '🔄', label: '总分享数', value: formatNumber(overview.value.totalShareCount), unit: '' },
  { icon: '📝', label: '总评论数', value: formatNumber(overview.value.totalReplyCount), unit: '' }
])

const categoryChart = ref(null)
const trendChart = ref(null)
const interactionChart = ref(null)
const radarChart = ref(null)
const gaugeChart = ref(null)
const wordCloudChart = ref(null)
const funnelChart = ref(null)
const categoryLikesChart = ref(null)

let chartInstances = {}
let timeInterval = null

const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 100000000) return (num / 100000000).toFixed(2) + '亿'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  return num.toLocaleString()
}

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
  currentDate.value = now.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  currentWeekday.value = weekdays[now.getDay()]
}

const goBack = () => {
  router.push('/')
}

const fetchOverview = async () => {
  try {
    const res = await api.get('/admin/analysis/overview')
    if (res.data.success) {
      overview.value = res.data
      renderFunnelChart()
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
    if (res.data.success) {
      renderCategoryChart(res.data.data)
    }
  } catch (e) {
    console.error(e)
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

const fetchTagCloud = async () => {
  try {
    const res = await api.get('/admin/analysis/tag-cloud')
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
    '#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', 
    '#13c2c2', '#eb2f96', '#fa8c16', '#2f54eb', '#13c2c2'
  ]
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(0, 21, 41, 0.9)',
      borderColor: '#1890ff',
      textStyle: { color: '#fff' },
      formatter: '{b}: {c}'
    },
    grid: { left: '3%', right: '10%', bottom: '3%', top: '3%', containLabel: true },
    xAxis: {
      type: 'value',
      axisLabel: { color: 'rgba(255, 255, 255, 0.5)', fontSize: 10 },
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
      splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.08)' } }
    },
    yAxis: {
      type: 'category',
      data: data.map(item => item.category).reverse(),
      axisLabel: { color: 'rgba(255, 255, 255, 0.7)', fontSize: 10 },
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } }
    },
    series: [{
      type: 'bar',
      data: data.map((item, index) => ({
        value: item.totalLikes,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: colors[index % colors.length] },
            { offset: 1, color: colors[(index + 1) % colors.length] }
          ])
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
      itemStyle: { borderRadius: [0, 4, 4, 0] }
    }]
  }
  
  chartInstances.categoryLikes.setOption(option)
}

const renderCategoryChart = (data) => {
  if (!chartInstances.category) {
    chartInstances.category = echarts.init(categoryChart.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      backgroundColor: 'rgba(0, 21, 41, 0.9)',
      borderColor: '#1890ff',
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
          color: ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2', '#eb2f96', '#fa8c16'][index]
        }
      })),
      label: { show: false },
      emphasis: {
        itemStyle: {
          shadowBlur: 20,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
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
      backgroundColor: 'rgba(0, 21, 41, 0.9)',
      borderColor: '#1890ff',
      textStyle: { color: '#fff' }
    },
    grid: { left: '8%', right: '5%', bottom: '15%', top: '10%' },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map(item => item.date.slice(5)),
      axisLabel: { color: 'rgba(255, 255, 255, 0.5)', fontSize: 9, rotate: 45 },
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: 'rgba(255, 255, 255, 0.5)', fontSize: 9 },
      splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.08)' } }
    },
    series: [{
      data: data.map(item => item.count),
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(24, 144, 255, 0.6)' },
          { offset: 1, color: 'rgba(24, 144, 255, 0.05)' }
        ])
      },
      lineStyle: { color: '#1890ff', width: 3, shadowColor: 'rgba(24, 144, 255, 0.5)', shadowBlur: 10 },
      itemStyle: { color: '#1890ff', borderColor: '#fff', borderWidth: 2 }
    }]
  }
  
  chartInstances.trend.setOption(option)
}

const renderInteractionChart = (data) => {
  if (!chartInstances.interaction) {
    chartInstances.interaction = echarts.init(interactionChart.value)
  }
  
  const colors = [
    { from: '#f5222d', to: '#ff4d4f' },
    { from: '#faad14', to: '#ffc53d' },
    { from: '#52c41a', to: '#73d13d' },
    { from: '#1890ff', to: '#40a9ff' },
    { from: '#722ed1', to: '#9254de' },
    { from: '#eb2f96', to: '#f759ab' }
  ]
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(0, 21, 41, 0.9)',
      borderColor: '#1890ff',
      textStyle: { color: '#fff' }
    },
    grid: { left: '8%', right: '5%', bottom: '10%', top: '15%' },
    xAxis: {
      type: 'category',
      data: ['点赞', '投币', '收藏', '弹幕', '分享', '评论'],
      axisLabel: { color: 'rgba(255, 255, 255, 0.7)', fontSize: 11 },
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: 'rgba(255, 255, 255, 0.5)' },
      splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.08)' } }
    },
    series: [{
      data: [
        { value: Math.round(data.avgLike || 0), itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: colors[0].from }, { offset: 1, color: colors[0].to }]) } },
        { value: Math.round(data.avgCoin || 0), itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: colors[1].from }, { offset: 1, color: colors[1].to }]) } },
        { value: Math.round(data.avgFavorite || 0), itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: colors[2].from }, { offset: 1, color: colors[2].to }]) } },
        { value: Math.round(data.avgDanmaku || 0), itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: colors[3].from }, { offset: 1, color: colors[3].to }]) } },
        { value: Math.round(data.avgShare || 0), itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: colors[4].from }, { offset: 1, color: colors[4].to }]) } },
        { value: Math.round(data.avgReply || 0), itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: colors[5].from }, { offset: 1, color: colors[5].to }]) } }
      ],
      type: 'bar',
      barWidth: '50%',
      label: { show: true, position: 'top', color: '#fff', fontSize: 10 },
      itemStyle: { borderRadius: [4, 4, 0, 0] }
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
    tooltip: { backgroundColor: 'rgba(0, 21, 41, 0.9)', borderColor: '#1890ff', textStyle: { color: '#fff' } },
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
      splitArea: { areaStyle: { color: ['rgba(24, 144, 255, 0.1)', 'rgba(24, 144, 255, 0.05)'] } },
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
      splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.1)' } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: [data.avgLike || 0, data.avgCoin || 0, data.avgFavorite || 0, data.avgDanmaku || 0, data.avgShare || 0, data.avgReply || 0],
        name: '互动数据',
        areaStyle: { color: 'rgba(24, 144, 255, 0.3)' },
        lineStyle: { color: '#1890ff', width: 2 },
        itemStyle: { color: '#1890ff', borderColor: '#fff', borderWidth: 2 }
      }]
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
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#1890ff' }, { offset: 1, color: '#52c41a' }]) },
      progress: { show: true, width: 16 },
      pointer: { show: false },
      axisLine: { lineStyle: { width: 16, color: [[1, 'rgba(255, 255, 255, 0.1)']] } },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      anchor: { show: false },
      title: { show: false },
      detail: { show: false },
      data: [{ value: Math.round(percent) }]
    },
    {
      type: 'gauge',
      center: ['50%', '50%'],
      radius: '60%',
      startAngle: 190,
      endAngle: -10,
      min: 0,
      max: 100,
      itemStyle: { color: '#1890ff' },
      progress: { show: true, width: 6 },
      pointer: { show: false },
      axisLine: { lineStyle: { width: 6, color: [[1, 'rgba(255, 255, 255, 0.05)']] } },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      detail: { show: false },
      data: [{ value: Math.round(percent) }]
    }]
  }
  
  chartInstances.gauge.setOption(option)
}

const renderWordCloudChart = (data) => {
  if (!chartInstances.wordCloud) {
    chartInstances.wordCloud = echarts.init(wordCloudChart.value)
  }
  
  const colors = ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2', '#eb2f96', '#fa8c16']
  const maxValue = Math.max(...data.map(d => d.value))
  
  const option = {
    tooltip: { backgroundColor: 'rgba(0, 21, 41, 0.9)', borderColor: '#1890ff', textStyle: { color: '#fff' } },
    series: [{
      type: 'wordCloud',
      shape: 'circle',
      left: 'center',
      top: 'center',
      width: '90%',
      height: '90%',
      sizeRange: [12, 36],
      rotationRange: [-45, 45],
      rotationStep: 45,
      gridSize: 8,
      drawOutOfBound: false,
      textStyle: {
        fontFamily: 'sans-serif',
        fontWeight: 'bold',
        color: function () {
          return colors[Math.floor(Math.random() * colors.length)]
        }
      },
      emphasis: {
        textStyle: {
          shadowBlur: 10,
          shadowColor: '#333'
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
      backgroundColor: 'rgba(0, 21, 41, 0.9)',
      borderColor: '#1890ff',
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
      gap: 2,
      label: {
        show: true,
        position: 'inside',
        color: '#fff',
        fontSize: 10,
        formatter: '{b}: {c}%'
      },
      labelLine: { show: false },
      itemStyle: { borderWidth: 0 },
      emphasis: {
        label: { fontSize: 12 }
      },
      data: [
        { value: 100, name: '播放量', itemStyle: { color: '#1890ff' } },
        { value: getPercentage(totalDanmaku), name: '弹幕量', itemStyle: { color: '#52c41a' } },
        { value: getPercentage(totalLike), name: '点赞数', itemStyle: { color: '#faad14' } },
        { value: getPercentage(totalCoin), name: '投币数', itemStyle: { color: '#f5222d' } },
        { value: getPercentage(totalFavorite), name: '收藏数', itemStyle: { color: '#722ed1' } },
        { value: getPercentage(totalShare), name: '分享数', itemStyle: { color: '#13c2c2' } },
        { value: getPercentage(totalReply), name: '评论数', itemStyle: { color: '#eb2f96' } }
      ]
    }]
  }
  
  chartInstances.funnel.setOption(option)
}

const handleResize = () => {
  Object.values(chartInstances).forEach(instance => instance?.resize())
}

onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  
  fetchOverview()
  fetchInteractionStats()
  fetchTopVideos()
  fetchCategoryDistribution()
  fetchDailyTrend()
  fetchTagCloud()
  fetchCategoryLikes()
  fetchDataCompleteness()
  
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (timeInterval) clearInterval(timeInterval)
  window.removeEventListener('resize', handleResize)
  Object.values(chartInstances).forEach(instance => instance?.dispose())
})
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes pulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

@keyframes glow {
  0%, 100% { box-shadow: 0 0 5px rgba(24, 144, 255, 0.5); }
  50% { box-shadow: 0 0 20px rgba(24, 144, 255, 0.8); }
}

@keyframes slideIn {
  from { transform: translateX(-100%); }
  to { transform: translateX(100%); }
}

.analysis-screen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #0a1628 0%, #1a2a4a 50%, #0a1628 100%);
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
}

.decoration-line {
  position: absolute;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(24, 144, 255, 0.3), transparent);
  animation: slideIn 8s linear infinite;
}

.line-1 { top: 20%; width: 100%; animation-delay: 0s; }
.line-2 { top: 40%; width: 100%; animation-delay: 2s; }
.line-3 { top: 60%; width: 100%; animation-delay: 4s; }
.line-4 { top: 80%; width: 100%; animation-delay: 6s; }

.screen-header {
  height: clamp(50px, 8vh, 80px);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 clamp(10px, 2vw, 30px);
  background: linear-gradient(180deg, rgba(0, 21, 41, 0.9) 0%, rgba(0, 21, 41, 0.6) 100%);
  border-bottom: 1px solid rgba(24, 144, 255, 0.3);
  position: relative;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: clamp(8px, 1.5vw, 15px);
}

.logo-icon {
  font-size: clamp(24px, 3vw, 36px);
  animation: pulse 2s ease-in-out infinite;
}

.screen-header h1 {
  font-size: clamp(16px, 2.2vw, 28px);
  font-weight: 700;
  background: linear-gradient(90deg, #1890ff, #52c41a, #1890ff);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: gradient 3s ease infinite;
  letter-spacing: clamp(2px, 0.3vw, 4px);
  white-space: nowrap;
}

@keyframes gradient {
  0% { background-position: 0% center; }
  50% { background-position: 100% center; }
  100% { background-position: 0% center; }
}

.header-center {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.date-info {
  display: flex;
  gap: clamp(8px, 1vw, 15px);
  align-items: center;
}

.date {
  font-size: clamp(12px, 1.2vw, 16px);
  color: rgba(255, 255, 255, 0.8);
}

.weekday {
  font-size: clamp(11px, 1vw, 14px);
  color: #1890ff;
  padding: 2px clamp(6px, 0.8vw, 10px);
  background: rgba(24, 144, 255, 0.2);
  border-radius: 10px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: clamp(10px, 2vw, 25px);
}

.time-display {
  padding: clamp(4px, 0.6vw, 8px) clamp(10px, 1.5vw, 20px);
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.2), rgba(82, 196, 26, 0.2));
  border-radius: 20px;
  border: 1px solid rgba(24, 144, 255, 0.3);
}

.time {
  font-size: clamp(14px, 1.6vw, 20px);
  font-weight: 600;
  font-family: 'Courier New', monospace;
  color: #fff;
  letter-spacing: 2px;
}

.exit-btn {
  background: linear-gradient(135deg, #1890ff, #52c41a);
  border: none;
  border-radius: 20px;
  padding: clamp(5px, 0.6vw, 8px) clamp(12px, 1.5vw, 20px);
  font-weight: 500;
  font-size: clamp(11px, 1vw, 14px);
}

.screen-content {
  display: flex;
  height: calc(100vh - clamp(50px, 8vh, 80px));
  padding: clamp(10px, 1.5vw, 20px);
  gap: clamp(10px, 1.5vw, 20px);
  position: relative;
  z-index: 5;
  min-height: 0;
}

.left-panel, .right-panel {
  width: clamp(220px, 20%, 360px);
  display: flex;
  flex-direction: column;
  gap: clamp(8px, 1.2vw, 15px);
  min-width: 0;
  flex-shrink: 0;
}

.right-panel .chart-card:nth-child(1) {
  flex: 0 0 15%;
}

.right-panel .chart-card:nth-child(2) {
  flex: 1;
}

.right-panel .chart-card:nth-child(3) {
  flex: 0 0 30%;
}

.center-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: clamp(8px, 1.2vw, 15px);
}

.chart-card {
  flex: 1;
  min-width: 0;
  min-height: 0;
  background: linear-gradient(135deg, rgba(0, 21, 41, 0.8) 0%, rgba(0, 21, 41, 0.4) 100%);
  border-radius: clamp(8px, 1vw, 12px);
  border: 1px solid rgba(24, 144, 255, 0.2);
  padding: clamp(10px, 1.2vw, 15px);
  display: flex;
  flex-direction: column;
  animation: fadeIn 0.5s ease-out;
  position: relative;
  overflow: hidden;
}

.chart-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, #1890ff, transparent);
}

.card-title {
  font-size: clamp(12px, 1.2vw, 15px);
  font-weight: 600;
  margin-bottom: clamp(6px, 0.8vw, 10px);
  display: flex;
  align-items: center;
  gap: clamp(5px, 0.6vw, 8px);
  color: #fff;
  flex-wrap: wrap;
}

.title-icon {
  font-size: clamp(14px, 1.4vw, 18px);
}

.sort-radio {
  margin-left: auto;
}

.sort-radio :deep(.el-radio-button__inner) {
  background: rgba(24, 144, 255, 0.1);
  border-color: rgba(24, 144, 255, 0.3);
  color: rgba(255, 255, 255, 0.7);
  font-size: clamp(10px, 0.8vw, 11px);
  padding: 3px clamp(6px, 0.8vw, 10px);
}

.sort-radio :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #1890ff;
  border-color: #1890ff;
  color: #fff;
}

.chart-container {
  flex: 1;
  min-height: clamp(80px, 12vh, 150px);
}

.gauge-container {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px;
}

.gauge-chart {
  flex: 0 0 60%;
  height: 100%;
  min-height: 80px;
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
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
  margin-bottom: 5px;
  text-shadow: 0 0 10px rgba(24, 144, 255, 0.5);
}

.gauge-value .value-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.word-cloud {
  min-height: clamp(200px, 30vh, 350px);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: clamp(8px, 1.2vw, 15px);
}

.stat-card {
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.15) 0%, rgba(24, 144, 255, 0.05) 100%);
  border-radius: clamp(8px, 1vw, 12px);
  padding: clamp(10px, 1.2vw, 18px);
  display: flex;
  align-items: center;
  gap: clamp(8px, 1vw, 15px);
  border: 1px solid rgba(24, 144, 255, 0.2);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  border-color: rgba(24, 144, 255, 0.5);
  animation: glow 2s ease-in-out infinite;
}

.stat-card:nth-child(2) .stat-icon { background: linear-gradient(135deg, #52c41a, #73d13d); }
.stat-card:nth-child(3) .stat-icon { background: linear-gradient(135deg, #f5222d, #ff4d4f); }
.stat-card:nth-child(4) .stat-icon { background: linear-gradient(135deg, #faad14, #ffc53d); }
.stat-card:nth-child(5) .stat-icon { background: linear-gradient(135deg, #722ed1, #9254de); }
.stat-card:nth-child(6) .stat-icon { background: linear-gradient(135deg, #13c2c2, #36cfc9); }
.stat-card:nth-child(7) .stat-icon { background: linear-gradient(135deg, #eb2f96, #f759ab); }
.stat-card:nth-child(8) .stat-icon { background: linear-gradient(135deg, #fa8c16, #ffa940); }

.stat-icon {
  font-size: clamp(20px, 2.2vw, 28px);
  width: clamp(36px, 4vw, 50px);
  height: clamp(36px, 4vw, 50px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  border-radius: clamp(8px, 1vw, 12px);
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.value-number {
  font-size: clamp(16px, 2vw, 24px);
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
}

.value-unit {
  font-size: clamp(10px, 0.9vw, 12px);
  color: rgba(255, 255, 255, 0.6);
}

.stat-label {
  font-size: clamp(10px, 1vw, 13px);
  color: rgba(255, 255, 255, 0.6);
  margin-top: 4px;
}

.stat-decoration {
  position: absolute;
  right: -10px;
  bottom: -10px;
  width: 60px;
  height: 60px;
  background: radial-gradient(circle, rgba(24, 144, 255, 0.2) 0%, transparent 70%);
  border-radius: 50%;
}

.center-charts {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: clamp(8px, 1.2vw, 15px);
  min-height: 0;
}

.center-top-row {
  display: flex;
  gap: clamp(8px, 1.2vw, 15px);
  flex: 0 0 40%;
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
  width: 4px;
}

.top-videos-list::-webkit-scrollbar-thumb {
  background: rgba(24, 144, 255, 0.3);
  border-radius: 2px;
}

.video-item {
  display: flex;
  align-items: center;
  padding: clamp(6px, 0.8vw, 10px) clamp(8px, 1vw, 12px);
  border-radius: 8px;
  margin-bottom: 6px;
  background: rgba(255, 255, 255, 0.03);
  transition: all 0.3s ease;
}

.video-item:hover {
  background: rgba(24, 144, 255, 0.1);
  transform: translateX(5px);
}

.rank {
  width: clamp(22px, 2.2vw, 28px);
  height: clamp(22px, 2.2vw, 28px);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: clamp(10px, 1vw, 12px);
  font-weight: 600;
  margin-right: clamp(8px, 1vw, 12px);
  background: rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
}

.rank-1 { background: linear-gradient(135deg, #ffd700, #ffaa00); }
.rank-2 { background: linear-gradient(135deg, #c0c0c0, #a0a0a0); }
.rank-3 { background: linear-gradient(135deg, #cd7f32, #b87333); }

.medal {
  font-size: clamp(12px, 1.4vw, 16px);
}

.title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: clamp(11px, 1vw, 13px);
  color: rgba(255, 255, 255, 0.9);
}

.video-stats {
  display: flex;
  gap: clamp(8px, 1vw, 12px);
  margin-left: clamp(8px, 1vw, 15px);
  flex-shrink: 0;
}

.stat-item {
  font-size: clamp(9px, 0.85vw, 11px);
  color: rgba(255, 255, 255, 0.5);
}

@media (max-width: 1400px) {
  .stats-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .center-top-row {
    flex-basis: 45%;
  }
  
  .left-panel, .right-panel {
    width: clamp(200px, 22%, 320px);
  }
}

@media (max-width: 1200px) {
  .left-panel, .right-panel {
    width: clamp(180px, 24%, 280px);
  }
  
  .stats-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .header-center {
    display: none;
  }
  
  .center-top-row {
    flex-basis: 48%;
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
    font-size: 14px;
  }
  
  .time-display {
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
    padding: 4px 10px;
    font-size: 10px;
  }
  
  .screen-content {
    padding: 10px;
  }
  
  .center-charts {
    min-height: 700px;
  }
}
</style>
