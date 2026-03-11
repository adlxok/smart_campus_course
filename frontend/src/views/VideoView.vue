<template>
  <div class="video-page">
    <div class="video-header">
      <el-button text @click="router.push('/')">
        <el-icon><ArrowLeft /></el-icon>
        返回首页
      </el-button>
    </div>
    
    <div class="video-container" v-if="video">
      <video ref="videoPlayer" :src="video.videoUrl" controls autoplay class="main-video"></video>
      
      <div class="video-info">
        <h1 class="video-title">{{ video.title }}</h1>
        
        <div class="video-meta">
          <div class="author-info">
            <el-avatar :size="44" :src="video.coverUrl || defaultCover">
              {{ video.username.charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="author-detail">
              <span class="author-name">{{ video.username }}</span>
              <span class="author-label">创作者</span>
            </div>
          </div>
          <div class="interaction-buttons">
            <button 
              class="interaction-btn like-btn" 
              :class="{ active: isLiked }"
              @click="toggleLike"
            >
              <span class="btn-icon">
                <svg v-if="isLiked" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M1 21h4V9H1v12zm22-11c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.59 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-2z"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" fill="currentColor">
                  <path d="M9 21h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73V10c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.59 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2zM9 9l4.34-4.34L12 10h9v2l-3 7H9V9zm-8 0v12h4V9H1z"/>
                </svg>
              </span>
              <span class="btn-count">{{ likeCount }}</span>
            </button>
            <button 
              class="interaction-btn favorite-btn" 
              :class="{ active: isFavorited }"
              @click="toggleFavorite"
            >
              <span class="btn-icon">
                <svg v-if="isFavorited" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" fill="currentColor">
                  <path d="M22 9.24l-7.19-.62L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21 12 17.27 18.18 21l-1.63-7.03L22 9.24zM12 15.4l-3.76 2.27 1-4.28-3.32-2.88 4.38-.38L12 6.1l1.71 4.04 4.38.38-3.32 2.88 1 4.28L12 15.4z"/>
                </svg>
              </span>
              <span class="btn-count">{{ favoriteCount }}</span>
            </button>
          </div>
          <div class="stats">
            <span class="stat-item">
              <el-icon><View /></el-icon>
              {{ video.viewCount }} 次播放
            </span>
            <span class="stat-item">
              <el-icon><Clock /></el-icon>
              {{ formatDate(video.createTime) }}
            </span>
          </div>
        </div>
        
        <div class="video-description">
          <h3><el-icon><Document /></el-icon> 视频简介</h3>
          <p>{{ video.description || '暂无描述' }}</p>
        </div>
        
 <el-divider />

        <div class="video-actions">
          <button 
            class="action-btn like-action"
            :class="{ active: isLiked }"
            @click="toggleLike"
          >
            <span class="action-icon">
              <svg v-if="isLiked" viewBox="0 0 24 24" fill="currentColor">
                <path d="M1 21h4V9H1v12zm22-11c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.59 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-2z"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="currentColor">
                <path d="M9 21h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73V10c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.59 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2zM9 9l4.34-4.34L12 10h9v2l-3 7H9V9zm-8 0v12h4V9H1z"/>
              </svg>
            </span>
            <span class="action-text">{{ likeCount }} 点赞</span>
          </button>
          <button 
            class="action-btn favorite-action"
            :class="{ active: isFavorited }"
            @click="toggleFavorite"
          >
            <span class="action-icon">
              <svg v-if="isFavorited" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="currentColor">
                <path d="M22 9.24l-7.19-.62L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21 12 17.27 18.18 21l-1.63-7.03L22 9.24zM12 15.4l-3.76 2.27 1-4.28-3.32-2.88 4.38-.38L12 6.1l1.71 4.04 4.38.38-3.32 2.88 1 4.28L12 15.4z"/>
              </svg>
            </span>
            <span class="action-text">{{ favoriteCount }} 收藏</span>
          </button>
        </div>
        
        <div class="comment-section">
          <div class="comment-header">
            <h3><el-icon><ChatDotRound /></el-icon> 全部评论</h3>
            <span class="comment-count">{{ totalComments }} 条评论</span>
          </div>
          
          <div class="comment-input-inline" ref="commentInputRef">
            <el-avatar :size="36" :src="userInfo.avatar">
              {{ userInfo.username ? userInfo.username.charAt(0).toUpperCase() : 'U' }}
            </el-avatar>
            <div class="input-box">
              <el-input
                v-model="newComment"
                :placeholder="replyTo ? `回复 @${replyTo.username}：` : '说点什么吧...'"
                type="textarea"
                :rows="1"
                maxlength="500"
                show-word-limit
                :autosize="{ minRows: 1, maxRows: 3 }"
              />
              <div class="input-footer">
                <el-button v-if="replyTo" size="small" @click="cancelReply" class="cancel-btn">
                  <el-icon><Close /></el-icon>
                  取消
                </el-button>
                <el-button type="primary" @click="submitComment" :loading="submitting" :disabled="!newComment.trim()" size="small">
                  <el-icon><Promotion v-if="!submitting" /></el-icon>
                  {{ replyTo ? '回复' : '发表' }}
                </el-button>
              </div>
            </div>
          </div>
          
          <div class="comment-list" v-if="topLevelComments.length > 0">
            <div v-for="comment in topLevelComments" :key="comment.id" class="comment-item">
              <CommentNode
                :comment="comment"
                :all-comments="comments"
                :user-info="userInfo"
                :default-cover="defaultCover"
                @reply="setReply"
                @delete="deleteComment"
              />
            </div>
          </div>
          
          <el-empty v-else description="还没有评论，快来抢沙发吧！" :image-size="120" />
        </div>
      </div>
      
      <div class="comment-input-fixed">
        <el-avatar :size="32" :src="userInfo.avatar">
          {{ userInfo.username ? userInfo.username.charAt(0).toUpperCase() : 'U' }}
        </el-avatar>
        <div class="fixed-input-box">
          <el-input
            v-model="newComment"
            :placeholder="replyTo ? `回复 @${replyTo.username}：` : '说点什么吧...'"
            type="textarea"
            :rows="1"
            maxlength="500"
            :autosize="{ minRows: 1, maxRows: 2 }"
          />
          <el-button v-if="replyTo" size="small" @click="cancelReply" class="cancel-btn-fixed">
            取消
          </el-button>
          <el-button type="primary" @click="submitComment" :loading="submitting" :disabled="!newComment.trim()" size="small">
            {{ replyTo ? '回复' : '发表' }}
          </el-button>
        </div>
      </div>
    </div>
    
    <div v-else class="loading">
      <el-icon class="is-loading" :size="48"><Loading /></el-icon>
      <p>加载中...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h, defineComponent } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElAvatar } from 'element-plus'
import { 
  ArrowLeft, View, Clock, Document, ChatDotRound, 
  Loading, Promotion, Close
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

interface Comment {
  id: number;
  videoId: number;
  userId: number;
  username: string;
  avatar: string;
  content: string;
  parentId: number | null;
  replyToUsername: string | null;
  createTime: string;
}

interface UserInfo {
  id: number;
  username: string;
  avatar: string;
  role: string;
}

const CommentNode = defineComponent({
  name: 'CommentNode',
  props: {
    comment: { type: Object as () => Comment, required: true },
    allComments: { type: Array as () => Comment[], required: true },
    userInfo: { type: Object as () => UserInfo, required: true },
    defaultCover: { type: String, required: true }
  },
  emits: ['reply', 'delete'],
  setup(props, { emit }) {
    const getAllReplies = (parentId: number): Comment[] => {
      const directReplies = props.allComments.filter(c => c.parentId === parentId)
      const allReplies: Comment[] = []
      
      const collectReplies = (replies: Comment[]) => {
        replies.forEach(reply => {
          allReplies.push(reply)
          const nestedReplies = props.allComments.filter(c => c.parentId === reply.id)
          if (nestedReplies.length > 0) {
            collectReplies(nestedReplies)
          }
        })
      }
      
      collectReplies(directReplies)
      return allReplies.sort((a, b) => 
        new Date(a.createTime).getTime() - new Date(b.createTime).getTime()
      )
    }
    
    const formatDate = (dateStr: string) => {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
    }
    
    const isOwner = computed(() => props.comment.userId === props.userInfo.id)
    
    const getAvatarColor = (username: string) => {
      const colors = [
        'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
        'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
        'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
        'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
        'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)'
      ]
      const index = username.charCodeAt(0) % colors.length
      return colors[index]
    }
    
    return () => {
      const replies = getAllReplies(props.comment.id)
      const avatarBg = getAvatarColor(props.comment.username)
      const hasAvatar = props.comment.avatar && props.comment.avatar.trim() !== ''
      
      return h('div', { class: 'comment-wrapper' }, [
        h('div', { class: 'comment-row' }, [
          hasAvatar 
            ? h('img', { 
                class: 'user-avatar-img', 
                src: props.comment.avatar 
              })
            : h('div', { class: 'avatar-wrapper', style: { background: avatarBg } }, [
                h('span', { class: 'avatar-letter' }, props.comment.username.charAt(0).toUpperCase())
              ]),
          h('div', { class: 'comment-main' }, [
            h('div', { class: 'comment-header' }, [
              h('span', { class: 'username' }, props.comment.username),
              props.comment.replyToUsername 
                ? h('span', { class: 'reply-arrow' }, ' 回复 ')
                : null,
              props.comment.replyToUsername 
                ? h('span', { class: 'reply-to' }, `@${props.comment.replyToUsername}`)
                : null,
              h('span', { class: 'time-dot' }, '·'),
              h('span', { class: 'time' }, formatDate(props.comment.createTime))
            ]),
            h('div', { class: 'comment-text' }, props.comment.content),
            h('div', { class: 'comment-footer' }, [
              h('button', { 
                class: 'footer-btn like',
                onClick: () => emit('reply', props.comment)
              }, [
                h('svg', { class: 'icon', viewBox: '0 0 24 24' }, [
                  h('path', { d: 'M8.59,16.59L13.17,12L8.59,7.41L10,6l6,6l-6,6L8.59,16.59z' })
                ]),
                '回复'
              ]),
              isOwner.value 
                ? h('button', { 
                    class: 'footer-btn delete',
                    onClick: () => emit('delete', props.comment.id)
                  }, [
                    h('svg', { class: 'icon', viewBox: '0 0 24 24' }, [
                      h('path', { d: 'M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41z' })
                    ]),
                    '删除'
                  ])
                : null
            ])
          ])
        ]),
        replies.length > 0 
          ? h('div', { class: 'reply-container' }, [
              h('div', { class: 'reply-count' }, `${replies.length} 条回复`),
              ...replies.map(reply => {
                const replyAvatarBg = getAvatarColor(reply.username)
                const replyHasAvatar = reply.avatar && reply.avatar.trim() !== ''
                
                return h('div', { class: 'comment-row', key: reply.id }, [
                  replyHasAvatar 
                    ? h('img', { 
                        class: 'user-avatar-img', 
                        src: reply.avatar 
                      })
                    : h('div', { class: 'avatar-wrapper', style: { background: replyAvatarBg } }, [
                        h('span', { class: 'avatar-letter' }, reply.username.charAt(0).toUpperCase())
                      ]),
                  h('div', { class: 'comment-main' }, [
                    h('div', { class: 'comment-header' }, [
                      h('span', { class: 'username' }, reply.username),
                      reply.replyToUsername 
                        ? h('span', { class: 'reply-arrow' }, ' 回复 ')
                        : null,
                      reply.replyToUsername 
                        ? h('span', { class: 'reply-to' }, `@${reply.replyToUsername}`)
                        : null,
                      h('span', { class: 'time-dot' }, '·'),
                      h('span', { class: 'time' }, formatDate(reply.createTime))
                    ]),
                    h('div', { class: 'comment-text' }, reply.content),
                    h('div', { class: 'comment-footer' }, [
                      h('button', { 
                        class: 'footer-btn like',
                        onClick: () => emit('reply', reply)
                      }, [
                        h('svg', { class: 'icon', viewBox: '0 0 24 24' }, [
                          h('path', { d: 'M8.59,16.59L13.17,12L8.59,7.41L10,6l6,6l-6,6L8.59,16.59z' })
                        ]),
                        '回复'
                      ]),
                      reply.userId === props.userInfo.id 
                        ? h('button', { 
                            class: 'footer-btn delete',
                            onClick: () => emit('delete', reply.id)
                          }, [
                            h('svg', { class: 'icon', viewBox: '0 0 24 24' }, [
                              h('path', { d: 'M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41z' })
                            ]),
                            '删除'
                          ])
                        : null
                    ])
                  ])
                ])
              })
            ])
          : null
      ])
    }
  }
})

const router = useRouter()
const route = useRoute()

const video = ref<Video | null>(null)
const comments = ref<Comment[]>([])
const newComment = ref('')
const submitting = ref(false)
const replyTo = ref<Comment | null>(null)
const defaultCover = 'http://localhost:8080/backend/image/default_image/defaultImage.png'

const userInfo = ref<UserInfo>({
  id: 0,
  username: '',
  avatar: '',
  role: ''
})

const commentTotal = ref(0)
const loadingComments = ref(false)

const isLiked = ref(false)
const isFavorited = ref(false)
const likeCount = ref(0)
const favoriteCount = ref(0)

const topLevelComments = computed(() => {
  return comments.value.filter(c => !c.parentId)
})

const totalComments = computed(() => commentTotal.value)

const setReply = (comment: Comment) => {
  replyTo.value = comment
}

const cancelReply = () => {
  replyTo.value = null
}

const loadInteractionInfo = async (videoId: number) => {
  try {
    const token = localStorage.getItem('token')
    const headers: Record<string, string> = {}
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    
    const response = await fetch(`http://localhost:8080/api/video/${videoId}/interaction`, {
      headers
    })
    const data = await response.json()
    
    if (data.success) {
      isLiked.value = data.isLiked
      isFavorited.value = data.isFavorited
      likeCount.value = data.likeCount
      favoriteCount.value = data.favoriteCount
    }
  } catch (error) {
    console.error('加载互动信息失败', error)
  }
}

const toggleLike = async () => {
  if (!video.value) return
  
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    const response = await fetch(`http://localhost:8080/api/video/${video.value.id}/like`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const data = await response.json()
    
    if (data.success) {
      isLiked.value = data.isLiked
      likeCount.value = data.likeCount
      ElMessage.success(data.isLiked ? '点赞成功' : '取消点赞')
    } else {
      ElMessage.error(data.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const toggleFavorite = async () => {
  if (!video.value) return
  
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    const response = await fetch(`http://localhost:8080/api/video/${video.value.id}/favorite`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const data = await response.json()
    
    if (data.success) {
      isFavorited.value = data.isFavorited
      favoriteCount.value = data.favoriteCount
      ElMessage.success(data.isFavorited ? '收藏成功' : '取消收藏')
    } else {
      ElMessage.error(data.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(async () => {
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    userInfo.value = JSON.parse(storedUser)
  }
  
  const videoId = route.query.id
  if (!videoId) {
    ElMessage.error('视频不存在')
    router.push('/')
    return
  }
  
  try {
    const response = await fetch(`http://localhost:8080/api/video/list?pageNum=1&pageSize=100`)
    const data = await response.json()
    
    if (data.success) {
      const foundVideo = data.data.find((v: Video) => v.id === Number(videoId))
      if (foundVideo) {
        video.value = foundVideo
        incrementViewCount(foundVideo.id)
        loadComments(foundVideo.id)
        loadInteractionInfo(foundVideo.id)
      } else {
        ElMessage.error('视频不存在')
        router.push('/')
      }
    }
  } catch (error) {
    ElMessage.error('加载视频失败')
    router.push('/')
  }
})

const loadComments = async (videoId: number) => {
  if (loadingComments.value) return
  
  loadingComments.value = true
  
  try {
    const response = await fetch(
      `http://localhost:8080/api/comment/list/${videoId}`
    )
    const data = await response.json()
    
    if (data.success) {
      comments.value = data.data
      commentTotal.value = data.total
    }
  } catch (error) {
    console.error('加载评论失败', error)
  } finally {
    loadingComments.value = false
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  if (!video.value) return
  
  submitting.value = true
  try {
    const token = localStorage.getItem('token')
    const body: any = {
      videoId: video.value.id,
      content: newComment.value
    }
    
    if (replyTo.value) {
      body.parentId = replyTo.value.id
      body.replyToUsername = replyTo.value.username
    }
    
    const response = await fetch('http://localhost:8080/api/comment/add', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(body)
    })
    
    const data = await response.json()
    
    if (data.success) {
      ElMessage.success(replyTo.value ? '回复成功' : '评论成功')
      newComment.value = ''
      replyTo.value = null
      loadComments(video.value.id)
    } else {
      ElMessage.error(data.message || '评论失败')
    }
  } catch (error) {
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

const deleteComment = async (commentId: number) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`http://localhost:8080/api/comment/${commentId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    const data = await response.json()
    
    if (data.success) {
      ElMessage.success('删除成功')
      if (video.value) {
        loadComments(video.value.id)
      }
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const incrementViewCount = async (videoId: number) => {
  try {
    const token = localStorage.getItem('token')
    await fetch(`http://localhost:8080/api/video/${videoId}/view`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
  } catch (error) {
    console.error('更新播放量失败', error)
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.video-page {
  min-height: 100vh;
  background: #0f0f0f;
  padding-bottom: 60px;
}

.comment-input-fixed {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.08);
  z-index: 100;
}

.comment-input-fixed .el-avatar {
  flex-shrink: 0;
}

.comment-input-fixed .fixed-input-box {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-input-fixed .fixed-input-box .el-textarea {
  flex: 1;
}

.comment-input-fixed .fixed-input-box .el-textarea__inner {
  border-radius: 16px;
  padding: 6px 10px;
}

.cancel-btn-fixed {
  color: #909399;
  border-color: #dcdfe6;
}

.cancel-btn-fixed:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.video-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  padding: 16px 24px;
  background: linear-gradient(to bottom, rgba(0,0,0,0.9), transparent);
}

.video-header :deep(.el-button) {
  color: #fff;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.video-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.main-video {
  width: 100%;
  max-width: 1000px;
  height: calc(100vh - 500px);
  min-height: 400px;
  object-fit: contain;
  background: #000;
}

.video-info {
  width: 100%;
  max-width: 1000px;
  padding: 32px;
  background: #fff;
  border-radius: 16px 16px 0 0;
  margin-top: -20px;
  position: relative;
}

.video-title {
  margin: 0 0 20px;
  font-size: 26px;
  color: #1a1a1a;
  font-weight: 700;
  line-height: 1.4;
}

.video-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-detail {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 16px;
  color: #1a1a1a;
  font-weight: 600;
}

.author-label {
  font-size: 12px;
  color: #ff6b6b;
  background: #fff0f0;
  padding: 2px 8px;
  border-radius: 4px;
  margin-top: 2px;
  display: inline-block;
}

.interaction-buttons {
  display: flex;
  gap: 12px;
}

.interaction-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 20px;
  background: #f5f5f5;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  color: #666;
}

.interaction-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.interaction-btn .btn-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
}

.interaction-btn .btn-icon svg {
  width: 20px;
  height: 20px;
}

.interaction-btn .btn-count {
  font-weight: 600;
  min-width: 20px;
}

.interaction-btn.like-btn:hover,
.interaction-btn.like-btn.active {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
  color: #fff;
}

.interaction-btn.like-btn.active {
  animation: pulse 0.3s ease;
}

.interaction-btn.favorite-btn:hover,
.interaction-btn.favorite-btn.active {
  background: linear-gradient(135deg, #ffd93d 0%, #ffec8b 100%);
  color: #8b6914;
}

.interaction-btn.favorite-btn.active {
  animation: pulse 0.3s ease;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.video-actions {
  display: flex;
  justify-content: center;
  gap: 24px;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  border: 2px solid #e8e8e8;
  border-radius: 25px;
  background: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 15px;
  color: #666;
  font-weight: 500;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.action-btn .action-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
}

.action-btn .action-icon svg {
  width: 24px;
  height: 24px;
}

.action-btn.like-action:hover,
.action-btn.like-action.active {
  border-color: #ff6b6b;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%);
  color: #ff6b6b;
}

.action-btn.like-action.active {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
  color: #fff;
  border-color: #ff6b6b;
  animation: bounce 0.4s ease;
}

.action-btn.favorite-action:hover,
.action-btn.favorite-action.active {
  border-color: #ffd93d;
  background: linear-gradient(135deg, #fffdf5 0%, #fff8e1 100%);
  color: #d4a800;
}

.action-btn.favorite-action.active {
  background: linear-gradient(135deg, #ffd93d 0%, #ffec8b 100%);
  color: #8b6914;
  border-color: #ffd93d;
  animation: bounce 0.4s ease;
}

@keyframes bounce {
  0% { transform: scale(1) translateY(0); }
  30% { transform: scale(1.15) translateY(-4px); }
  50% { transform: scale(0.95) translateY(2px); }
  70% { transform: scale(1.05) translateY(-2px); }
  100% { transform: scale(1) translateY(0); }
}

.stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 14px;
}

.video-description {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 24px;
}

.video-description h3 {
  margin: 0 0 12px;
  font-size: 15px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.video-description p {
  margin: 0;
  line-height: 1.8;
  font-size: 14px;
  color: #666;
}

.comment-section {
  margin-top: 8px;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.comment-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-count {
  color: #999;
  font-size: 14px;
}

.comment-input-inline {
  display: flex;
  gap: 12px;
  background: #f5f7fa;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.comment-input-inline .el-avatar {
  flex-shrink: 0;
}

.comment-input-inline .input-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.comment-input-inline :deep(.el-textarea__inner) {
  border-radius: 8px;
}

.comment-input-inline .input-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

:deep(.comment-wrapper) {
  margin-bottom: 0;
}

:deep(.comment-row) {
  display: flex;
  gap: 14px;
  padding: 18px 16px;
  background: #fff;
  border-radius: 12px;
  transition: all 0.25s ease;
  margin-bottom: 2px;
}

:deep(.comment-row:hover) {
  background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

:deep(.user-avatar-img) {
  flex-shrink: 0;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

:deep(.avatar-wrapper) {
  flex-shrink: 0;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

:deep(.avatar-letter) {
  color: #fff;
  font-weight: 700;
  font-size: 18px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

:deep(.comment-main) {
  flex: 1;
  min-width: 0;
}

:deep(.comment-header) {
  display: flex;
  align-items: center;
  gap: 0;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

:deep(.username) {
  font-size: 15px;
  color: #1a1a1a;
  font-weight: 700;
  letter-spacing: 0.3px;
}

:deep(.reply-arrow) {
  color: #999;
  font-size: 13px;
  margin: 0 6px;
}

:deep(.reply-to) {
  font-size: 14px;
  color: #409eff;
  background: linear-gradient(135deg, #e6f3ff 0%, #d6ecff 100%);
  padding: 2px 10px;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.2s;
}

:deep(.reply-to:hover) {
  background: linear-gradient(135deg, #d6ecff 0%, #b3daff 100%);
  color: #1976d2;
}

:deep(.time-dot) {
  color: #ddd;
  margin: 0 8px;
  font-size: 14px;
}

:deep(.time) {
  font-size: 13px;
  color: #999;
  font-weight: 400;
}

:deep(.comment-text) {
  margin: 0 0 14px;
  font-size: 14px;
  color: #333;
  line-height: 1.8;
  word-break: break-word;
  padding: 10px 14px;
  background: linear-gradient(135deg, #f8f9fa 0%, #f0f2f5 100%);
  border-radius: 10px;
  border-left: 3px solid #409eff;
}

:deep(.comment-footer) {
  display: flex;
  gap: 12px;
}

:deep(.footer-btn) {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  border: none;
  background: none;
  color: #888;
  font-size: 13px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
  font-weight: 500;
}

:deep(.footer-btn .icon) {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

:deep(.footer-btn:hover) {
  background: linear-gradient(135deg, #f0f4ff 0%, #e6eeff 100%);
  color: #409eff;
}

:deep(.footer-btn.delete:hover) {
  background: linear-gradient(135deg, #fff0f0 0%, #ffe6e6 100%);
  color: #f56c6c;
}

:deep(.reply-container) {
  margin-left: 54px;
  padding-left: 0;
  border-left: none;
  margin-top: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

:deep(.reply-container .comment-row) {
  background: linear-gradient(135deg, #fafbfc 0%, #f8f9fa 100%);
  padding: 14px 16px;
}

:deep(.reply-container .comment-text) {
  font-size: 13px;
  padding: 8px 12px;
}

:deep(.reply-container .avatar-wrapper) {
  width: 36px;
  height: 36px;
}

:deep(.reply-container .user-avatar-img) {
  width: 36px;
  height: 36px;
}

:deep(.reply-container .avatar-letter) {
  font-size: 15px;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  color: #fff;
}

.loading .el-icon {
  animation: rotating 1.5s linear infinite;
  margin-bottom: 16px;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

:deep(.el-divider) {
  margin: 24px 0;
}

:deep(.el-empty__description) {
  color: #999;
}

.load-more-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  margin-top: 10px;
}

.no-more-comments {
  text-align: center;
  padding: 20px 0;
  color: #999;
  font-size: 13px;
}
</style>
