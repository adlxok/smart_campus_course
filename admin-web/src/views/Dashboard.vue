<template>
  <div class="dashboard">
    <aside class="sidebar">
      <div class="logo">
        <h2>B站管理系统</h2>
      </div>
      <nav class="nav-menu">
        <div 
          class="nav-item" 
          :class="{ active: currentMenu === 'dashboard' }"
          @click="currentMenu = 'dashboard'"
        >
          <span class="icon">📊</span>
          <span>数据概览</span>
        </div>
        <div 
          class="nav-item" 
          :class="{ active: currentMenu === 'videos' }"
          @click="currentMenu = 'videos'"
        >
          <span class="icon">🎬</span>
          <span>视频管理</span>
        </div>
        <div 
          class="nav-item" 
          :class="{ active: currentMenu === 'categories' }"
          @click="currentMenu = 'categories'"
        >
          <span class="icon">📁</span>
          <span>分类管理</span>
        </div>
        <div 
          class="nav-item" 
          :class="{ active: currentMenu === 'users' }"
          @click="currentMenu = 'users'"
        >
          <span class="icon">👥</span>
          <span>用户管理</span>
        </div>
        <div 
          class="nav-item" 
          :class="{ active: currentMenu === 'settings' }"
          @click="currentMenu = 'settings'"
        >
          <span class="icon">⚙️</span>
          <span>系统设置</span>
        </div>
      </nav>
      <div class="user-section">
        <div class="user-avatar">{{ username.charAt(0).toUpperCase() }}</div>
        <div class="user-info">
          <span class="username">{{ username }}</span>
          <span class="role">管理员</span>
        </div>
        <button @click="logout" class="logout-btn">退出</button>
      </div>
    </aside>

    <main class="main-content">
      <header class="header">
        <h1>{{ menuTitle }}</h1>
      </header>

      <div class="content">
        <div v-if="currentMenu === 'dashboard'">
          <div class="stats-container">
            <div class="stat-card">
              <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">📹</div>
              <div class="stat-info">
                <h3>视频总数</h3>
                <p class="stat-number">{{ stats.videoCount || 0 }}</p>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">👁️</div>
              <div class="stat-info">
                <h3>总播放量</h3>
                <p class="stat-number">{{ formatNumber(stats.totalViews) }}</p>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">👍</div>
              <div class="stat-info">
                <h3>总点赞数</h3>
                <p class="stat-number">{{ formatNumber(stats.totalLikes) }}</p>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">📁</div>
              <div class="stat-info">
                <h3>分类数</h3>
                <p class="stat-number">{{ stats.totalCategories || 0 }}</p>
              </div>
            </div>
          </div>

          <div class="chart-section">
            <div class="chart-card">
              <h3>分类统计</h3>
              <div class="category-list">
                <div v-for="(item, index) in stats.categoryStats" :key="index" class="category-item">
                  <span class="category-name">{{ item.category || '未分类' }}</span>
                  <div class="category-bar">
                    <div class="category-progress" :style="{ width: getCategoryPercent(item.count) + '%' }"></div>
                  </div>
                  <span class="category-count">{{ item.count }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentMenu === 'videos'">
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
        </div>

        <div v-if="currentMenu === 'categories'">
          <div class="toolbar">
            <button @click="openCategoryModal()" class="add-btn">+ 新增分类</button>
          </div>

          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>分类名称</th>
                  <th>分类编码</th>
                  <th>排序</th>
                  <th>创建时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="cat in categories" :key="cat.id">
                  <td>{{ cat.id }}</td>
                  <td>{{ cat.name }}</td>
                  <td>{{ cat.code || '-' }}</td>
                  <td>{{ cat.sort || 0 }}</td>
                  <td>{{ formatDate(cat.createTime) }}</td>
                  <td>
                    <button @click="openCategoryModal(cat)" class="edit-btn">编辑</button>
                    <button @click="deleteCategory(cat.id)" class="delete-btn">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="pagination">
            <button @click="prevCategoryPage" :disabled="categoryPage === 1">上一页</button>
            <span>第 {{ categoryPage }} 页 / 共 {{ categoryTotalPages }} 页</span>
            <button @click="nextCategoryPage" :disabled="categoryPage >= categoryTotalPages">下一页</button>
          </div>
        </div>

        <div v-if="currentMenu === 'users'">
          <div class="empty-state">
            <span class="empty-icon">👥</span>
            <p>用户管理功能开发中...</p>
          </div>
        </div>

        <div v-if="currentMenu === 'settings'">
          <div class="empty-state">
            <span class="empty-icon">⚙️</span>
            <p>系统设置功能开发中...</p>
          </div>
        </div>
      </div>
    </main>

    <div v-if="showVideoModal" class="modal" @click.self="showVideoModal = false">
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
        <button @click="showVideoModal = false" class="close-btn">关闭</button>
      </div>
    </div>

    <div v-if="showCategoryModal" class="modal" @click.self="showCategoryModal = false">
      <div class="modal-content">
        <h2>{{ categoryForm.id ? '编辑分类' : '新增分类' }}</h2>
        <div class="form-group">
          <label>分类名称 <span class="required">*</span></label>
          <input type="text" v-model="categoryForm.name" placeholder="请输入分类名称" />
        </div>
        <div class="form-group">
          <label>分类编码</label>
          <input type="text" v-model="categoryForm.code" placeholder="请输入分类编码（可选）" />
        </div>
        <div class="form-group">
          <label>排序</label>
          <input type="number" v-model.number="categoryForm.sort" placeholder="数字越小越靠前" />
        </div>
        <div class="form-actions">
          <button @click="showCategoryModal = false" class="cancel-btn">取消</button>
          <button @click="saveCategory" class="save-btn">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '../utils/api'

export default {
  name: 'Dashboard',
  setup() {
    const router = useRouter()
    const username = ref('')
    const currentMenu = ref('dashboard')
    const videos = ref([])
    const stats = ref({})
    const page = ref(1)
    const pageSize = ref(20)
    const total = ref(0)
    const searchKeyword = ref('')
    const showVideoModal = ref(false)
    const selectedVideo = ref(null)

    const categories = ref([])
    const categoryPage = ref(1)
    const categoryPageSize = ref(10)
    const categoryTotal = ref(0)
    const showCategoryModal = ref(false)
    const categoryForm = ref({
      id: null,
      name: '',
      code: '',
      sort: 0
    })

    const menuTitle = computed(() => {
      const titles = {
        dashboard: '数据概览',
        videos: '视频管理',
        categories: '分类管理',
        users: '用户管理',
        settings: '系统设置'
      }
      return titles[currentMenu.value] || '控制台'
    })

    const totalPages = computed(() => Math.ceil(total.value / pageSize.value))
    const categoryTotalPages = computed(() => Math.ceil(categoryTotal.value / categoryPageSize.value))

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

    const getCategoryPercent = (count) => {
      if (!stats.value.videoCount) return 0
      return (count / stats.value.videoCount * 100).toFixed(1)
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

    const fetchCategories = async () => {
      try {
        const response = await api.get('/admin/category/list', {
          params: {
            page: categoryPage.value,
            pageSize: categoryPageSize.value
          }
        })
        if (response.data.success) {
          categories.value = response.data.data
          categoryTotal.value = response.data.total
        }
      } catch (error) {
        console.error('获取分类列表失败:', error)
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

    const prevCategoryPage = () => {
      if (categoryPage.value > 1) {
        categoryPage.value--
        fetchCategories()
      }
    }

    const nextCategoryPage = () => {
      if (categoryPage.value < categoryTotalPages.value) {
        categoryPage.value++
        fetchCategories()
      }
    }

    const viewVideo = async (video) => {
      try {
        const response = await api.get(`/admin/video/${video.bvid}`)
        if (response.data.success) {
          selectedVideo.value = response.data.data
          showVideoModal.value = true
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

    const openCategoryModal = (category = null) => {
      if (category) {
        categoryForm.value = {
          id: category.id,
          name: category.name,
          code: category.code || '',
          sort: category.sort || 0
        }
      } else {
        categoryForm.value = {
          id: null,
          name: '',
          code: '',
          sort: 0
        }
      }
      showCategoryModal.value = true
    }

    const saveCategory = async () => {
      if (!categoryForm.value.name.trim()) {
        alert('请输入分类名称')
        return
      }

      try {
        let response
        if (categoryForm.value.id) {
          response = await api.put('/admin/category/update', categoryForm.value)
        } else {
          response = await api.post('/admin/category/add', categoryForm.value)
        }

        if (response.data.success) {
          alert(response.data.message)
          showCategoryModal.value = false
          fetchCategories()
          fetchStats()
        } else {
          alert(response.data.message)
        }
      } catch (error) {
        console.error('保存分类失败:', error)
        alert('保存失败')
      }
    }

    const deleteCategory = async (id) => {
      if (!confirm('确定要删除这个分类吗？')) return

      try {
        const response = await api.delete(`/admin/category/delete/${id}`)
        if (response.data.success) {
          alert('删除成功')
          fetchCategories()
          fetchStats()
        } else {
          alert(response.data.message)
        }
      } catch (error) {
        console.error('删除分类失败:', error)
      }
    }

    const logout = () => {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    }

    watch(currentMenu, (newMenu) => {
      if (newMenu === 'categories') {
        fetchCategories()
      }
    })

    onMounted(() => {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      username.value = user.username || 'Admin'
      fetchVideos()
      fetchStats()
    })

    return {
      username,
      currentMenu,
      menuTitle,
      videos,
      stats,
      page,
      totalPages,
      searchKeyword,
      showVideoModal,
      selectedVideo,
      categories,
      categoryPage,
      categoryTotalPages,
      showCategoryModal,
      categoryForm,
      formatNumber,
      formatDate,
      getCategoryPercent,
      fetchVideos,
      searchVideos,
      prevPage,
      nextPage,
      prevCategoryPage,
      nextCategoryPage,
      viewVideo,
      deleteVideo,
      openCategoryModal,
      saveCategory,
      deleteCategory,
      logout
    }
  }
}
</script>

<style scoped>
.dashboard {
  display: flex;
  min-height: 100vh;
  background: #f0f2f5;
}

.sidebar {
  width: 240px;
  background: #001529;
  color: white;
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100vh;
  left: 0;
  top: 0;
}

.logo {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo h2 {
  margin: 0;
  font-size: 18px;
  color: #fff;
}

.nav-menu {
  flex: 1;
  padding: 10px 0;
}

.nav-item {
  padding: 12px 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(255, 255, 255, 0.65);
  transition: all 0.3s;
}

.nav-item:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.nav-item.active {
  color: #fff;
  background: #1890ff;
}

.nav-item .icon {
  font-size: 16px;
}

.user-section {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 14px;
  color: #fff;
}

.role {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
}

.logout-btn {
  padding: 5px 10px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: rgba(255, 255, 255, 0.65);
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.logout-btn:hover {
  color: #fff;
  border-color: #fff;
}

.main-content {
  flex: 1;
  margin-left: 240px;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  padding: 16px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header h1 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.content {
  padding: 24px;
  flex: 1;
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-info h3 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #666;
  font-weight: normal;
}

.stat-number {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.chart-section {
  margin-bottom: 24px;
}

.chart-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.chart-card h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #333;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.category-name {
  width: 80px;
  font-size: 14px;
  color: #666;
}

.category-bar {
  flex: 1;
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.category-progress {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
}

.category-count {
  width: 50px;
  text-align: right;
  font-size: 14px;
  color: #333;
}

.toolbar {
  margin-bottom: 16px;
}

.add-btn {
  padding: 10px 20px;
  background: #52c41a;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.add-btn:hover {
  background: #73d13d;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.search-bar input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
}

.search-bar input:focus {
  outline: none;
  border-color: #1890ff;
}

.search-bar button {
  padding: 10px 20px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.search-bar button:hover {
  background: #40a9ff;
}

.table-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

th {
  background: #fafafa;
  font-weight: 500;
  color: #333;
}

.title-cell {
  max-width: 250px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.view-btn, .edit-btn, .delete-btn {
  padding: 4px 8px;
  margin-right: 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.view-btn {
  background: #1890ff;
  color: white;
}

.edit-btn {
  background: #faad14;
  color: white;
}

.delete-btn {
  background: #ff4d4f;
  color: white;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 16px;
}

.pagination button {
  padding: 8px 16px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.pagination button:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: #fff;
  border-radius: 8px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  color: #999;
  font-size: 14px;
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
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 24px;
  border-radius: 8px;
  width: 500px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content h2 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 18px;
}

.video-detail p {
  margin: 8px 0;
  font-size: 14px;
}

.video-detail a {
  color: #1890ff;
}

.close-btn {
  margin-top: 16px;
  padding: 10px 20px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  width: 100%;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
}

.form-group label .required {
  color: #ff4d4f;
}

.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #1890ff;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.cancel-btn {
  flex: 1;
  padding: 10px 20px;
  background: #f0f0f0;
  color: #666;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.save-btn {
  flex: 1;
  padding: 10px 20px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
</style>
