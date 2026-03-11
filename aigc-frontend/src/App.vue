<template>
  <div id="app">
    <div class="bg-orb orb-1"></div>
    <div class="bg-orb orb-2"></div>
    <div class="bg-orb orb-3"></div>
    <div class="bg-grid"></div>
    <div class="bg-scanline"></div>
    <router-view />
  </div>
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  overflow-x: hidden;
}

#app {
  min-height: 100vh;
  position: relative;
  background: #05050a;
  overflow-y: auto;
  overflow-x: hidden;
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
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(255, 0, 128, 0.4) 0%, transparent 70%);
  top: -200px;
  left: -200px;
  animation-delay: 0s;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(0, 255, 255, 0.35) 0%, transparent 70%);
  top: 50%;
  right: -150px;
  animation-delay: -7s;
}

.orb-3 {
  width: 450px;
  height: 450px;
  background: radial-gradient(circle, rgba(255, 204, 0, 0.3) 0%, transparent 70%);
  bottom: -100px;
  left: 30%;
  animation-delay: -14s;
}

@keyframes floatOrb {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(30px, -30px) scale(1.1);
  }
  50% {
    transform: translate(-20px, 20px) scale(0.95);
  }
  75% {
    transform: translate(-30px, -20px) scale(1.05);
  }
}

.bg-grid {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(255, 0, 128, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
  z-index: 0;
}

.bg-scanline {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 2px,
    rgba(0, 0, 0, 0.1) 2px,
    rgba(0, 0, 0, 0.1) 4px
  );
  pointer-events: none;
  z-index: 1;
  opacity: 0.3;
  animation: scanline 8s linear infinite;
}

@keyframes scanline {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(4px);
  }
}

.page {
  position: relative;
  z-index: 2;
}

.glass-card {
  background: rgba(10, 10, 20, 0.7);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border: 1px solid rgba(255, 0, 128, 0.3);
  border-radius: 24px;
  position: relative;
  overflow: visible;
  box-shadow: 
    0 0 30px rgba(255, 0, 128, 0.1),
    0 0 60px rgba(0, 255, 255, 0.05),
    inset 0 0 60px rgba(255, 0, 128, 0.05);
}

.glass-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, #ff0080, #00ffff, transparent);
  animation: borderGlow 3s ease-in-out infinite;
}

@keyframes borderGlow {
  0%, 100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}

.glass-card::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 30%,
    rgba(255, 0, 128, 0.03) 50%,
    transparent 70%
  );
  animation: shimmer 4s infinite;
  pointer-events: none;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%) rotate(0deg);
  }
  100% {
    transform: translateX(100%) rotate(0deg);
  }
}

.van-dropdown-menu {
  z-index: 2000 !important;
}

.van-dropdown-menu__bar {
  font-size: calc(18px * var(--text-scale)) !important;
}

.van-dropdown-menu__title {
  font-size: calc(18px * var(--text-scale)) !important;
}

.van-dropdown-item {
  z-index: 2001 !important;
}

.van-dropdown-item__option {
  font-size: calc(16px * var(--text-scale)) !important;
}

.van-action-sheet {
  background: rgba(10, 10, 20, 0.98) !important;
  border-radius: 24px 24px 0 0 !important;
  border-top: 2px solid rgba(255, 0, 128, 0.5);
}

.van-action-sheet__item {
  background: transparent !important;
  color: rgba(255, 255, 255, 0.8) !important;
  font-size: calc(16px * var(--text-scale));
  padding: 16px;
}

.van-action-sheet__item:active {
  background: rgba(255, 0, 128, 0.2) !important;
}

.van-action-sheet__cancel {
  background: rgba(20, 20, 35, 0.9) !important;
  color: #00ffff !important;
  margin-top: 8px;
  border-top: 1px solid rgba(255, 0, 128, 0.3);
}

.van-popup {
  z-index: 3000 !important;
}
</style>
