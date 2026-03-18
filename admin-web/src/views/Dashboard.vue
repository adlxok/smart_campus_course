<template>
  <div class="dashboard">
    <header class="header">
      <h1>B站视频管理系统</h1>
      <div class="user-info">
        <span>欢迎, {{ username }}</span>
        <button @click="logout" class="logout-btn">退出登录</button>
      </div>
    </header>
    
    <main class="main">
      <div class="stats-container">
        <div class="stat-card">
          <h3>视频总数</h3>
          <p class="stat-number">{{ stats.videoCount || 0 }}</p>
        </div>
        <div class="stat-card">
          <h3>总播放量</h3>
          <p class="stat-number">{{ formatNumber(stats.totalViews) }}</p>
        </div>
        <div class="stat-card">
          <h3>总点赞数</h3>
          <p class="stat-number">{{ formatNumber(stats.totalLikes) }}</p>
        </div>
        <div class="stat-card">
          <h3>分类数</h3>
          <p class="stat-number">{{ stats.totalCategories || 0 }}</p>
        </div>
      </div>

      <div class="search-bar">
        <input 
          type="text" 
          v-model="searchKeyword" 
          placeholder="搜索视频标题或BV号"
          @keyup.enter="searchVideos"
        />
        <button @click="searchVideos">搜索</button>
      </div>

      <div class="table-container">
        <table>
          <thead>
            <tr>
              <th>BV号</th>
              <th>标题</th>
              <th>分类</th>
              <th>播放量</th>
              <th>点赞</th>
              <th>收藏</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="video in videos" :key="video.id">
              <td>{{ video.bvid }}</td>
              <td class="title-cell">{{ video.title }}</td>
              <td>{{ video.category }}</td>
              <td>{{ formatNumber(video.viewCount) }}</td>
              <td>{{ formatNumber(video.likeCount) }}</td>
              <td>{{ formatNumber(video.favoriteCount) }}</td>
              <td>{{ formatDate(video.createTime) }}</td>
              <td>
                <button @click="viewVideo(video)" class="view-btn">查看</button>
                <button @click="deleteVideo(video.bvid)" class="delete-btn">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination">
        <button @click="prevPage" :disabled="page === 1">上一页</button>
        <span>第 {{ page }} 页 / 共 {{ totalPages }} 页</span>
        <button @click="nextPage" :disabled="page >= totalPages">下一页</button>
      </div>
    </main>

    <div v-if="showModal" class="modal" @click.self="showModal = false">
      <div class="modal-content">
        <h2>视频详情</h2>
        <div class="video-detail" v-if="selectedVideo">
          <p><strong>BV号:</strong> {{ selectedVideo.bvid }}</p>
          <p><strong>标题:</strong> {{ selectedVideo.title }}</p>
          <p><strong>分类:</strong> {{ selectedVideo.category }}</p>
          <p><strong>标签:</strong> {{ selectedVideo.tags }}</p>
          <p><strong>播放量:</strong> {{ formatNumber(selectedVideo.viewCount) }}</p>
          <p><strong>弹幕:</strong> {{ formatNumber(selectedVideo.danmakuCount) }}</p>
          <p><strong>点赞:</strong> {{ formatNumber(selectedVideo.likeCount) }}</p>
          <p><strong>投币:</strong> {{ formatNumber(selectedVideo.coinCount) }}</p>
          <p><strong>收藏:</strong> {{ formatNumber(selectedVideo.favoriteCount) }}</p>
          <p><strong>分享:</strong> {{ formatNumber(selectedVideo.shareCount) }}</p>
          <p><strong>评论:</strong> {{ formatNumber(selectedVideo.replyCount) }}</p>
          <p><strong>封面:</strong> <a :href="selectedVideo.cover" target="_blank">查看封面</a></p>
          <p><strong>创建时间:</strong> {{ formatDate(selectedVideo.createTime) }}</p>
        </div>
        <button @click="showModal = false" class="close-btn">关闭</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '../utils/api'

export default {
  name: 'Dashboard',
  setup() {
    const router = useRouter()
    const username = ref('')
    const videos = ref([])
    const stats = ref({})
    const page = ref(1)
    const pageSize = ref(20)
    const total = ref(0)
    const searchKeyword = ref('')
    const showModal = ref(false)
    const selectedVideo = ref(null)

    const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

    const formatNumber = (num) => {
      if (!num) return '0'
      if (num >= 10000) {
        return (num / 10000).toFixed(1) + '万'
      }
      return num.toLocaleString()
    }

    const formatDate = (dateStr) => {
      if (!dateStr) return '-'
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN')
    }

    const fetchVideos = async () => {
      try {
        const response = await api.get('/admin/videos', {
          params: {
            page: page.value,
            pageSize: pageSize.value,
            keyword: searchKeyword.value
          }
        })
        if (response.data.success) {
          videos.value = response.data.data
          total.value = response.data.total
        }
      } catch (error) {
        console.error('获取视频列表失败:', error)
      }
    }

    const fetchStats = async () => {
      try {
        const response = await api.get('/admin/statistics')
        if (response.data.success) {
          stats.value = response.data.data
        }
      } catch (error) {
        console.error('获取统计数据失败:', error)
      }
    }

    const searchVideos = () => {
      page.value = 1
      fetchVideos()
    }

    const prevPage = () => {
      if (page.value > 1) {
        page.value--
        fetchVideos()
      }
    }

    const nextPage = () => {
      if (page.value < totalPages.value) {
        page.value++
        fetchVideos()
      }
    }

    const viewVideo = async (video) => {
      try {
        const response = await api.get(`/admin/video/${video.bvid}`)
        if (response.data.success) {
          selectedVideo.value = response.data.data
          showModal.value = true
        }
      } catch (error) {
        console.error('获取视频详情失败:', error)
      }
    }

    const deleteVideo = async (bvid) => {
      if (!confirm('确定要删除这个视频吗？')) return
      
      try {
        const response = await api.delete(`/admin/video/${bvid}`)
        if (response.data.success) {
          alert('删除成功')
          fetchVideos()
          fetchStats()
        } else {
          alert(response.data.message)
        }
      } catch (error) {
        console.error('删除视频失败:', error)
      }
    }

    const logout = () => {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    }

    onMounted(() => {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      username.value = user.username || 'Admin'
      fetchVideos()
      fetchStats()
    })

    return {
      username,
      videos,
      stats,
      page,
      totalPages,
      searchKeyword,
      showModal,
      selectedVideo,
      formatNumber,
      formatDate,
      fetchVideos,
      searchVideos,
      prevPage,
      nextPage,
      viewVideo,
      deleteVideo,
      logout
    }
  }
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header h1 {
  margin: 0;
  font-size: 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.logout-btn {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  border-radius: 5px;
  cursor: pointer;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.main {
  padding: 30px;
  max-width: 1400px;
  margin: 0 auto;
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 25px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  text-align: center;
}

.stat-card h3 {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
}

.stat-number {
  margin: 0;
  font-size: 28px;
  font-weight: bold;
  color: #667eea;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-bar input {
  flex: 1;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
}

.search-bar button {
  padding: 12px 25px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.table-container {
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.title-cell {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.view-btn, .delete-btn {
  padding: 5px 10px;
  margin-right: 5px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
}

.view-btn {
  background: #3498db;
  color: white;
}

.delete-btn {
  background: #e74c3c;
  color: white;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
}

.pagination button {
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.pagination button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 10px;
  width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content h2 {
  margin-top: 0;
  margin-bottom: 20px;
}

.video-detail p {
  margin: 10px 0;
  line-height: 1.6;
}

.video-detail a {
  color: #667eea;
}

.close-btn {
  margin-top: 20px;
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  width: 100%;
}
</style>
