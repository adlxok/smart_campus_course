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
        
        <div v-else-if="activeMenu === 'analytics'" class="content-panel">
          <div class="panel-header">
            <h3>数据分析</h3>
          </div>
          <div class="analytics-section">
            <el-empty description="数据分析功能开发中..." />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  House, Upload, VideoPlay, DataAnalysis, View, Star, User, Plus 
} from '@element-plus/icons-vue'

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

const handleMenuSelect = (index: string) => {
  activeMenu.value = index
  if (index === 'videos') {
    loadMyVideos()
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
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
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
