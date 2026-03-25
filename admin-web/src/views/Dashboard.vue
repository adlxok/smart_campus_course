<template>
  <div class="dashboard">
    <aside class="sidebar">
      <div class="logo">
        <h2>后台管理系统</h2>
      </div>
      <nav class="nav-menu">
        <div class="menu-group" v-if="canAccessSystem">
          <div class="menu-group-title" @click="toggleGroup('system')">
            <span class="group-icon">⚙️</span>
            <span>系统管理</span>
            <span class="arrow" :class="{ expanded: expandedGroups.system }">▼</span>
          </div>
          <div class="menu-group-items" v-show="expandedGroups.system">
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
              :class="{ active: currentMenu === 'notifications' }"
              @click="currentMenu = 'notifications'"
            >
              <span class="icon">🔔</span>
              <span>系统通知管理</span>
            </div>
            <div 
              class="nav-item" 
              :class="{ active: currentMenu === 'banners' }"
              @click="currentMenu = 'banners'"
            >
              <span class="icon">🖼️</span>
              <span>轮播图管理</span>
            </div>
          </div>
        </div>

        <div class="menu-group" v-if="canAccessData">
          <div class="menu-group-title" @click="toggleGroup('crawler')">
            <span class="group-icon">🕷️</span>
            <span>数据爬取和分析</span>
            <span class="arrow" :class="{ expanded: expandedGroups.crawler }">▼</span>
          </div>
          <div class="menu-group-items" v-show="expandedGroups.crawler">
            <div 
              class="nav-item" 
              :class="{ active: currentMenu === 'videos' }"
              @click="currentMenu = 'videos'"
            >
              <span class="icon">🎬</span>
              <span>B站视频管理</span>
            </div>
            <div 
              class="nav-item" 
              :class="{ active: currentMenu === 'crawler' }"
              @click="currentMenu = 'crawler'"
            >
              <span class="icon">🕷️</span>
              <span>视频爬取</span>
            </div>
            <div 
              class="nav-item" 
              :class="{ active: currentMenu === 'import' }"
              @click="currentMenu = 'import'"
            >
              <span class="icon">📥</span>
              <span>视频导入</span>
            </div>
            <div 
              class="nav-item" 
              :class="{ active: currentMenu === 'proxy' }"
              @click="currentMenu = 'proxy'"
            >
              <span class="icon">🌐</span>
              <span>代理管理</span>
            </div>
            <div 
              class="nav-item" 
              :class="{ active: currentMenu === 'analysis' }"
              @click="goToAnalysis"
            >
              <span class="icon">📊</span>
              <span>B站视频数据分析</span>
            </div>
          </div>
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
              <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">�</div>
              <div class="stat-info">
                <h3>用户总数</h3>
                <p class="stat-number">{{ stats.userCount || 0 }}</p>
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

        <div v-if="currentMenu === 'crawler'" class="crawler-section">
          <el-card class="crawler-card">
            <template #header>
              <div class="card-header">
                <span>🕷️ B站视频爬取</span>
              </div>
            </template>
            
            <el-form :model="crawlerForm" label-width="100px">
              <el-form-item label="选择分类">
                <el-select 
                  v-model="crawlerForm.categoryCode" 
                  placeholder="选择分类(自动填充URL)"
                  clearable
                  style="width: 200px"
                  @change="onCategoryChange"
                >
                  <el-option
                    v-for="cat in categories"
                    :key="cat.id"
                    :label="cat.name"
                    :value="cat.code"
                  />
                </el-select>
                <span class="form-tip">选择分类后自动填充URL</span>
              </el-form-item>
              
              <el-form-item label="目标URL">
                <el-input 
                  v-model="crawlerForm.url" 
                  placeholder="请输入B站页面URL，如: www.bilibili.com/c/music/"
                  clearable
                >
                  <template #prepend>https://</template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="爬取数量">
                <el-input-number 
                  v-model="crawlerForm.maxVideos" 
                  :min="1" 
                  :max="1000" 
                  :step="10"
                />
                <span class="form-tip">最大支持1000条</span>
              </el-form-item>

              <el-form-item>
                <el-button 
                  type="primary" 
                  @click="startCrawler"
                  :loading="crawlerLoading"
                  :disabled="!crawlerForm.url"
                >
                  {{ crawlerLoading ? '爬取中...' : '开始爬取' }}
                </el-button>
                <el-button 
                  v-if="crawlerLoading" 
                  type="danger" 
                  @click="stopCrawler"
                >
                  停止爬取
                </el-button>
                <el-button 
                  type="warning" 
                  @click="cleanTags"
                  :loading="cleaningTags"
                >
                  清洗标签
                </el-button>
              </el-form-item>
            </el-form>

            <el-divider />

            <div v-if="crawlerTask" class="crawler-status">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="任务ID">{{ crawlerTask.taskId }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="getTaskStatusType(crawlerTask.status)">
                    {{ getTaskStatusText(crawlerTask.status) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="目标URL" :span="2">{{ crawlerTask.url }}</el-descriptions-item>
                <el-descriptions-item label="当前进度" :span="2">{{ crawlerTask.progress }}</el-descriptions-item>
              </el-descriptions>
            </div>

            <div v-if="crawlerLogs" class="crawler-logs">
              <div class="logs-header">
                <span>执行日志</span>
                <el-button size="small" @click="clearLogs">清空日志</el-button>
              </div>
              <el-input
                ref="logsTextarea"
                type="textarea"
                v-model="crawlerLogs"
                :rows="15"
                readonly
                class="logs-textarea"
              />
            </div>
          </el-card>

          <el-card class="crawler-tips">
            <template #header>
              <span>📝 使用说明</span>
            </template>
            <ul class="tips-list">
              <li>支持爬取B站任意分区页面，如音乐区、动画区等</li>
              <li>爬虫会自动滚动页面获取视频链接</li>
              <li>数据会自动保存到数据库中</li>
              <li>爬取过程中请勿关闭浏览器窗口</li>
              <li>建议每次爬取数量不超过500条</li>
            </ul>
          </el-card>
        </div>

        <div v-if="currentMenu === 'import'" class="import-section">
          <el-card class="import-card">
            <template #header>
              <div class="card-header">
                <span>📥 视频导入</span>
              </div>
            </template>
            
            <div class="import-filters">
              <div class="filter-row">
                <span class="filter-label">分类筛选:</span>
                <el-select v-model="importCategory" placeholder="选择分类(可选)" clearable style="width: 200px">
                  <el-option
                    v-for="cat in categories"
                    :key="cat.id"
                    :label="cat.name"
                    :value="cat.name"
                  />
                </el-select>
                
                <span class="filter-label" style="margin-left: 20px">导入数量:</span>
                <el-input-number 
                  v-model="importLimit" 
                  :min="1" 
                  :max="1000" 
                  :step="10"
                  style="width: 150px"
                />
                <span class="filter-tip">条（按时间倒序，已排除重复）</span>
                
                <el-checkbox v-model="useProxy" style="margin-left: 20px">
                  使用代理IP
                </el-checkbox>
              </div>
              
              <div class="filter-row" style="margin-top: 12px">
                <el-button type="success" @click="startImportWithFilters" :loading="importLoading">
                  {{ importLoading ? '导入中...' : '开始导入' }}
                </el-button>
                <el-button type="primary" @click="fetchBilibiliVideos">刷新列表</el-button>
              </div>
            </div>

            <el-divider />

            <div class="import-toolbar">
              <el-input
                v-model="importSearchKeyword"
                placeholder="搜索视频标题或BV号"
                style="width: 300px"
                @keyup.enter="searchBilibiliVideos"
                clearable
              >
                <template #prefix>
                  <el-icon><search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="searchBilibiliVideos">搜索</el-button>
              <el-button type="warning" @click="startImportSelected" :disabled="selectedBilibiliVideos.length === 0">
                导入选中 ({{ selectedBilibiliVideos.length }})
              </el-button>
            </div>

            <el-table
              :data="bilibiliVideos"
              style="width: 100%; margin-top: 16px"
              @selection-change="handleBilibiliSelectionChange"
              v-loading="bilibiliLoading"
            >
              <el-table-column type="selection" width="55" />
              <el-table-column prop="bvid" label="BV号" width="140" />
              <el-table-column prop="title" label="标题" min-width="250">
                <template #default="{ row }">
                  <span class="title-cell">{{ row.title }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="category" label="分类" width="100" />
              <el-table-column prop="viewCount" label="播放量" width="100">
                <template #default="{ row }">
                  {{ formatNumber(row.viewCount) }}
                </template>
              </el-table-column>
              <el-table-column prop="likeCount" label="点赞" width="80">
                <template #default="{ row }">
                  {{ formatNumber(row.likeCount) }}
                </template>
              </el-table-column>
              <el-table-column label="封面" width="80">
                <template #default="{ row }">
                  <el-image
                    v-if="row.cover"
                    :src="formatImageUrl(row.cover)"
                    :preview-src-list="[formatImageUrl(row.cover)]"
                    style="width: 60px; height: 40px"
                    fit="cover"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button size="small" type="primary" @click="importSingleVideo(row)">
                    导入
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination">
              <el-pagination
                v-model:current-page="bilibiliPage"
                v-model:page-size="bilibiliPageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="bilibiliTotal"
                layout="total, sizes, prev, pager, next"
                @size-change="fetchBilibiliVideos"
                @current-change="fetchBilibiliVideos"
              />
            </div>
          </el-card>

          <el-card v-if="importTask" class="import-status-card">
            <template #header>
              <div class="card-header">
                <span>📋 导入任务状态</span>
              </div>
            </template>
            
            <el-descriptions :column="3" border>
              <el-descriptions-item label="任务ID">{{ importTask.taskId }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getTaskStatusType(importTask.status)">
                  {{ getTaskStatusText(importTask.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="进度">{{ importTask.progress }}</el-descriptions-item>
              <el-descriptions-item label="总数">{{ importTask.totalCount || 0 }}</el-descriptions-item>
              <el-descriptions-item label="成功">
                <span style="color: #67c23a">{{ importTask.successCount || 0 }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="跳过">
                <span style="color: #e6a23c">{{ importTask.skipCount || 0 }}</span>
              </el-descriptions-item>
            </el-descriptions>

            <div v-if="importLogs" class="import-logs">
              <div class="logs-header">
                <span>执行日志</span>
                <el-button size="small" @click="clearImportLogs">清空日志</el-button>
              </div>
              <el-input
                type="textarea"
                v-model="importLogs"
                :rows="12"
                readonly
                class="logs-textarea"
              />
            </div>
          </el-card>
        </div>

        <div v-if="currentMenu === 'users'">
          <div class="empty-state">
            <span class="empty-icon">👥</span>
            <p>用户管理功能开发中...</p>
          </div>
        </div>

        <div v-if="currentMenu === 'banners'" class="banner-section">
          <el-card class="banner-card">
            <template #header>
              <div class="card-header">
                <span>🖼️ 轮播图管理</span>
                <el-button type="primary" size="small" @click="openBannerModal()">添加轮播图</el-button>
              </div>
            </template>
            
            <el-table :data="bannerList" style="width: 100%" v-loading="bannerLoading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column label="图片" width="150">
                <template #default="{ row }">
                  <el-image 
                    :src="row.imageUrl" 
                    :preview-src-list="[row.imageUrl]"
                    style="width: 120px; height: 60px"
                    fit="cover"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="title" label="标题" min-width="150" />
              <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
              <el-table-column prop="sortOrder" label="排序" width="80" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button size="small" type="primary" @click="editBanner(row)">编辑</el-button>
                  <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleBannerStatus(row)">
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button size="small" type="danger" @click="deleteBanner(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination">
              <el-pagination
                v-model:current-page="bannerPage"
                v-model:page-size="bannerPageSize"
                :page-sizes="[10, 20, 50]"
                :total="bannerTotal"
                layout="total, sizes, prev, pager, next"
                @size-change="fetchBannerList"
                @current-change="fetchBannerList"
              />
            </div>
          </el-card>

          <el-dialog v-model="showBannerModal" :title="bannerForm.id ? '编辑轮播图' : '添加轮播图'" width="600px">
            <el-form :model="bannerForm" label-width="80px">
              <el-form-item label="标题" required>
                <el-input v-model="bannerForm.title" placeholder="请输入标题" />
              </el-form-item>
              <el-form-item label="描述">
                <el-input v-model="bannerForm.description" type="textarea" :rows="2" placeholder="请输入描述" />
              </el-form-item>
              <el-form-item label="图片" required>
                <el-upload
                  class="banner-uploader"
                  :action="uploadUrl"
                  :show-file-list="false"
                  :on-success="handleBannerUploadSuccess"
                  :before-upload="beforeBannerUpload"
                  accept="image/*"
                >
                  <el-image 
                    v-if="bannerForm.imageUrl" 
                    :src="bannerForm.imageUrl" 
                    style="width: 100%; max-height: 200px"
                    fit="contain"
                  />
                  <el-icon v-else class="banner-uploader-icon"><Plus /></el-icon>
                </el-upload>
              </el-form-item>
              <el-form-item label="跳转链接">
                <el-input v-model="bannerForm.linkUrl" placeholder="请输入跳转链接（可选）" />
              </el-form-item>
              <el-form-item label="排序">
                <el-input-number v-model="bannerForm.sortOrder" :min="0" :max="999" />
              </el-form-item>
              <el-form-item label="状态">
                <el-switch v-model="bannerForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="showBannerModal = false">取消</el-button>
              <el-button type="primary" @click="saveBanner">保存</el-button>
            </template>
          </el-dialog>
        </div>

        <div v-if="currentMenu === 'proxy'" class="proxy-section">
          <el-card class="proxy-card">
            <template #header>
              <div class="card-header">
                <span>🌐 代理管理</span>
                <el-button type="primary" size="small" @click="openProxyModal()">添加代理</el-button>
              </div>
            </template>
            
            <div class="proxy-toolbar">
              <el-button type="success" @click="fetchProxyList" :loading="proxyLoading">刷新列表</el-button>
              <el-button type="warning" @click="reloadProxyCache">重载缓存</el-button>
            </div>

            <el-table :data="proxyList" style="width: 100%; margin-top: 16px" v-loading="proxyLoading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="protocol" label="协议" width="100" />
              <el-table-column prop="host" label="主机" min-width="150" />
              <el-table-column prop="port" label="端口" width="80" />
              <el-table-column prop="username" label="用户名" width="100">
                <template #default="{ row }">
                  {{ row.username || '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="successCount" label="成功次数" width="90" />
              <el-table-column prop="failCount" label="失败次数" width="90" />
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button size="small" type="primary" @click="editProxy(row)">编辑</el-button>
                  <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleProxyStatus(row)">
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button size="small" type="danger" @click="deleteProxy(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>

          <el-dialog v-model="showProxyModal" :title="proxyForm.id ? '编辑代理' : '添加代理'" width="500px">
            <el-form :model="proxyForm" label-width="80px">
              <el-form-item label="协议">
                <el-select v-model="proxyForm.protocol" style="width: 100%">
                  <el-option label="HTTP" value="http" />
                  <el-option label="HTTPS" value="https" />
                  <el-option label="SOCKS4" value="socks4" />
                  <el-option label="SOCKS5" value="socks5" />
                </el-select>
              </el-form-item>
              <el-form-item label="主机">
                <el-input v-model="proxyForm.host" placeholder="IP地址或域名" />
              </el-form-item>
              <el-form-item label="端口">
                <el-input-number v-model="proxyForm.port" :min="1" :max="65535" style="width: 100%" />
              </el-form-item>
              <el-form-item label="用户名">
                <el-input v-model="proxyForm.username" placeholder="可选" />
              </el-form-item>
              <el-form-item label="密码">
                <el-input v-model="proxyForm.password" type="password" placeholder="可选" show-password />
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="showProxyModal = false">取消</el-button>
              <el-button type="primary" @click="saveProxy">保存</el-button>
            </template>
          </el-dialog>
        </div>

        <div v-if="currentMenu === 'notifications'" class="notification-section">
          <el-card class="notification-card">
            <template #header>
              <div class="card-header">
                <span>🔔 系统通知管理</span>
                <el-button type="primary" size="small" @click="openNotificationModal()">发布通知</el-button>
              </div>
            </template>
            
            <el-table :data="notificationList" style="width: 100%" v-loading="notificationLoading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="标题" min-width="200" />
              <el-table-column prop="type" label="类型" width="100">
                <template #default="{ row }">
                  <el-tag :type="getNotificationTypeStyle(row.type)">{{ getNotificationTypeText(row.type) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="280">
                <template #default="{ row }">
                  <el-button size="small" type="primary" @click="editNotification(row)">编辑</el-button>
                  <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleNotificationStatus(row)">
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button size="small" type="info" @click="sendNotificationToAll(row)">发送给所有人</el-button>
                  <el-button size="small" type="danger" @click="deleteNotification(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination">
              <el-pagination
                v-model:current-page="notificationPage"
                v-model:page-size="notificationPageSize"
                :page-sizes="[10, 20, 50]"
                :total="notificationTotal"
                layout="total, sizes, prev, pager, next"
                @size-change="fetchNotificationList"
                @current-change="fetchNotificationList"
              />
            </div>
          </el-card>

          <el-dialog v-model="showNotificationModal" :title="notificationForm.id ? '编辑通知' : '发布通知'" width="600px">
            <el-form :model="notificationForm" label-width="80px">
              <el-form-item label="标题" required>
                <el-input v-model="notificationForm.title" placeholder="请输入通知标题" />
              </el-form-item>
              <el-form-item label="类型">
                <el-select v-model="notificationForm.type" style="width: 100%">
                  <el-option label="普通" value="normal" />
                  <el-option label="重要" value="important" />
                  <el-option label="紧急" value="urgent" />
                </el-select>
              </el-form-item>
              <el-form-item label="内容" required>
                <el-input v-model="notificationForm.content" type="textarea" :rows="5" placeholder="请输入通知内容" />
              </el-form-item>
              <el-form-item label="状态">
                <el-switch v-model="notificationForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
              </el-form-item>
              <el-form-item label="发送对象" v-if="!notificationForm.id">
                <el-radio-group v-model="notificationForm.sendType">
                  <el-radio value="all">所有用户</el-radio>
                  <el-radio value="selected">指定用户</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="选择用户" v-if="notificationForm.sendType === 'selected' && !notificationForm.id">
                <el-select v-model="notificationForm.selectedUsers" multiple filterable placeholder="选择用户" style="width: 100%">
                  <el-option
                    v-for="user in allUsers"
                    :key="user.id"
                    :label="user.username"
                    :value="user.id"
                  />
                </el-select>
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="showNotificationModal = false">取消</el-button>
              <el-button type="primary" @click="saveNotification">保存</el-button>
            </template>
          </el-dialog>
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
import { ref, onMounted, computed, watch, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import api from '../utils/api'

export default {
  name: 'Dashboard',
  setup() {
    const router = useRouter()
    const username = ref('')
    const currentUserId = ref(null)
    const currentMenu = ref('dashboard')
    const userRole = ref('')
    const userPermissions = ref([])
    const expandedGroups = ref({
      system: false,
      crawler: false
    })
    
    const hasPermission = (permission) => {
      if (userRole.value === 'SUPER_ADMIN') {
        return true
      }
      return userPermissions.value.includes(permission)
    }
    
    const canAccessSystem = computed(() => {
      return hasPermission('system:manage') || userRole.value === 'SUPER_ADMIN' || userRole.value === 'SYSTEM_ADMIN'
    })
    
    const canAccessData = computed(() => {
      return hasPermission('data:manage') || userRole.value === 'SUPER_ADMIN' || userRole.value === 'DATA_ADMIN'
    })
    
    const videos = ref([])

    const toggleGroup = (group) => {
      expandedGroups.value[group] = !expandedGroups.value[group]
    }
    const stats = ref({})
    const page = ref(1)
    const pageSize = ref(20)
    const total = ref(0)
    const searchKeyword = ref('')
    const showVideoModal = ref(false)
    const selectedVideo = ref(null)
    const logsTextarea = ref(null)

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

    const crawlerForm = ref({
      url: 'www.bilibili.com/c/music/',
      maxVideos: 100,
      categoryCode: 'music'
    })
    const crawlerLoading = ref(false)
    const crawlerTask = ref(null)
    const crawlerLogs = ref('')
    let statusCheckInterval = null
    let lastLogIndex = 0

    const bilibiliVideos = ref([])
    const bilibiliPage = ref(1)
    const bilibiliPageSize = ref(20)
    const bilibiliTotal = ref(0)
    const bilibiliLoading = ref(false)
    const importSearchKeyword = ref('')
    const selectedBilibiliVideos = ref([])
    const importLoading = ref(false)
    const importTask = ref(null)
    const importLogs = ref('')
    const importCategory = ref(null)
    const importLimit = ref(10)
    const useProxy = ref(false)
    let importStatusCheckInterval = null
    let importLastLogIndex = 0
    const cleaningTags = ref(false)

    const proxyList = ref([])
    const proxyLoading = ref(false)
    const showProxyModal = ref(false)
    const proxyForm = ref({
      id: null,
      protocol: 'http',
      host: '',
      port: 80,
      username: '',
      password: ''
    })

    const notificationList = ref([])
    const notificationLoading = ref(false)
    const notificationPage = ref(1)
    const notificationPageSize = ref(10)
    const notificationTotal = ref(0)
    const showNotificationModal = ref(false)
    const notificationForm = ref({
      id: null,
      title: '',
      content: '',
      type: 'normal',
      status: 1,
      sendType: 'all',
      selectedUsers: []
    })
    const allUsers = ref([])

    const bannerList = ref([])
    const bannerLoading = ref(false)
    const bannerPage = ref(1)
    const bannerPageSize = ref(10)
    const bannerTotal = ref(0)
    const showBannerModal = ref(false)
    const bannerForm = ref({
      id: null,
      title: '',
      description: '',
      imageUrl: '',
      linkUrl: '',
      sortOrder: 0,
      status: 1
    })
    const uploadUrl = 'http://localhost:8080/api/banner/admin/upload'

    const menuTitle = computed(() => {
      const titles = {
        dashboard: '数据概览',
        videos: '视频管理',
        categories: '分类管理',
        crawler: '视频爬取',
        import: '视频导入',
        proxy: '代理管理',
        users: '用户管理',
        notifications: '系统通知管理',
        banners: '轮播图管理'
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

    const formatImageUrl = (url) => {
      if (!url) return ''
      if (url.startsWith('hdfs://')) {
        return `/api/image/proxy?url=${encodeURIComponent(url)}`
      }
      if (url.startsWith('/covers/') || url.startsWith('/videos/')) {
        return `/api/image/proxy?url=${encodeURIComponent(url)}`
      }
      return url
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

    const getTaskStatusType = (status) => {
      const types = {
        pending: 'info',
        running: 'warning',
        completed: 'success',
        failed: 'danger'
      }
      return types[status] || 'info'
    }

    const getTaskStatusText = (status) => {
      const texts = {
        pending: '等待中',
        running: '运行中',
        completed: '已完成',
        failed: '失败'
      }
      return texts[status] || status
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

    const onCategoryChange = (code) => {
      if (code) {
        crawlerForm.value.url = `www.bilibili.com/c/${code}/`
      }
    }

    const startCrawler = async () => {
      if (!crawlerForm.value.url) {
        ElMessage.warning('请输入目标URL')
        return
      }

      let url = crawlerForm.value.url
      if (!url.startsWith('http://') && !url.startsWith('https://')) {
        url = 'https://' + url
      }

      crawlerLoading.value = true
      crawlerLogs.value = ''
      lastLogIndex = 0

      try {
        const response = await api.post('/admin/crawler/start', {
          url: url,
          maxVideos: crawlerForm.value.maxVideos,
          categoryCode: crawlerForm.value.categoryCode
        })

        if (response.data.success) {
          crawlerTask.value = {
            taskId: response.data.taskId,
            url: url,
            status: 'pending',
            progress: '正在启动...'
          }
          
          ElMessage.success('爬虫任务已启动')
          
          statusCheckInterval = setInterval(checkCrawlerStatus, 1000)
        } else {
          ElMessage.error(response.data.message)
          crawlerLoading.value = false
        }
      } catch (error) {
        console.error('启动爬虫失败:', error)
        ElMessage.error('启动爬虫失败')
        crawlerLoading.value = false
      }
    }

    const checkCrawlerStatus = async () => {
      if (!crawlerTask.value) return

      try {
        const [statusResponse, logsResponse] = await Promise.all([
          api.get(`/admin/crawler/status/${crawlerTask.value.taskId}`),
          api.get(`/admin/crawler/logs/${crawlerTask.value.taskId}?since=${lastLogIndex}`)
        ])

        if (statusResponse.data.success) {
          crawlerTask.value.status = statusResponse.data.status
          crawlerTask.value.progress = statusResponse.data.progress

          if (statusResponse.data.status === 'completed') {
            clearInterval(statusCheckInterval)
            crawlerLoading.value = false
            ElNotification({
              title: '爬取完成',
              message: '视频数据爬取已完成',
              type: 'success'
            })
            fetchStats()
            fetchVideos()
          } else if (statusResponse.data.status === 'failed') {
            clearInterval(statusCheckInterval)
            crawlerLoading.value = false
            ElNotification({
              title: '爬取失败',
              message: statusResponse.data.output || '爬取过程中发生错误',
              type: 'error'
            })
          }
        }

        if (logsResponse.data.success && logsResponse.data.logs && logsResponse.data.logs.length > 0) {
          const newLogs = logsResponse.data.logs.join('\n')
          if (crawlerLogs.value) {
            crawlerLogs.value += '\n' + newLogs
          } else {
            crawlerLogs.value = newLogs
          }
          lastLogIndex = logsResponse.data.nextIndex
          
          nextTick(() => {
            const textarea = document.querySelector('.logs-textarea textarea')
            if (textarea) {
              textarea.scrollTop = textarea.scrollHeight
            }
          })
        }
      } catch (error) {
        console.error('获取爬虫状态失败:', error)
      }
    }

    const stopCrawler = () => {
      if (statusCheckInterval) {
        clearInterval(statusCheckInterval)
      }
      crawlerLoading.value = false
      crawlerTask.value = null
      ElMessage.info('已停止监控爬虫状态')
    }

    const cleanTags = async () => {
      cleaningTags.value = true
      try {
        const response = await api.post('/admin/crawler/clean-tags')
        if (response.data.success) {
          ElMessage.success(`标签清洗完成，更新了 ${response.data.updatedCount} 条视频`)
        } else {
          ElMessage.error(response.data.message || '清洗失败')
        }
      } catch (error) {
        console.error('清洗标签失败:', error)
        ElMessage.error('清洗标签失败')
      } finally {
        cleaningTags.value = false
      }
    }

    const clearLogs = () => {
      crawlerLogs.value = ''
      lastLogIndex = 0
    }

    const fetchProxyList = async () => {
      proxyLoading.value = true
      try {
        const response = await api.get('/admin/proxy/list')
        if (response.data.success) {
          proxyList.value = response.data.data
        }
      } catch (error) {
        console.error('获取代理列表失败:', error)
        ElMessage.error('获取代理列表失败')
      } finally {
        proxyLoading.value = false
      }
    }

    const openProxyModal = (proxy = null) => {
      if (proxy) {
        proxyForm.value = {
          id: proxy.id,
          protocol: proxy.protocol,
          host: proxy.host,
          port: proxy.port,
          username: proxy.username || '',
          password: ''
        }
      } else {
        proxyForm.value = {
          id: null,
          protocol: 'http',
          host: '',
          port: 80,
          username: '',
          password: ''
        }
      }
      showProxyModal.value = true
    }

    const editProxy = (proxy) => {
      openProxyModal(proxy)
    }

    const saveProxy = async () => {
      if (!proxyForm.value.host || !proxyForm.value.port) {
        ElMessage.warning('主机和端口不能为空')
        return
      }

      try {
        let response
        if (proxyForm.value.id) {
          response = await api.put(`/admin/proxy/update/${proxyForm.value.id}`, proxyForm.value)
        } else {
          response = await api.post('/admin/proxy/add', proxyForm.value)
        }

        if (response.data.success) {
          ElMessage.success(proxyForm.value.id ? '更新成功' : '添加成功')
          showProxyModal.value = false
          fetchProxyList()
        } else {
          ElMessage.error(response.data.message || '操作失败')
        }
      } catch (error) {
        console.error('保存代理失败:', error)
        ElMessage.error('保存代理失败')
      }
    }

    const deleteProxy = async (proxy) => {
      if (!confirm(`确定要删除代理 ${proxy.host}:${proxy.port} 吗？`)) return

      try {
        const response = await api.delete(`/admin/proxy/delete/${proxy.id}`)
        if (response.data.success) {
          ElMessage.success('删除成功')
          fetchProxyList()
        } else {
          ElMessage.error(response.data.message || '删除失败')
        }
      } catch (error) {
        console.error('删除代理失败:', error)
        ElMessage.error('删除代理失败')
      }
    }

    const toggleProxyStatus = async (proxy) => {
      try {
        const response = await api.put(`/admin/proxy/toggle/${proxy.id}`)
        if (response.data.success) {
          ElMessage.success('状态切换成功')
          fetchProxyList()
        } else {
          ElMessage.error(response.data.message || '操作失败')
        }
      } catch (error) {
        console.error('切换状态失败:', error)
        ElMessage.error('切换状态失败')
      }
    }

    const reloadProxyCache = async () => {
      try {
        const response = await api.post('/admin/proxy/reload')
        if (response.data.success) {
          ElMessage.success(response.data.message)
        } else {
          ElMessage.error(response.data.message || '重载失败')
        }
      } catch (error) {
        console.error('重载代理缓存失败:', error)
        ElMessage.error('重载代理缓存失败')
      }
    }

    const fetchNotificationList = async () => {
      notificationLoading.value = true
      try {
        const response = await api.get('/admin/notification/list', {
          params: {
            page: notificationPage.value,
            pageSize: notificationPageSize.value
          }
        })
        if (response.data.success) {
          notificationList.value = response.data.data
          notificationTotal.value = response.data.total
        }
      } catch (error) {
        console.error('获取通知列表失败:', error)
        ElMessage.error('获取通知列表失败')
      } finally {
        notificationLoading.value = false
      }
    }

    const openNotificationModal = async () => {
      notificationForm.value = {
        id: null,
        title: '',
        content: '',
        type: 'normal',
        status: 1,
        sendType: 'all',
        selectedUsers: []
      }
      
      try {
        const response = await api.get('/admin/notification/users')
        if (response.data.success) {
          allUsers.value = response.data.data
        }
      } catch (error) {
        console.error('获取用户列表失败:', error)
      }
      
      showNotificationModal.value = true
    }

    const editNotification = (notification) => {
      notificationForm.value = {
        id: notification.id,
        title: notification.title,
        content: notification.content,
        type: notification.type || 'normal',
        status: notification.status,
        sendType: 'all',
        selectedUsers: []
      }
      showNotificationModal.value = true
    }

    const saveNotification = async () => {
      if (!notificationForm.value.title || !notificationForm.value.content) {
        ElMessage.warning('请填写标题和内容')
        return
      }

      try {
        let response
        if (notificationForm.value.id) {
          response = await api.put('/admin/notification/update', notificationForm.value)
        } else {
          const data = { ...notificationForm.value }
          if (data.sendType === 'all') {
            response = await api.post('/admin/notification/add', data)
          } else {
            response = await api.post('/admin/notification/add', data, {
              params: { userIds: data.selectedUsers.join(',') }
            })
          }
        }

        if (response.data.success) {
          ElMessage.success(notificationForm.value.id ? '更新成功' : '发布成功')
          showNotificationModal.value = false
          fetchNotificationList()
        } else {
          ElMessage.error(response.data.message || '操作失败')
        }
      } catch (error) {
        console.error('保存通知失败:', error)
        ElMessage.error('保存通知失败')
      }
    }

    const deleteNotification = async (notification) => {
      if (!confirm(`确定要删除通知"${notification.title}"吗？`)) return

      try {
        const response = await api.delete(`/admin/notification/delete/${notification.id}`)
        if (response.data.success) {
          ElMessage.success('删除成功')
          fetchNotificationList()
        } else {
          ElMessage.error(response.data.message || '删除失败')
        }
      } catch (error) {
        console.error('删除通知失败:', error)
        ElMessage.error('删除通知失败')
      }
    }

    const toggleNotificationStatus = async (notification) => {
      try {
        const response = await api.put(`/admin/notification/toggle/${notification.id}`)
        if (response.data.success) {
          ElMessage.success(response.data.message)
          fetchNotificationList()
        } else {
          ElMessage.error(response.data.message || '操作失败')
        }
      } catch (error) {
        console.error('切换状态失败:', error)
        ElMessage.error('切换状态失败')
      }
    }

    const sendNotificationToAll = async (notification) => {
      if (!confirm(`确定要将通知"${notification.title}"发送给所有用户吗？`)) return

      try {
        const response = await api.post(`/admin/notification/send-to-all/${notification.id}`)
        if (response.data.success) {
          ElMessage.success(response.data.message)
        } else {
          ElMessage.error(response.data.message || '发送失败')
        }
      } catch (error) {
        console.error('发送通知失败:', error)
        ElMessage.error('发送通知失败')
      }
    }

    const getNotificationTypeStyle = (type) => {
      const styles = {
        normal: '',
        important: 'warning',
        urgent: 'danger'
      }
      return styles[type] || ''
    }

    const getNotificationTypeText = (type) => {
      const texts = {
        normal: '普通',
        important: '重要',
        urgent: '紧急'
      }
      return texts[type] || '普通'
    }

    const fetchBannerList = async () => {
      bannerLoading.value = true
      try {
        const response = await api.get('/banner/admin/list', {
          params: {
            page: bannerPage.value,
            pageSize: bannerPageSize.value
          }
        })
        if (response.data.success) {
          bannerList.value = response.data.data
          bannerTotal.value = response.data.total
        }
      } catch (error) {
        console.error('获取轮播图列表失败:', error)
        ElMessage.error('获取轮播图列表失败')
      } finally {
        bannerLoading.value = false
      }
    }

    const openBannerModal = () => {
      bannerForm.value = {
        id: null,
        title: '',
        description: '',
        imageUrl: '',
        linkUrl: '',
        sortOrder: 0,
        status: 1
      }
      showBannerModal.value = true
    }

    const editBanner = (banner) => {
      bannerForm.value = {
        id: banner.id,
        title: banner.title,
        description: banner.description || '',
        imageUrl: banner.imageUrl,
        linkUrl: banner.linkUrl || '',
        sortOrder: banner.sortOrder || 0,
        status: banner.status
      }
      showBannerModal.value = true
    }

    const saveBanner = async () => {
      if (!bannerForm.value.title || !bannerForm.value.imageUrl) {
        ElMessage.warning('请填写标题并上传图片')
        return
      }

      try {
        let response
        if (bannerForm.value.id) {
          response = await api.put('/banner/admin/update', bannerForm.value)
        } else {
          response = await api.post('/banner/admin/add', bannerForm.value)
        }

        if (response.data.success) {
          ElMessage.success(bannerForm.value.id ? '更新成功' : '添加成功')
          showBannerModal.value = false
          fetchBannerList()
        } else {
          ElMessage.error(response.data.message || '操作失败')
        }
      } catch (error) {
        console.error('保存轮播图失败:', error)
        ElMessage.error('保存轮播图失败')
      }
    }

    const deleteBanner = async (banner) => {
      if (!confirm(`确定要删除轮播图"${banner.title}"吗？`)) return

      try {
        const response = await api.delete(`/banner/admin/delete/${banner.id}`)
        if (response.data.success) {
          ElMessage.success('删除成功')
          fetchBannerList()
        } else {
          ElMessage.error(response.data.message || '删除失败')
        }
      } catch (error) {
        console.error('删除轮播图失败:', error)
        ElMessage.error('删除轮播图失败')
      }
    }

    const toggleBannerStatus = async (banner) => {
      try {
        const response = await api.put(`/banner/admin/toggle/${banner.id}`)
        if (response.data.success) {
          ElMessage.success(response.data.message)
          fetchBannerList()
        } else {
          ElMessage.error(response.data.message || '操作失败')
        }
      } catch (error) {
        console.error('切换状态失败:', error)
        ElMessage.error('切换状态失败')
      }
    }

    const beforeBannerUpload = (file) => {
      const isImage = file.type.startsWith('image/')
      const isLt5M = file.size / 1024 / 1024 < 5

      if (!isImage) {
        ElMessage.error('只能上传图片文件!')
        return false
      }
      if (!isLt5M) {
        ElMessage.error('图片大小不能超过 5MB!')
        return false
      }
      return true
    }

    const handleBannerUploadSuccess = (response) => {
      if (response.success) {
        const imageUrl = response.url.startsWith('http') 
          ? response.url 
          : `http://localhost:8080${response.url}`
        bannerForm.value.imageUrl = imageUrl
        ElMessage.success('上传成功')
      } else {
        ElMessage.error(response.message || '上传失败')
      }
    }

    const fetchBilibiliVideos = async () => {
      bilibiliLoading.value = true
      try {
        const response = await api.get('/admin/bilibili/videos', {
          params: {
            page: bilibiliPage.value,
            pageSize: bilibiliPageSize.value,
            keyword: importSearchKeyword.value
          }
        })
        if (response.data.success) {
          bilibiliVideos.value = response.data.data
          bilibiliTotal.value = response.data.total
        }
      } catch (error) {
        console.error('获取B站视频列表失败:', error)
        ElMessage.error('获取视频列表失败')
      } finally {
        bilibiliLoading.value = false
      }
    }

    const searchBilibiliVideos = () => {
      bilibiliPage.value = 1
      fetchBilibiliVideos()
    }

    const handleBilibiliSelectionChange = (selection) => {
      selectedBilibiliVideos.value = selection
    }

    const startImportWithFilters = async () => {
      importLoading.value = true
      importLogs.value = ''
      importLastLogIndex = 0

      try {
        const params = {
          userId: currentUserId.value
        }
        if (importCategory.value) {
          params.category = importCategory.value
        }
        if (importLimit.value) {
          params.limit = importLimit.value
        }
        params.useProxy = useProxy.value
        
        const response = await api.post('/admin/import/start', params)

        if (response.data.success) {
          importTask.value = {
            taskId: response.data.taskId,
            status: 'pending',
            progress: '正在启动...'
          }
          
          ElMessage.success('导入任务已启动')
          importStatusCheckInterval = setInterval(checkImportStatus, 1000)
        } else {
          ElMessage.error(response.data.message)
          importLoading.value = false
        }
      } catch (error) {
        console.error('启动导入失败:', error)
        ElMessage.error('启动导入失败')
        importLoading.value = false
      }
    }

    const startImportAll = async () => {
      importLoading.value = true
      importLogs.value = ''
      importLastLogIndex = 0

      try {
        const response = await api.post('/admin/import/start', {
          importAll: true
        })

        if (response.data.success) {
          importTask.value = {
            taskId: response.data.taskId,
            status: 'pending',
            progress: '正在启动...'
          }
          
          ElMessage.success('导入任务已启动')
          importStatusCheckInterval = setInterval(checkImportStatus, 1000)
        } else {
          ElMessage.error(response.data.message)
          importLoading.value = false
        }
      } catch (error) {
        console.error('启动导入失败:', error)
        ElMessage.error('启动导入失败')
        importLoading.value = false
      }
    }

    const startImportSelected = async () => {
      if (selectedBilibiliVideos.value.length === 0) {
        ElMessage.warning('请选择要导入的视频')
        return
      }

      importLoading.value = true
      importLogs.value = ''
      importLastLogIndex = 0

      const videoIds = selectedBilibiliVideos.value.map(v => v.id)

      try {
        const response = await api.post('/admin/import/start', {
          videoIds: videoIds,
          useProxy: useProxy.value
        })

        if (response.data.success) {
          importTask.value = {
            taskId: response.data.taskId,
            status: 'pending',
            progress: '正在启动...'
          }
          
          ElMessage.success('导入任务已启动')
          importStatusCheckInterval = setInterval(checkImportStatus, 1000)
        } else {
          ElMessage.error(response.data.message)
          importLoading.value = false
        }
      } catch (error) {
        console.error('启动导入失败:', error)
        ElMessage.error('启动导入失败')
        importLoading.value = false
      }
    }

    const importSingleVideo = async (video) => {
      importLoading.value = true
      importLogs.value = ''
      importLastLogIndex = 0

      try {
        const response = await api.post('/admin/import/start', {
          videoIds: [video.id]
        })

        if (response.data.success) {
          importTask.value = {
            taskId: response.data.taskId,
            status: 'pending',
            progress: '正在启动...'
          }
          
          ElMessage.success('导入任务已启动')
          importStatusCheckInterval = setInterval(checkImportStatus, 1000)
        } else {
          ElMessage.error(response.data.message)
          importLoading.value = false
        }
      } catch (error) {
        console.error('启动导入失败:', error)
        ElMessage.error('启动导入失败')
        importLoading.value = false
      }
    }

    const checkImportStatus = async () => {
      if (!importTask.value) return

      try {
        const [statusResponse, logsResponse] = await Promise.all([
          api.get(`/admin/import/status/${importTask.value.taskId}`),
          api.get(`/admin/import/logs/${importTask.value.taskId}?since=${importLastLogIndex}`)
        ])

        if (statusResponse.data.success) {
          importTask.value.status = statusResponse.data.status
          importTask.value.progress = statusResponse.data.progress
          importTask.value.totalCount = statusResponse.data.totalCount
          importTask.value.successCount = statusResponse.data.successCount
          importTask.value.skipCount = statusResponse.data.skipCount

          if (statusResponse.data.status === 'completed') {
            clearInterval(importStatusCheckInterval)
            importLoading.value = false
            ElNotification({
              title: '导入完成',
              message: `成功: ${importTask.value.successCount}, 跳过: ${importTask.value.skipCount}`,
              type: 'success'
            })
            fetchStats()
            fetchVideos()
          } else if (statusResponse.data.status === 'failed') {
            clearInterval(importStatusCheckInterval)
            importLoading.value = false
            ElNotification({
              title: '导入失败',
              message: '导入过程中发生错误',
              type: 'error'
            })
          }
        }

        if (logsResponse.data.success && logsResponse.data.logs && logsResponse.data.logs.length > 0) {
          const newLogs = logsResponse.data.logs.join('\n')
          if (importLogs.value) {
            importLogs.value += '\n' + newLogs
          } else {
            importLogs.value = newLogs
          }
          importLastLogIndex = logsResponse.data.nextIndex
          
          nextTick(() => {
            const textarea = document.querySelectorAll('.logs-textarea textarea')
            if (textarea.length > 1) {
              textarea[1].scrollTop = textarea[1].scrollHeight
            } else if (textarea.length === 1) {
              textarea[0].scrollTop = textarea[0].scrollHeight
            }
          })
        }
      } catch (error) {
        console.error('获取导入状态失败:', error)
      }
    }

    const clearImportLogs = () => {
      importLogs.value = ''
      importLastLogIndex = 0
    }

    const logout = () => {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    }

    const goToAnalysis = () => {
      router.push('/analysis')
    }

    watch(currentMenu, (newMenu) => {
      const systemMenus = ['dashboard', 'categories', 'users', 'notifications', 'banners']
      const crawlerMenus = ['videos', 'crawler', 'import', 'proxy', 'analysis']
      
      if (systemMenus.includes(newMenu)) {
        expandedGroups.value.system = true
      }
      if (crawlerMenus.includes(newMenu)) {
        expandedGroups.value.crawler = true
      }
      
      if (newMenu === 'categories') {
        fetchCategories()
      }
      if (newMenu === 'import') {
        fetchBilibiliVideos()
      }
      if (newMenu === 'proxy') {
        fetchProxyList()
      }
      if (newMenu === 'notifications') {
        fetchNotificationList()
      }
      if (newMenu === 'banners') {
        fetchBannerList()
      }
    })

    onMounted(async () => {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      username.value = user.username || 'Admin'
      currentUserId.value = user.id || null
      userRole.value = user.roleCode || 'USER'
      userPermissions.value = user.permissions || []
      
      const systemMenus = ['dashboard', 'categories', 'users', 'notifications', 'banners']
      const crawlerMenus = ['videos', 'crawler', 'import', 'proxy', 'analysis']
      
      if (systemMenus.includes(currentMenu.value)) {
        expandedGroups.value.system = true
      }
      if (crawlerMenus.includes(currentMenu.value)) {
        expandedGroups.value.crawler = true
      }
      
      fetchVideos()
      fetchStats()
      fetchCategories()
      fetchNotificationList()
      fetchBannerList()
    })

    onUnmounted(() => {
      if (statusCheckInterval) {
        clearInterval(statusCheckInterval)
      }
      if (importStatusCheckInterval) {
        clearInterval(importStatusCheckInterval)
      }
    })

    return {
      username,
      currentMenu,
      expandedGroups,
      toggleGroup,
      menuTitle,
      videos,
      stats,
      page,
      totalPages,
      searchKeyword,
      showVideoModal,
      selectedVideo,
      logsTextarea,
      categories,
      categoryPage,
      categoryTotalPages,
      showCategoryModal,
      categoryForm,
      crawlerForm,
      crawlerLoading,
      crawlerTask,
      crawlerLogs,
      bilibiliVideos,
      bilibiliPage,
      bilibiliPageSize,
      bilibiliTotal,
      bilibiliLoading,
      importSearchKeyword,
      selectedBilibiliVideos,
      importLoading,
      importTask,
      importLogs,
      importCategory,
      importLimit,
      formatNumber,
      formatImageUrl,
      formatDate,
      getCategoryPercent,
      getTaskStatusType,
      getTaskStatusText,
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
      startCrawler,
      stopCrawler,
      clearLogs,
      cleaningTags,
      cleanTags,
      onCategoryChange,
      fetchBilibiliVideos,
      searchBilibiliVideos,
      handleBilibiliSelectionChange,
      startImportWithFilters,
      startImportSelected,
      importSingleVideo,
      clearImportLogs,
      proxyList,
      proxyLoading,
      showProxyModal,
      proxyForm,
      fetchProxyList,
      openProxyModal,
      editProxy,
      saveProxy,
      deleteProxy,
      toggleProxyStatus,
      reloadProxyCache,
      logout,
      goToAnalysis,
      notificationList,
      notificationLoading,
      notificationPage,
      notificationPageSize,
      notificationTotal,
      showNotificationModal,
      notificationForm,
      allUsers,
      fetchNotificationList,
      openNotificationModal,
      editNotification,
      saveNotification,
      deleteNotification,
      toggleNotificationStatus,
      sendNotificationToAll,
      getNotificationTypeStyle,
      getNotificationTypeText,
      bannerList,
      bannerLoading,
      bannerPage,
      bannerPageSize,
      bannerTotal,
      showBannerModal,
      bannerForm,
      uploadUrl,
      fetchBannerList,
      openBannerModal,
      editBanner,
      saveBanner,
      deleteBanner,
      toggleBannerStatus,
      beforeBannerUpload,
      handleBannerUploadSuccess,
      canAccessSystem,
      canAccessData
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

.menu-group {
  margin-bottom: 4px;
}

.menu-group-title {
  padding: 12px 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
  user-select: none;
}

.menu-group-title:hover {
  background: rgba(255, 255, 255, 0.05);
}

.menu-group-title .group-icon {
  font-size: 16px;
}

.menu-group-title .arrow {
  margin-left: auto;
  font-size: 10px;
  transition: transform 0.3s;
}

.menu-group-title .arrow.expanded {
  transform: rotate(180deg);
}

.menu-group-items {
  overflow: hidden;
}

.nav-item {
  padding: 12px 24px;
  padding-left: 40px;
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

.crawler-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.crawler-card {
  min-height: 500px;
}

.card-header {
  font-size: 16px;
  font-weight: 500;
}

.form-tip {
  margin-left: 12px;
  color: #999;
  font-size: 12px;
}

.crawler-status {
  margin-top: 16px;
}

.crawler-logs {
  margin-top: 16px;
}

.logs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-weight: 500;
}

.logs-textarea {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
}

.logs-textarea :deep(textarea) {
  background: #1e1e1e;
  color: #d4d4d4;
}

.crawler-tips {
  height: fit-content;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  color: #666;
  font-size: 14px;
  line-height: 2;
}

.tips-list li {
  margin-bottom: 8px;
}

.import-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.import-card {
  min-height: 400px;
}

.import-filters {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-label {
  font-weight: 500;
  color: #606266;
}

.filter-tip {
  color: #909399;
  font-size: 12px;
}

.import-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.import-status-card {
  margin-top: 16px;
}

.import-logs {
  margin-top: 16px;
}

.proxy-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.proxy-card {
  min-height: 400px;
}

.proxy-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>
