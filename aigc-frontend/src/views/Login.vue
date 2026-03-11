<template>
  <div class="login-page page">
    <div class="bg-orb orb-1"></div>
    <div class="bg-orb orb-2"></div>
    <div class="bg-orb orb-3"></div>
    
    <div class="login-container glass-card">
      <div class="login-header">
        <div class="logo-container">
          <div class="logo-glow"></div>
          <div class="logo-icon">⚡</div>
          <h1 class="app-title">
            <span class="title-neon">AI 创意工坊</span>
          </h1>
          <p class="app-subtitle">欢迎回来</p>
        </div>
      </div>
      
      <div class="login-form">
        <van-form @submit="onSubmit">
          <van-cell-group inset>
            <van-field
              v-model="loginForm.username"
              name="username"
              label="用户名"
              placeholder="请输入用户名"
              :rules="[{ required: true, message: '请输入用户名' }]"
              left-icon="user-o"
            />
            <van-field
              v-model="loginForm.password"
              type="password"
              name="password"
              label="密码"
              placeholder="请输入密码"
              :rules="[{ required: true, message: '请输入密码' }]"
              left-icon="lock-o"
            />
          </van-cell-group>
          
          <div class="login-actions">
            <van-button 
              round 
              block 
              type="primary" 
              native-type="submit"
              :loading="loading"
              class="login-btn"
            >
              登录
            </van-button>
            
            <div class="register-link">
              还没有账户？
              <router-link to="/register" class="link-text">立即注册</router-link>
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
import { showToast, showSuccessToast } from 'vant'
import { login } from '@/api/auth'
import { setToken, setUserInfo } from '@/utils/auth'

const router = useRouter()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const onSubmit = async () => {
  loading.value = true
  
  try {
    const response = await login({
      username: loginForm.username,
      password: loginForm.password
    })
    
    if (response.code === 200) {
      setToken(response.data.token)
      setUserInfo(response.data.userInfo)
      showSuccessToast('登录成功！')
      
      setTimeout(() => {
        router.push('/')
      }, 1500)
    } else {
      showToast(response.message || '登录失败')
    }
  } catch (error) {
    showToast('登录失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: #05050a;
  overflow: hidden;
  padding: 0;
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

.login-container {
  position: relative;
  z-index: 1;
  width: 62vw;
  min-height: auto;
  padding: 20px 70px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 25px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-header {
  text-align: center;
  margin-bottom: 12px;
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
  width: 112px;
  height: 112px;
  background: radial-gradient(circle, rgba(255, 0, 128, 0.3) 0%, transparent 70%);
  filter: blur(34px);
  border-radius: 50%;
}

.logo-icon {
  font-size: 67px;
  margin-bottom: 4px;
  display: block;
}

.app-title {
  font-size: 67px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 4px;
}

.title-neon {
  background: linear-gradient(45deg, #ff00ff, #00ffff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.app-subtitle {
  color: rgba(255, 255, 255, 0.7);
  font-size: 36px;
}

.login-form {
  margin-top: 18px;
}

.login-actions {
  margin-top: 27px;
}

.login-btn {
  background: linear-gradient(45deg, #ff00ff, #00ffff);
  border: none;
  height: 134px;
  font-size: 69px;
  font-weight: 500;
}

.register-link {
  text-align: center;
  margin-top: 27px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 43px;
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
  margin-bottom: 20px;
  padding: 0;
}

:deep(.van-field) {
  font-size: 60px;
  padding: 45px 0;
}

:deep(.van-field__label) {
  font-size: 60px;
  color: rgba(255, 255, 255, 0.9);
  min-width: 224px;
}

:deep(.van-field__control) {
  font-size: 60px;
  color: #fff;
}

:deep(.van-field__control::placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

@media (max-width: 1200px) {
  .login-container {
    width: 70vw;
    min-height: auto;
    padding: 15px 50px;
  }

  .logo-glow {
    width: 80px;
    height: 80px;
  }

  .logo-icon {
    font-size: 46px;
  }

  .app-title {
    font-size: 46px;
  }

  .app-subtitle {
    font-size: 25px;
  }

  .login-btn {
    height: 93px;
    font-size: 48px;
  }

  .register-link {
    font-size: 30px;
  }

  :deep(.van-field) {
    font-size: 42px;
    padding: 31px 0;
  }

  :deep(.van-field__label) {
    font-size: 42px;
    min-width: 157px;
  }

  :deep(.van-field__control) {
    font-size: 42px;
  }
}

@media (max-width: 768px) {
  .login-container {
    width: 85vw;
    min-height: auto;
    padding: 12px 30px;
  }

  .logo-glow {
    width: 60px;
    height: 60px;
  }

  .logo-icon {
    font-size: 35px;
  }

  .app-title {
    font-size: 35px;
  }

  .app-subtitle {
    font-size: 19px;
  }

  .login-btn {
    height: 70px;
    font-size: 36px;
  }

  .register-link {
    font-size: 22px;
  }

  :deep(.van-field) {
    font-size: 32px;
    padding: 24px 0;
  }

  :deep(.van-field__label) {
    font-size: 32px;
    min-width: 119px;
  }

  :deep(.van-field__control) {
    font-size: 32px;
  }
}

@media (max-width: 480px) {
  .login-container {
    width: 90vw;
    min-height: auto;
    padding: 10px 20px;
  }

  .logo-glow {
    width: 40px;
    height: 40px;
  }

  .logo-icon {
    font-size: 23px;
  }

  .app-title {
    font-size: 23px;
  }

  .app-subtitle {
    font-size: 12px;
  }

  .login-btn {
    height: 47px;
    font-size: 24px;
  }

  .register-link {
    font-size: 15px;
  }

  :deep(.van-field) {
    font-size: 21px;
    padding: 16px 0;
  }

  :deep(.van-field__label) {
    font-size: 21px;
    min-width: 78px;
  }

  :deep(.van-field__control) {
    font-size: 21px;
  }
}
</style>
