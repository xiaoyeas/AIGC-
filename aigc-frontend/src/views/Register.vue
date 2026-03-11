<template>
  <div class="register-page page">
    <div class="bg-orb orb-1"></div>
    <div class="bg-orb orb-2"></div>
    <div class="bg-orb orb-3"></div>
    
    <div class="register-container glass-card">
      <div class="register-header">
        <div class="logo-container">
          <div class="logo-glow"></div>
          <div class="logo-icon">⚡</div>
          <h1 class="app-title">
            <span class="title-neon">AI 创意工坊</span>
          </h1>
          <p class="app-subtitle">创建您的账户</p>
        </div>
      </div>
      
      <div class="register-form">
        <van-form @submit="onSubmit">
          <van-cell-group inset>
            <van-field
              v-model="registerForm.username"
              name="username"
              label="用户名"
              placeholder="请输入用户名（3-20位）"
              :rules="[
                { required: true, message: '请输入用户名' },
                { validator: validateUsername, message: '用户名长度必须在3-20位之间' }
              ]"
              left-icon="user-o"
            />
            <van-field
              v-model="registerForm.password"
              type="password"
              name="password"
              label="密码"
              placeholder="请输入密码（6-20位）"
              :rules="[
                { required: true, message: '请输入密码' },
                { validator: validatePassword, message: '密码长度必须在6-20位之间' }
              ]"
              left-icon="lock-o"
            />
            <van-field
              v-model="registerForm.confirmPassword"
              type="password"
              name="confirmPassword"
              label="确认密码"
              placeholder="请再次输入密码"
              :rules="[
                { required: true, message: '请确认密码' },
                { validator: validateConfirmPassword, message: '两次输入的密码不一致' }
              ]"
              left-icon="lock-o"
            />
          </van-cell-group>
          
          <div class="register-actions">
            <van-button 
              round 
              block 
              type="primary" 
              native-type="submit"
              :loading="loading"
              class="register-btn"
            >
              注册
            </van-button>
            
            <div class="login-link">
              已有账户？
              <router-link to="/login" class="link-text">立即登录</router-link>
            </div>
          </div>
        </van-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { register } from '@/api/auth'

const router = useRouter()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateUsername = (val) => {
  return val && val.length >= 3 && val.length <= 20
}

const validatePassword = (val) => {
  return val && val.length >= 6 && val.length <= 20
}

const validateConfirmPassword = (val) => {
  return val === registerForm.password
}

const onSubmit = async () => {
  loading.value = true
  
  try {
    const response = await register({
      username: registerForm.username,
      password: registerForm.password
    })
    
    if (response.code === 200) {
      showToast({ message: '注册成功，请登录', type: 'success' })
      
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      showToast({ message: response.message || '注册失败', type: 'fail' })
    }
  } catch (error) {
    showToast({ message: '注册失败，请检查网络连接', type: 'fail' })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: #05050a;
  overflow: hidden;
}

.bg-orb {
  position: fixed;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
  z-index: 0;
  animation: floatOrb 20s ease-in-out infinite;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255, 0, 128, 0.3) 0%, transparent 70%);
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(0, 255, 255, 0.3) 0%, transparent 70%);
  top: 60%;
  right: 10%;
  animation-delay: 5s;
}

.orb-3 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(128, 0, 255, 0.3) 0%, transparent 70%);
  bottom: 20%;
  left: 20%;
  animation-delay: 10s;
}

.register-container {
  position: relative;
  z-index: 1;
  width: 60vw;
  min-height: 75vh;
  padding: 10px 70px 10px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 25px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.register-header {
  text-align: center;
  margin-bottom: 5px;
}

.logo-container {
  position: relative;
  display: inline-block;
}

.logo-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(255, 0, 128, 0.3) 0%, transparent 70%);
  filter: blur(30px);
  border-radius: 50%;
}

.logo-icon {
  font-size: 61px;
  margin-bottom: 2px;
  display: block;
}

.app-title {
  font-size: 61px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 2px;
}

.title-neon {
  background: linear-gradient(45deg, #ff00ff, #00ffff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.app-subtitle {
  color: rgba(255, 255, 255, 0.7);
  font-size: 35px;
}

.register-form {
  margin-top: 8px;
}

.register-actions {
  margin-top: 12px;
}

.register-btn {
  background: linear-gradient(45deg, #ff00ff, #00ffff);
  border: none;
  height: 120px;
  font-size: 61px;
  font-weight: 500;
}

.login-link {
  text-align: center;
  margin-top: 12px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 42px;
}

.link-text {
  color: #ff00ff;
  text-decoration: none;
  margin-left: 5px;
}

.link-text:hover {
  text-decoration: underline;
}

@keyframes floatOrb {
  0%, 100% {
    transform: translateY(0px) scale(1);
  }
  50% {
    transform: translateY(-20px) scale(1.1);
  }
}

:deep(.van-cell-group) {
  background: transparent;
  padding: 0 10px;
}

:deep(.van-cell) {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  margin-bottom: 5px;
  padding: 0;
}

:deep(.van-field) {
  font-size: 54px;
  padding: 40px 0;
}

:deep(.van-field__label) {
  font-size: 54px;
  color: rgba(255, 255, 255, 0.9);
  min-width: 220px;
}

:deep(.van-field__control) {
  font-size: 54px;
  color: #fff;
}

:deep(.van-field__control::placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

@media (max-width: 1200px) {
  .register-container {
    width: 70vw;
    min-height: 70vh;
    padding: 50px 50px 55px;
  }

  .logo-icon {
    font-size: 55px;
  }

  .app-title {
    font-size: 36px;
  }

  .app-subtitle {
    font-size: 20px;
  }

  .register-btn {
    height: 75px;
    font-size: 28px;
  }

  .login-link {
    font-size: 20px;
  }

  :deep(.van-field) {
    font-size: 24px;
    padding: 24px 0;
  }

  :deep(.van-field__label) {
    font-size: 24px;
  }

  :deep(.van-field__control) {
    font-size: 24px;
  }
}

@media (max-width: 768px) {
  .register-container {
    width: 85vw;
    min-height: 65vh;
    padding: 40px 35px 45px;
  }

  .logo-icon {
    font-size: 48px;
  }

  .app-title {
    font-size: 30px;
  }

  .app-subtitle {
    font-size: 18px;
  }

  .register-btn {
    height: 65px;
    font-size: 24px;
  }

  .login-link {
    font-size: 18px;
  }

  :deep(.van-field) {
    font-size: 22px;
    padding: 20px 0;
  }

  :deep(.van-field__label) {
    font-size: 22px;
  }

  :deep(.van-field__control) {
    font-size: 22px;
  }
}

@media (max-width: 480px) {
  .register-container {
    width: 92vw;
    min-height: 60vh;
    padding: 30px 25px 35px;
  }

  .logo-icon {
    font-size: 40px;
  }

  .app-title {
    font-size: 24px;
  }

  .app-subtitle {
    font-size: 15px;
  }

  .register-btn {
    height: 55px;
    font-size: 20px;
  }

  .login-link {
    font-size: 15px;
  }

  :deep(.van-field) {
    font-size: 18px;
    padding: 16px 0;
  }

  :deep(.van-field__label) {
    font-size: 18px;
  }

  :deep(.van-field__control) {
    font-size: 18px;
  }
}
</style>
