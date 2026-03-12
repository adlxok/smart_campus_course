<template>
  <div class="upload-page">
    <el-card shadow="hover" class="upload-card">
      <template #header>
        <div class="upload-header">
          <h2>上传视频</h2>
        </div>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="视频标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入视频标题" maxlength="100" show-word-limit></el-input>
        </el-form-item>
        
        <el-form-item label="视频描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入视频描述" :rows="4" maxlength="500" show-word-limit></el-input>
        </el-form-item>
        
        <el-form-item label="视频分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择视频分类" style="width: 100%;">
            <el-option label="动画" :value="2"></el-option>
            <el-option label="音乐" :value="3"></el-option>
            <el-option label="舞蹈" :value="4"></el-option>
            <el-option label="游戏" :value="5"></el-option>
            <el-option label="知识" :value="6"></el-option>
            <el-option label="科技" :value="7"></el-option>
            <el-option label="生活" :value="8"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="视频标签" prop="tags">
          <div class="tags-input-container">
            <el-tag
              v-for="tag in form.tags"
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
          <div class="tags-hint">最多添加5个标签，每个标签不超过10个字</div>
        </el-form-item>
        
        <el-form-item label="选择视频" prop="video">
          <el-upload
            class="video-uploader"
            :auto-upload="false"
            :show-file-list="false"
            accept="video/*"
            :limit="1"
            @change="handleVideoChange"
          >
            <el-button type="primary">
              <el-icon><VideoCamera /></el-icon>
              选择视频文件
            </el-button>
          </el-upload>
          <div v-if="form.video" class="video-info">
            <el-icon><VideoCamera /></el-icon>
            <span>{{ form.video.name }}</span>
          </div>
        </el-form-item>
        
        <el-form-item label="视频封面" prop="cover">
          <el-upload
            class="cover-uploader"
            :auto-upload="false"
            :show-file-list="false"
            accept="image/*"
            :limit="1"
            @change="handleCoverChange"
          >
            <el-avatar v-if="!form.coverUrl" :size="150" shape="square">
              <el-icon :size="50"><Picture /></el-icon>
            </el-avatar>
            <img v-else :src="form.coverUrl" class="cover-preview" />
          </el-upload>
          <div class="cover-tip">建议尺寸: 16:9, 支持 jpg、png 格式</div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="uploading" @click="submitUpload" style="width: 200px;">
            {{ uploading ? '上传中...' : '发布视频' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElForm } from 'element-plus'
import { VideoCamera, Picture } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref<InstanceType<typeof ElForm> | null>(null)

const form = reactive({
  title: '',
  description: '',
  categoryId: null as number | null,
  tags: [] as string[],
  video: null as File | null,
  cover: null as File | null,
  coverUrl: ''
})

const tagInput = ref('')

const rules = reactive({
  title: [
    { required: true, message: '请输入视频标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ]
})

const uploading = ref(false)

const handleVideoChange = (uploadFile: any) => {
  form.video = uploadFile.raw
}

const handleCoverChange = (uploadFile: any) => {
  form.cover = uploadFile.raw
  form.coverUrl = URL.createObjectURL(uploadFile.raw)
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
  if (form.tags.length >= 5) {
    ElMessage.warning('最多只能添加5个标签')
    return
  }
  if (form.tags.includes(tag)) {
    ElMessage.warning('标签已存在')
    return
  }
  form.tags.push(tag)
  tagInput.value = ''
}

const removeTag = (tag: string) => {
  form.tags = form.tags.filter(t => t !== tag)
}

const submitUpload = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (!form.video) {
        ElMessage.error('请选择视频文件')
        return
      }
      
      uploading.value = true
      
      try {
        const token = localStorage.getItem('token')
        const formData = new FormData()
        formData.append('title', form.title)
        formData.append('description', form.description)
        if (form.categoryId) {
          formData.append('categoryId', form.categoryId.toString())
        }
        if (form.video) {
          formData.append('video', form.video)
        }
        if (form.cover) {
          formData.append('cover', form.cover)
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
          if (form.tags.length > 0 && data.data && data.data.id) {
            await fetch(`http://localhost:8080/api/tag/video/${data.data.id}/tags`, {
              method: 'POST',
              headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
              },
              body: JSON.stringify(form.tags)
            })
          }
          ElMessage.success('视频上传成功')
          router.push('/')
        } else {
          ElMessage.error(data.message || '上传失败')
        }
      } catch (error) {
        ElMessage.error('上传失败，请重试')
      } finally {
        uploading.value = false
      }
    }
  })
}
</script>

<style scoped>
.upload-page {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 70vh;
  padding: 20px;
}

.upload-card {
  width: 100%;
  max-width: 700px;
  border-radius: 8px;
}

.upload-header {
  text-align: center;
}

.upload-header h2 {
  margin: 0;
  color: #409eff;
  font-size: 24px;
  font-weight: 600;
}

.video-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  color: #67c23a;
  font-size: 14px;
}

.cover-uploader {
  cursor: pointer;
}

.cover-preview {
  width: 150px;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
}

.cover-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
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
</style>
