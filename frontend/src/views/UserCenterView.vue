<template>
  <div class="user-center">
    <div class="center-container">
      <div class="sidebar">
        <div class="sidebar-header">
          <h2>个人中心</h2>
        </div>
        <div class="sidebar-menu">
          <el-menu :default-active="activeMenu" class="menu" @select="handleMenuSelect">
            <el-menu-item index="home" class="menu-item">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="info" class="menu-item">
              <el-icon><User /></el-icon>
              <span>我的信息</span>
            </el-menu-item>
            <el-menu-item index="avatar" class="menu-item">
              <el-icon><Picture /></el-icon>
              <span>我的头像</span>
            </el-menu-item>
            <el-menu-item index="achievements" class="menu-item">
              <el-icon><Trophy /></el-icon>
              <span>成就勋章</span>
            </el-menu-item>
            <el-menu-item index="security" class="menu-item">
              <el-icon><Lock /></el-icon>
              <span>账号安全</span>
            </el-menu-item>
            <el-menu-item index="records" class="menu-item">
              <el-icon><Timer /></el-icon>
              <span>我的记录</span>
            </el-menu-item>
            <el-menu-item index="follows" class="menu-item">
              <el-icon><Star /></el-icon>
              <span>我的关注</span>
            </el-menu-item>
          </el-menu>
        </div>
      </div>
      
      <div class="main-content">
        <div v-if="activeMenu === 'home'" class="content-panel">
          <div class="user-info-section">
            <div class="user-info">
              <el-avatar :size="80" :src="userInfo.avatar"></el-avatar>
              <div class="user-details">
                <div class="username-section">
                  <h3 class="username">{{ userInfo.username }}</h3>
                  <span class="member-status">正式会员</span>
                </div>
                <div class="level-section">
                  <span class="level">LV{{ userInfo.level }}</span>
                  <el-progress :percentage="levelPercentage" :format="formatProgress" />
                </div>
                <div class="coins-section">
                  <span class="coin-item">
                    <el-icon><Coin /></el-icon>
                    {{ userInfo.gold }}
                  </span>
                  <span class="coin-item">
                    <el-icon><Money /></el-icon>
                    {{ userInfo.coins }}
                  </span>
                </div>
              </div>
              <div class="action-buttons">
                <el-button type="primary" size="small" @click="activeMenu = 'info'">修改资料</el-button>
                <el-button size="small">个人空间</el-button>
              </div>
            </div>
          </div>
          
          <div class="daily-rewards-section">
            <div class="section-header">
              <el-icon><Present /></el-icon>
              <h3>每日奖励</h3>
            </div>
            <div class="rewards-grid">
              <div class="reward-item" :class="{ completed: dailyRewards.login }" @click="claimReward('login')">
                <div class="reward-icon">
                  <el-icon v-if="dailyRewards.login" :size="40"><Check /></el-icon>
                  <span v-else class="exp-text">5EXP</span>
                </div>
                <div class="reward-title">每日登录</div>
                <div class="reward-status">{{ dailyRewards.login ? '5经验值到手' : '点击领取' }}</div>
              </div>
              <div class="reward-item" :class="{ completed: dailyRewards.watch }" @click="claimReward('watch')">
                <div class="reward-icon">
                  <el-icon v-if="dailyRewards.watch" :size="40"><Check /></el-icon>
                  <span v-else class="exp-text">5EXP</span>
                </div>
                <div class="reward-title">每日观看视频</div>
                <div class="reward-status">{{ dailyRewards.watch ? '5经验值到手' : '观看视频后领取' }}</div>
              </div>
              <div class="reward-item" :class="{ completed: dailyRewards.coin >= 50 }">
                <div class="reward-icon">
                  <el-icon v-if="dailyRewards.coin >= 50" :size="40"><Check /></el-icon>
                  <span v-else class="exp-text">50EXP</span>
                </div>
                <div class="reward-title">每日投币</div>
                <div class="reward-status">已获得{{ dailyRewards.coin }}/50</div>
              </div>
              <div class="reward-item" :class="{ completed: dailyRewards.share }">
                <div class="reward-icon">
                  <el-icon v-if="dailyRewards.share" :size="40"><Check /></el-icon>
                  <span v-else class="exp-text">5EXP</span>
                </div>
                <div class="reward-title">每日分享视频</div>
                <div class="reward-status">{{ dailyRewards.share ? '已完成' : '未完成' }}</div>
              </div>
            </div>
          </div>
          
          <div class="achievements-section">
            <div class="section-header">
              <el-icon><Trophy /></el-icon>
              <h3>成就勋章</h3>
            </div>
            <div class="achievements-content">
              <div class="empty-achievements">
                <el-empty description="你还没有领取过勋章哦！" />
                <el-button type="primary" size="small" @click="activeMenu = 'achievements'">查看勋章详情</el-button>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else-if="activeMenu === 'info'" class="content-panel">
          <div class="panel-header">
            <h3>我的信息</h3>
          </div>
          <el-form ref="userInfoFormRef" :model="userInfo" :rules="userInfoRules" label-width="100px" class="info-form">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="userInfo.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="userInfo.gender">
                <el-radio value="male">男</el-radio>
                <el-radio value="female">女</el-radio>
                <el-radio value="secret">保密</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="生日">
              <el-date-picker v-model="userInfo.birthday" type="date" placeholder="选择生日" value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-form-item label="个性签名">
              <el-input v-model="userInfo.signature" type="textarea" :rows="3" placeholder="请输入个性签名" maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveUserInfo">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <div v-else-if="activeMenu === 'avatar'" class="content-panel">
          <div class="panel-header">
            <h3>我的头像</h3>
          </div>
          <div class="avatar-section">
            <div class="avatar-preview">
              <el-avatar :size="150" :src="previewUrl || userInfo.avatar || 'http://localhost:8080/api/avatar/default'"></el-avatar>
              <p>当前头像</p>
            </div>
            <div class="avatar-actions">
              <el-upload
                class="avatar-uploader"
                :auto-upload="false"
                :show-file-list="false"
                accept="image/*"
                @change="handleFileChange"
              >
                <el-button type="primary">
                  <el-icon><Upload /></el-icon>
                  选择头像
                </el-button>
              </el-upload>
              <el-button type="success" :disabled="!avatarFile" @click="submitAvatar">
                <el-icon><Check /></el-icon>
                上传头像
              </el-button>
            </div>
            <p class="avatar-tip">支持 jpg、png、gif 格式，建议上传正方形图片，文件大小不超过 2MB</p>
          </div>
        </div>
        
        <div v-else-if="activeMenu === 'achievements'" class="content-panel">
          <div class="panel-header">
            <h3>成就勋章</h3>
          </div>
          <div class="achievements-list">
            <el-empty description="暂无成就勋章" />
          </div>
        </div>
        
        <div v-else-if="activeMenu === 'security'" class="content-panel">
          <div class="panel-header">
            <h3>账号安全</h3>
          </div>
          <div class="security-section">
            <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px" class="security-form">
              <el-form-item label="当前密码" prop="currentPassword">
                <el-input v-model="passwordForm.currentPassword" type="password" show-password placeholder="请输入当前密码" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="changePassword">修改密码</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
        
        <div v-else-if="activeMenu === 'records'" class="content-panel">
          <div class="panel-header">
            <h3>我的记录</h3>
          </div>
          <el-tabs v-model="recordTab" @tab-change="handleRecordTabChange">
            <el-tab-pane label="观看历史" name="watch">
              <el-empty description="暂无观看记录" />
            </el-tab-pane>
            <el-tab-pane label="点赞记录" name="like">
              <div v-if="loadingRecords" class="loading-records">
                <el-icon class="is-loading" :size="40"><Loading /></el-icon>
                <p>加载中...</p>
              </div>
              <el-empty v-else-if="likeRecords.length === 0" description="暂无点赞记录" />
              <div v-else class="record-list">
                <div class="record-item" v-for="video in likeRecords" :key="video.id" @click="goToVideo(video.id)">
                  <div class="record-cover">
                    <img :src="video.coverUrl || 'http://localhost:8080/backend/image/default_image/defaultImage.png'" alt="视频封面" />
                    <div class="play-overlay">
                      <el-icon :size="24"><VideoPlay /></el-icon>
                    </div>
                  </div>
                  <div class="record-info">
                    <div class="record-title">{{ video.title }}</div>
                    <div class="record-meta">
                      <span class="author">{{ video.username }}</span>
                      <span class="dot">·</span>
                      <span>{{ formatViewCount(video.viewCount) }}次观看</span>
                    </div>
                    <div class="record-time">点赞于 {{ formatDate(video.likeTime) }}</div>
                  </div>
                  <div class="record-actions">
                    <el-button type="danger" size="small" text @click.stop="removeLike(video.id)">
                      <el-icon><Delete /></el-icon>
                      取消点赞
                    </el-button>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="收藏记录" name="favorite">
              <div v-if="loadingRecords" class="loading-records">
                <el-icon class="is-loading" :size="40"><Loading /></el-icon>
                <p>加载中...</p>
              </div>
              <el-empty v-else-if="favoriteRecords.length === 0" description="暂无收藏记录" />
              <div v-else class="record-list">
                <div class="record-item" v-for="video in favoriteRecords" :key="video.id" @click="goToVideo(video.id)">
                  <div class="record-cover">
                    <img :src="video.coverUrl || 'http://localhost:8080/backend/image/default_image/defaultImage.png'" alt="视频封面" />
                    <div class="play-overlay">
                      <el-icon :size="24"><VideoPlay /></el-icon>
                    </div>
                  </div>
                  <div class="record-info">
                    <div class="record-title">{{ video.title }}</div>
                    <div class="record-meta">
                      <span class="author">{{ video.username }}</span>
                      <span class="dot">·</span>
                      <span>{{ formatViewCount(video.viewCount) }}次观看</span>
                    </div>
                    <div class="record-time">收藏于 {{ formatDate(video.favoriteTime) }}</div>
                  </div>
                  <div class="record-actions">
                    <el-button type="danger" size="small" text @click.stop="removeFavorite(video.id)">
                      <el-icon><Delete /></el-icon>
                      取消收藏
                    </el-button>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
        
        <div v-else-if="activeMenu === 'follows'" class="content-panel">
          <div class="panel-header">
            <h3>我的关注</h3>
          </div>
          <el-tabs v-model="followTab" @tab-change="handleFollowTabChange">
            <el-tab-pane label="我的关注" name="followings">
              <div v-if="loadingFollows" class="loading-records">
                <el-icon class="is-loading" :size="40"><Loading /></el-icon>
                <p>加载中...</p>
              </div>
              <el-empty v-else-if="followings.length === 0" description="暂无关注的用户" />
              <div v-else class="follow-list">
                <div class="follow-item" v-for="user in followings" :key="user.id" @click="goToUserProfile(user.id)">
                  <el-avatar :size="50" :src="user.avatar || 'http://localhost:8080/backend/image/default_image/defaultImage.png'"></el-avatar>
                  <div class="follow-info">
                    <div class="follow-username">{{ user.username }}</div>
                    <div class="follow-signature" v-if="user.signature">{{ user.signature }}</div>
                    <div class="follow-stats">
                      <span>{{ user.videoCount || 0 }} 视频</span>
                      <span class="dot">·</span>
                      <span>{{ user.followerCount || 0 }} 粉丝</span>
                    </div>
                  </div>
                  <div class="follow-actions">
                    <el-button type="primary" size="small" plain @click.stop="toggleUserFollow(user)">
                      取消关注
                    </el-button>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="我的粉丝" name="followers">
              <div v-if="loadingFollows" class="loading-records">
                <el-icon class="is-loading" :size="40"><Loading /></el-icon>
                <p>加载中...</p>
              </div>
              <el-empty v-else-if="followers.length === 0" description="暂无粉丝" />
              <div v-else class="follow-list">
                <div class="follow-item" v-for="user in followers" :key="user.id" @click="goToUserProfile(user.id)">
                  <el-avatar :size="50" :src="user.avatar || 'http://localhost:8080/backend/image/default_image/defaultImage.png'"></el-avatar>
                  <div class="follow-info">
                    <div class="follow-username">{{ user.username }}</div>
                    <div class="follow-signature" v-if="user.signature">{{ user.signature }}</div>
                    <div class="follow-stats">
                      <span>{{ user.videoCount || 0 }} 视频</span>
                      <span class="dot">·</span>
                      <span>{{ user.followerCount || 0 }} 粉丝</span>
                    </div>
                  </div>
                  <div class="follow-actions">
                    <el-button 
                      :type="user.isFollowing ? 'default' : 'primary'" 
                      size="small" 
                      plain 
                      @click.stop="toggleUserFollow(user)"
                    >
                      {{ user.isFollowing ? '已关注' : '关注' }}
                    </el-button>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  House, User, Picture, Star, Trophy, Lock, Close, Money, Timer, Check, Share,
  Present, Coin, Upload, VideoPlay, Delete, Loading
} from '@element-plus/icons-vue'

const router = useRouter()

const activeMenu = ref('home')

const originalUsername = ref('')

const userInfo = ref({
  username: '',
  avatar: '',
  gender: 'secret',
  birthday: '',
  signature: '',
  level: 4,
  experience: 6000,
  maxExperience: 10800,
  coins: 426.1,
  gold: 0
})

const userInfoFormRef = ref<InstanceType<typeof ElForm> | null>(null)

const userInfoRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

const levelPercentage = computed(() => {
  return Math.round((userInfo.value.experience / userInfo.value.maxExperience) * 100)
})

const formatProgress = () => {
  return `${userInfo.value.experience} / ${userInfo.value.maxExperience}`
}

const dailyRewards = ref({
  login: true,
  watch: true,
  coin: 25,
  share: false
})

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordFormRef = ref<InstanceType<typeof ElForm> | null>(null)

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const verifyForm = ref({
  realName: '',
  idCard: ''
})

const recordTab = ref('watch')
const loadingRecords = ref(false)
const likeRecords = ref<any[]>([])
const favoriteRecords = ref<any[]>([])

const followTab = ref('followings')
const loadingFollows = ref(false)
const followings = ref<any[]>([])
const followers = ref<any[]>([])

const avatarFile = ref<File | null>(null)
const previewUrl = ref('')

const inviteLink = computed(() => {
  return `http://localhost:5173/register?invite=${userInfo.value.username}`
})

const handleMenuSelect = (index: string) => {
  activeMenu.value = index
}

const claimReward = (type: string) => {
  if (type === 'login' && !dailyRewards.value.login) {
    dailyRewards.value.login = true
    ElMessage.success('领取成功，获得5经验值')
  } else if (type === 'watch' && !dailyRewards.value.watch) {
    dailyRewards.value.watch = true
    ElMessage.success('领取成功，获得5经验值')
  }
}

const saveUserInfo = async () => {
  if (!userInfoFormRef.value) return
  
  await userInfoFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const token = localStorage.getItem('token')
        const response = await fetch('http://localhost:8080/api/auth/update-profile', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify({
            username: userInfo.value.username,
            oldUsername: originalUsername.value,
            gender: userInfo.value.gender,
            birthday: userInfo.value.birthday,
            signature: userInfo.value.signature
          })
        })
        
        const data = await response.json()
        if (data.success) {
          ElMessage.success(data.message || '保存成功')
          originalUsername.value = userInfo.value.username
          localStorage.setItem('user', JSON.stringify(userInfo.value))
          window.dispatchEvent(new Event('loginSuccess'))
        } else {
          ElMessage.error(data.message || '保存失败')
        }
      } catch (error) {
        ElMessage.error('保存失败，请重试')
      }
    }
  })
}

const handleFileChange = (uploadFile: any) => {
  const file = uploadFile.raw
  avatarFile.value = file
  previewUrl.value = URL.createObjectURL(file)
}

const submitAvatar = async () => {
  if (!avatarFile.value) {
    ElMessage.error('请选择要上传的头像')
    return
  }
  
  try {
    const token = localStorage.getItem('token')
    const formData = new FormData()
    formData.append('avatar', avatarFile.value)
    formData.append('username', userInfo.value.username)
    
    const response = await fetch('http://localhost:8080/api/auth/change-avatar', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    })
    
    const data = await response.json()
    if (data.success) {
      ElMessage.success(data.message)
      userInfo.value.avatar = data.user.avatar
      localStorage.setItem('user', JSON.stringify(userInfo.value))
      window.dispatchEvent(new Event('loginSuccess'))
      avatarFile.value = null
      previewUrl.value = ''
    } else {
      ElMessage.error(data.message)
    }
  } catch (error) {
    ElMessage.error('修改头像失败，请重试')
  }
}

const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const token = localStorage.getItem('token')
        const response = await fetch('http://localhost:8080/api/auth/change-password', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Authorization': `Bearer ${token}`
          },
          body: new URLSearchParams({
            username: userInfo.value.username,
            currentPassword: passwordForm.value.currentPassword,
            newPassword: passwordForm.value.newPassword
          })
        })
        
        const data = await response.json()
        if (data.success) {
          ElMessage.success(data.message)
          passwordForm.value.currentPassword = ''
          passwordForm.value.newPassword = ''
          passwordForm.value.confirmPassword = ''
        } else {
          ElMessage.error(data.message)
        }
      } catch (error) {
        ElMessage.error('修改密码失败，请重试')
      }
    }
  })
}

const copyInviteLink = () => {
  navigator.clipboard.writeText(inviteLink.value)
  ElMessage.success('链接已复制到剪贴板')
}

const handleRecordTabChange = (tab: string) => {
  if (tab === 'like') {
    loadLikeRecords()
  } else if (tab === 'favorite') {
    loadFavoriteRecords()
  }
}

const loadLikeRecords = async () => {
  loadingRecords.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('http://localhost:8080/api/video/likes', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const data = await response.json()
    if (data.success) {
      likeRecords.value = data.likes
    } else {
      ElMessage.error(data.message || '获取点赞记录失败')
    }
  } catch (error) {
    ElMessage.error('获取点赞记录失败')
  } finally {
    loadingRecords.value = false
  }
}

const loadFavoriteRecords = async () => {
  loadingRecords.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('http://localhost:8080/api/video/favorites', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const data = await response.json()
    if (data.success) {
      favoriteRecords.value = data.favorites
    } else {
      ElMessage.error(data.message || '获取收藏记录失败')
    }
  } catch (error) {
    ElMessage.error('获取收藏记录失败')
  } finally {
    loadingRecords.value = false
  }
}

const goToVideo = (videoId: number) => {
  router.push(`/video?id=${videoId}`)
}

const removeLike = async (videoId: number) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`http://localhost:8080/api/video/${videoId}/like`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const data = await response.json()
    if (data.success) {
      ElMessage.success('已取消点赞')
      likeRecords.value = likeRecords.value.filter(v => v.id !== videoId)
    } else {
      ElMessage.error(data.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const removeFavorite = async (videoId: number) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`http://localhost:8080/api/video/${videoId}/favorite`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const data = await response.json()
    if (data.success) {
      ElMessage.success('已取消收藏')
      favoriteRecords.value = favoriteRecords.value.filter(v => v.id !== videoId)
    } else {
      ElMessage.error(data.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const formatViewCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toString()
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const handleFollowTabChange = (tab: string) => {
  if (tab === 'followings') {
    loadFollowings()
  } else if (tab === 'followers') {
    loadFollowers()
  }
}

const loadFollowings = async () => {
  loadingFollows.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('http://localhost:8080/api/user/followings', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const data = await response.json()
    if (data.success) {
      followings.value = data.followings.map((u: any) => ({ ...u, isFollowing: true }))
    } else {
      ElMessage.error(data.message || '获取关注列表失败')
    }
  } catch (error) {
    ElMessage.error('获取关注列表失败')
  } finally {
    loadingFollows.value = false
  }
}

const loadFollowers = async () => {
  loadingFollows.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('http://localhost:8080/api/user/followers', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const data = await response.json()
    if (data.success) {
      followers.value = data.followers
    } else {
      ElMessage.error(data.message || '获取粉丝列表失败')
    }
  } catch (error) {
    ElMessage.error('获取粉丝列表失败')
  } finally {
    loadingFollows.value = false
  }
}

const goToUserProfile = (userId: number) => {
  router.push(`/profile?id=${userId}`)
}

const toggleUserFollow = async (user: any) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`http://localhost:8080/api/user/${user.id}/follow`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const data = await response.json()
    if (data.success) {
      user.isFollowing = data.isFollowing
      if (followTab.value === 'followings' && !data.isFollowing) {
        followings.value = followings.value.filter(u => u.id !== user.id)
      }
      ElMessage.success(data.isFollowing ? '关注成功' : '已取消关注')
    } else {
      ElMessage.error(data.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    userInfo.value.username = user.username
    userInfo.value.avatar = user.avatar || 'http://localhost:8080/api/avatar/default'
    userInfo.value.gender = user.gender || 'secret'
    userInfo.value.birthday = user.birthday || ''
    userInfo.value.signature = user.signature || ''
    originalUsername.value = user.username
  }
})
</script>

<style scoped>
.user-center {
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

.user-info-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
}

.user-details {
  flex: 1;
  margin-left: 20px;
}

.username-section {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.username {
  margin: 0 10px 0 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.member-status {
  background-color: #f0f9eb;
  color: #67c23a;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.level-section {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.level {
  display: inline-block;
  background-color: #fde2e2;
  color: #f56c6c;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  margin-right: 10px;
}

.level-section :deep(.el-progress) {
  flex: 1;
}

.coins-section {
  display: flex;
  gap: 20px;
}

.coin-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #606266;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.daily-rewards-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0 0 0 10px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.rewards-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.reward-item {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  background-color: #f9f9f9;
  transition: all 0.3s;
  cursor: pointer;
}

.reward-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.reward-item.completed .reward-icon {
  background-color: #67c23a;
  color: #fff;
}

.reward-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  font-size: 24px;
}

.exp-text {
  font-size: 18px;
  font-weight: 600;
}

.reward-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 5px;
}

.reward-status {
  font-size: 12px;
  color: #909399;
}

.achievements-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.achievements-content {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
}

.empty-achievements {
  text-align: center;
}

.info-form,
.security-form,
.verify-form {
  max-width: 500px;
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 50px;
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.current-avatar {
  text-align: center;
}

.current-avatar p {
  margin-top: 10px;
  color: #909399;
}

.upload-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}

.coins-overview {
  margin-bottom: 30px;
}

.coin-card {
  height: 120px;
}

.coin-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.coin-text {
  flex: 1;
}

.coin-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.coin-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.coin-records {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.coin-records h4 {
  margin: 0 0 20px;
  font-size: 16px;
  color: #303133;
}

.invite-content {
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.invite-link {
  margin: 20px 0;
}

.invite-link p {
  margin-bottom: 10px;
  color: #606266;
}

.invite-stats {
  margin-top: 30px;
}

.loading-records {
  text-align: center;
  padding: 60px 0;
  color: #909399;
}

.loading-records .el-icon {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.record-item {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.record-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.record-cover {
  position: relative;
  width: 160px;
  height: 90px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.record-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.record-cover .play-overlay {
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

.record-item:hover .play-overlay {
  opacity: 1;
}

.play-overlay .el-icon {
  color: #fff;
  background-color: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  padding: 8px;
}

.record-info {
  flex: 1;
  margin-left: 16px;
  min-width: 0;
}

.record-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-meta {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.record-meta .author {
  color: #606266;
}

.record-meta .dot {
  font-size: 10px;
}

.record-time {
  font-size: 12px;
  color: #c0c4cc;
}

.record-actions {
  margin-left: 16px;
}

.follow-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.follow-item {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.follow-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.follow-info {
  flex: 1;
  margin-left: 16px;
  min-width: 0;
}

.follow-username {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.follow-signature {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.follow-stats {
  font-size: 12px;
  color: #c0c4cc;
  display: flex;
  align-items: center;
  gap: 8px;
}

.follow-stats .dot {
  font-size: 10px;
}

.follow-actions {
  margin-left: 16px;
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
  
  .rewards-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .user-info {
    flex-direction: column;
    text-align: center;
  }
  
  .user-details {
    margin-left: 0;
    margin-top: 15px;
  }
  
  .action-buttons {
    margin-top: 15px;
  }
  
  .avatar-section {
    flex-direction: column;
    gap: 20px;
  }
}
</style>
