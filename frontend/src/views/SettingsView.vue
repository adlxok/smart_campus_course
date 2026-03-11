<template>
  <div class="settings">
    <el-card shadow="hover" class="settings-card">
      <template #header>
        <div class="settings-header">
          <h2>个人设置</h2>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" type="card" class="settings-tabs">
        <el-tab-pane label="修改密码" name="password">
          <div class="tab-content">
            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px" class="password-form">
              <el-form-item label="当前密码" prop="currentPassword">
                <el-input v-model="passwordForm.currentPassword" type="password" placeholder="请输入当前密码" prefix-icon="el-icon-lock" show-password style="width: 300px;"></el-input>
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" prefix-icon="el-icon-lock" show-password style="width: 300px;"></el-input>
              </el-form-item>
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" prefix-icon="el-icon-lock" show-password style="width: 300px;"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="changePassword">修改密码</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="修改头像" name="avatar">
          <div class="tab-content avatar-content">
            <div class="avatar-preview">
              <el-avatar :size="150" :src="previewUrl || userInfo.avatar || 'http://localhost:8080/api/avatar/default'"></el-avatar>
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
            <p class="avatar-tip">支持 jpg、png、gif 格式，建议上传正方形图片</p>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElForm } from 'element-plus';
import { Upload, Check } from '@element-plus/icons-vue';

interface UserInfo {
  id: number;
  username: string;
  avatar: string;
  role: string;
}

const userInfo = ref<UserInfo>({
  id: 0,
  username: '',
  avatar: '',
  role: ''
});

const activeTab = ref('password');
const passwordFormRef = ref<InstanceType<typeof ElForm> | null>(null);

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
};

const passwordRules = reactive({
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
});

const avatarFile = ref<File | null>(null);
const previewUrl = ref('');

onMounted(() => {
  const storedUser = localStorage.getItem('user');
  if (storedUser) {
    userInfo.value = JSON.parse(storedUser);
  }
});

const handleFileChange = (uploadFile: any) => {
  console.log('handleFileChange called:', uploadFile);
  const file = uploadFile.raw;
  avatarFile.value = file;
  // 创建预览URL
  previewUrl.value = URL.createObjectURL(file);
  console.log('previewUrl:', previewUrl.value);
};

const changePassword = async () => {
  if (!passwordFormRef.value) return;
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch('http://localhost:8080/api/auth/change-password', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Authorization': `Bearer ${token}`
          },
          body: new URLSearchParams({
            username: userInfo.value.username,
            currentPassword: passwordForm.currentPassword,
            newPassword: passwordForm.newPassword
          })
        });
        
        const data = await response.json();
        if (data.success) {
          ElMessage.success(data.message);
          passwordForm.currentPassword = '';
          passwordForm.newPassword = '';
          passwordForm.confirmPassword = '';
        } else {
          ElMessage.error(data.message);
        }
      } catch (error) {
        ElMessage.error('修改密码失败，请重试');
      }
    }
  });
};

const submitAvatar = async () => {
  if (!avatarFile.value) {
    ElMessage.error('请选择要上传的头像');
    return;
  }
  
  try {
    const token = localStorage.getItem('token');
    const formData = new FormData();
    formData.append('avatar', avatarFile.value);
    formData.append('username', userInfo.value.username);
    
    const response = await fetch('http://localhost:8080/api/auth/change-avatar', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    });
    
    const data = await response.json();
    if (data.success) {
      ElMessage.success(data.message);
      userInfo.value.avatar = data.user.avatar;
      localStorage.setItem('user', JSON.stringify(userInfo.value));
      // 触发登录成功事件，通知App.vue更新用户状态
      window.dispatchEvent(new Event('loginSuccess'));
    } else {
      ElMessage.error(data.message);
    }
  } catch (error) {
    ElMessage.error('修改头像失败，请重试');
  }
};
</script>

<style scoped>
.settings {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 70vh;
}

.settings-card {
  width: 100%;
  max-width: 600px;
  border-radius: 8px;
}

.settings-header {
  text-align: center;
}

.settings-header h2 {
  margin: 0;
  color: #409eff;
  font-size: 24px;
  font-weight: 600;
}

.settings-tabs {
  margin-top: 20px;
}

.tab-content {
  padding: 30px 20px;
}

.password-form {
  max-width: 450px;
  margin: 0 auto;
}

.avatar-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-preview {
  margin-bottom: 30px;
}

.avatar-actions {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.avatar-tip {
  color: #909399;
  font-size: 12px;
  margin: 0;
}
</style>