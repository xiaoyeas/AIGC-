<template>
  <div class="not-found-page page">
    <div class="bg-orb orb-1"></div>
    <div class="bg-orb orb-2"></div>
    <div class="bg-orb orb-3"></div>
    
    <div class="not-found-container glass-card">
      <div class="error-code">404</div>
      <div class="error-title">页面未找到</div>
      <div class="error-description">抱歉，您访问的页面不存在</div>
      
      <div class="action-buttons">
        <van-button 
          round 
          type="primary" 
          @click="goHome"
          class="home-btn"
        >
          返回首页
        </van-button>
        <van-button 
          round 
          @click="goBack"
          class="back-btn"
        >
          返回上页
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.not-found-page {
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
  background: radial-gradient(circle, rgba(255, 0, 0, 0.3) 0%, transparent 70%);
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255, 165, 0, 0.3) 0%, transparent 70%);
  top: 60%;
  right: 10%;
  animation-delay: 5s;
}

.orb-3 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255, 255, 0, 0.3) 0%, transparent 70%);
  bottom: 20%;
  left: 20%;
  animation-delay: 10s;
}

.not-found-container {
  position: relative;
  z-index: 1;
  width: 90%;
  max-width: 400px;
  padding: 40px 30px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  text-align: center;
}

.error-code {
  font-size: 80px;
  font-weight: 700;
  background: linear-gradient(45deg, #ff0000, #ffa500);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 10px;
}

.error-title {
  font-size: 24px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 10px;
}

.error-description {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
  margin-bottom: 30px;
}

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.home-btn {
  background: linear-gradient(45deg, #ff0000, #ffa500);
  border: none;
  min-width: 120px;
}

.back-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  min-width: 120px;
}

@keyframes floatOrb {
  0%, 100% {
    transform: translateY(0px) scale(1);
  }
  50% {
    transform: translateY(-20px) scale(1.1);
  }
}
</style>