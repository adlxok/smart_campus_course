<template>
  <div class="login-container">
    <div class="login-box">
      <h1 class="title">后台管理系统</h1>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>用户名</label>
          <input 
            type="text" 
            v-model="username" 
            placeholder="请输入用户名"
            required
          />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input 
            type="password" 
            v-model="password" 
            placeholder="请输入密码"
            required
          />
        </div>
        <div class="error-message" v-if="errorMsg">{{ errorMsg }}</div>
        <button type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      <div class="admin-info">
        <p>超级管理员账号: admin</p>
        <p>密码: 123456</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../utils/api'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const username = ref('')
    const password = ref('')
    const errorMsg = ref('')
    const loading = ref(false)

    const handleLogin = async () => {
      errorMsg.value = ''
      loading.value = true
      
      try {
        const response = await api.post('/auth/login', {
          username: username.value,
          password: password.value
        })
        
        if (response.data.success) {
          localStorage.setItem('token', response.data.token)
          
          const roleResponse = await api.get('/role/user/info', {
            headers: { 'Authorization': `Bearer ${response.data.token}` }
          })
          
          if (roleResponse.data.success) {
            const userInfo = roleResponse.data.data
            const roleCode = userInfo.roleCode
            
            if (roleCode === 'SUPER_ADMIN' || roleCode === 'SYSTEM_ADMIN' || roleCode === 'DATA_ADMIN') {
              localStorage.setItem('user', JSON.stringify(userInfo))
              router.push('/dashboard')
            } else {
              errorMsg.value = '您没有访问后台管理系统的权限'
              localStorage.removeItem('token')
            }
          } else {
            errorMsg.value = '获取用户权限失败'
            localStorage.removeItem('token')
          }
        } else {
          errorMsg.value = response.data.message || '登录失败'
        }
      } catch (error) {
        errorMsg.value = error.response?.data?.message || '网络错误，请重试'
      } finally {
        loading.value = false
      }
    }

    return {
      username,
      password,
      errorMsg,
      loading,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  width: 400px;
}

.title {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.error-message {
  color: #e74c3c;
  margin-bottom: 15px;
  font-size: 14px;
  text-align: center;
}

button {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
  transition: opacity 0.3s;
}

button:hover:not(:disabled) {
  opacity: 0.9;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.admin-info {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 5px;
  font-size: 12px;
  color: #666;
  text-align: center;
}

.admin-info p {
  margin: 5px 0;
}
</style>
