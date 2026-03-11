<template>
  <div class="creator-center">
    <div class="center-header">
      <h1>创作者中心</h1>
    </div>
    
    <div class="center-content">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="menu-card" @click="router.push('/upload')">
            <div class="menu-icon">
              <el-icon :size="40"><Upload /></el-icon>
            </div>
            <h3>发布视频</h3>
            <p>上传并发布您的视频作品</p>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="menu-card" @click="loadMyVideos">
            <div class="menu-icon">
              <el-icon :size="40"><VideoPlay /></el-icon>
            </div>
            <h3>我的视频</h3>
            <p>管理您上传的视频</p>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="menu-card" @click="router.push('/')">
            <div class="menu-icon">
              <el-icon :size="40"><DataAnalysis /></el-icon>
            </div>
            <h3>数据分析</h3>
            <p>查看视频播放数据统计</p>
          </el-card>
        </el-col>
      </el-row>
      
      <div class="my-videos" v-if="showMyVideos">
        <h2>我的视频列表</h2>
        <el-empty v-if="myVideos.length === 0" description="暂无上传视频" />
        <el-row :gutter="20" v-else>
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="video in myVideos" :key="video.id">
            <el-card shadow="hover" class="video-card" @click="playVideo(video)">
              <div class="video-cover">
                <img :src="video.coverUrl || defaultCover" alt="视频封面" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, VideoPlay, View, DataAnalysis } from '@element-plus/icons-vue'

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

const router = useRouter()

const myVideos = ref<Video[]>([])
const showMyVideos = ref(false)
const defaultCover = 'http://localhost:8080/backend/image/default_image/defaultImage.png'

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
      showMyVideos.value = true
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
    // 用户取消删除
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.creator-center {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.center-header {
  text-align: center;
  margin-bottom: 40px;
}

.center-header h1 {
  margin: 0;
  color: #409eff;
  font-size: 28px;
  font-weight: 600;
}

.center-content {
  margin-top: 20px;
}

.menu-card {
  cursor: pointer;
  text-align: center;
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.menu-card:hover {
  transform: translateY(-5px);
}

.menu-icon {
  color: #409eff;
  margin-bottom: 16px;
}

.menu-card h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #303133;
}

.menu-card p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.my-videos {
  margin-top: 40px;
}

.my-videos h2 {
  margin: 0 0 20px;
  font-size: 20px;
  color: #303133;
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
</style>
