<template>
  <div class="register-page">
    <div class="register-container">
      <el-card class="register-card">
        <template #header>
          <div class="register-header">
            <h2>用户注册</h2>
          </div>
        </template>
        <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="el-icon-user"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="el-icon-lock" show-password></el-input>
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="请确认密码" prefix-icon="el-icon-lock" show-password></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleRegister" style="width: 100%;">
              {{ loading ? '注册中...' : '注册' }}
            </el-button>
          </el-form-item>
          <el-form-item>
            <div class="login-link">
              已有账号？<router-link to="/login">立即登录</router-link>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElForm } from 'element-plus'

const router = useRouter()
const formRef = ref<InstanceType<typeof ElForm> | null>(null)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

const loading = ref(false)

const handleRegister = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      try {
        const response = await fetch('http://localhost:8080/api/auth/register', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            username: form.username,
            password: form.password
          })
        })
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        
        const data = await response.json()
        
        if (data.success) {
          localStorage.setItem('token', data.token)
          localStorage.setItem('user', JSON.stringify(data.user))
          ElMessage.success('注册成功')
          // 触发注册成功事件
          window.dispatchEvent(new Event('registerSuccess'))
          router.push('/')
        } else {
          ElMessage.error(data.message || '注册失败')
        }
      } catch (err) {
        if (err instanceof Error) {
          ElMessage.error(`注册失败: ${err.message}`)
        } else {
          ElMessage.error('注册失败，请稍后重试')
        }
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 70vh;
}

.register-container {
  width: 100%;
  max-width: 450px;
}

.register-card {
  border-radius: 8px;
  overflow: hidden;
}

.register-header {
  text-align: center;
  padding: 10px 0;
}

.register-header h2 {
  margin: 0;
  color: #409eff;
  font-size: 24px;
  font-weight: 600;
}

.login-link {
  text-align: center;
  margin-top: 10px;
  font-size: 14px;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>