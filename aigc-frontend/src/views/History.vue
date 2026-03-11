<template>
  <div class="history-page page">
    <div class="header-section">
      <div class="neon-border">
        <div class="logo-container">
          <div class="logo-glow"></div>
          <div class="logo-icon">📜</div>
          <h1 class="app-title">
            <span class="title-neon">创作历史</span>
          </h1>
          <p class="app-subtitle">回顾你的每一次灵感</p>
        </div>
      </div>
    </div>
    
    <div class="filter-section glass-card">
      <div class="filter-row">
        <div class="filter-box">
          <div class="filter-label">📊 数量</div>
          <div class="select-box" @click="showCountSelect = true">
            <span class="select-value">{{ getCountText() }}</span>
            <van-icon name="arrow-down" class="select-arrow" />
          </div>
        </div>
        <div class="filter-box">
          <div class="filter-label">🎨 类型</div>
          <div class="select-box" @click="showTypeSelect = true">
            <span class="select-value">{{ getTypeFilterText() }}</span>
            <van-icon name="arrow-down" class="select-arrow" />
          </div>
        </div>
      </div>
      
      <van-action-sheet
        :show="showCountSelect"
        :actions="countActions"
        close-on-click-action
        @select="onCountSelect"
        @close="showCountSelect = false"
      />
      
      <van-action-sheet
        :show="showTypeSelect"
        :actions="typeActions"
        close-on-click-action
        @select="onTypeSelect"
        @close="showTypeSelect = false"
      />
    </div>
    
    <div class="content-area">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          :loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div class="history-item glass-card" v-for="item in filteredList" :key="item.id">
            <div class="item-header">
              <div class="item-type-badge">
                <span class="type-icon">{{ getTypeIcon(item.generateType) }}</span>
                <span class="type-text">{{ getTypeText(item.generateType) }}</span>
              </div>
              <span class="item-time">{{ formatTime(item.createTime) }}</span>
            </div>
            
            <div class="item-content">
              <template v-if="item.generateType === 'img'">
                <div class="img-wrapper" @click="previewImage(item.imageUrl)">
                  <div class="thumbnail-border">
                    <van-image
                      :src="item.imageUrl"
                      width="120"
                      height="120"
                      radius="16"
                      fit="cover"
                      class="item-image"
                    />
                  </div>
                  <div class="img-overlay">
                    <van-icon name="search" size="32" />
                  </div>
                </div>
              </template>
              <template v-else>
                <div class="video-thumbnail" @click="previewVideo(item)">
                  <div class="thumbnail-border">
                    <video
                      :src="item.videoUrl"
                      width="120"
                      height="120"
                      class="item-video"
                      muted
                      :poster="item.coverUrl || item.imageUrl"
                    ></video>
                  </div>
                  <div class="play-overlay">
                    <div class="play-btn">
                      <van-icon name="play-circle-o" size="44" />
                    </div>
                  </div>
                </div>
              </template>
              
              <div class="item-info">
                <div class="item-prompt">{{ item.prompt }}</div>
                <div class="item-size">
                  {{ item.generateType === 'img' ? item.imageSize : '视频' }}
                </div>
              </div>
            </div>
            
            <div class="item-actions">
              <van-button class="action-btn reuse-btn" @click="useThisItem(item)">
                <span class="btn-icon">✨</span>
                再次创作
              </van-button>
              <van-button class="action-btn copy-btn" @click="copyPrompt(item)">
                <span class="btn-icon">📋</span>
                复制
              </van-button>
              <van-button class="action-btn download-btn" @click="downloadResult(item)">
                <span class="btn-icon">⬇️</span>
                下载
              </van-button>
              <van-button class="action-btn delete-btn" @click="handleDelete(item.id)">
                <span class="btn-icon">🗑️</span>
                删除
              </van-button>
            </div>
          </div>
        </van-list>
      </van-pull-refresh>
      
      <div v-if="filteredList.length === 0 && !loading" class="empty-state glass-card">
        <div class="empty-icon">✨</div>
        <div class="empty-text">还没有创作记录</div>
        <div class="empty-subtext">快去首页创作吧～</div>
      </div>
    </div>
    

    
    <van-dialog 
      :show="showVideoDialog" 
      class="video-dialog"
      :show-confirm-button="false"
      :close-on-click-overlay="true"
      @close="closeVideoDialog"
    >
      <div class="video-preview-container">
        <div class="video-preview-header">
          <span class="video-preview-title">视频预览</span>
          <van-button 
            class="close-btn" 
            icon="cross" 
            size="small" 
            @click="closeVideoDialog"
          />
        </div>
        <div class="video-wrapper">
          <video
            ref="videoRef"
            :src="currentVideoUrl"
            controls
            class="dialog-video"
            :poster="currentCoverUrl"
            playsinline
          ></video>
        </div>
        <div class="video-preview-footer">
          <van-button class="fullscreen-btn" @click="toggleFullscreen">
            <span class="btn-icon">⛶</span>
            全屏播放
          </van-button>
        </div>
      </div>
    </van-dialog>
    
    <van-tabbar v-model="activeTab" class="custom-tabbar">
      <van-tabbar-item name="home" icon="home-o" @click="goHome">
        <span>首页</span>
      </van-tabbar-item>
      <van-tabbar-item name="history" icon="clock-o">
        <span>历史</span>
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast, showSuccessToast, showImagePreview as vantShowImagePreview, showLoadingToast, closeToast } from 'vant'
import { getHistoryList, deleteHistory, downloadHistoryFile } from '@/api'

const router = useRouter()
const activeTab = ref('history')
const list = ref([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const countFilter = ref('all')
const typeFilter = ref('all')
const showCountSelect = ref(false)
const showTypeSelect = ref(false)
const showVideoDialog = ref(false)
const currentVideoUrl = ref('')
const currentCoverUrl = ref('')
const videoRef = ref(null)

const countActions = [
  { name: '全部', value: 'all' },
  { name: '最近 10 条', value: 'recent10' },
  { name: '最近 20 条', value: 'recent20' }
]

const typeActions = [
  { name: '全部', value: 'all' },
  { name: '文生图', value: 'img' },
  { name: '文生视频', value: 'text2video' },
  { name: '图生视频', value: 'img2video' }
]

const getCountText = () => {
  const action = countActions.find(a => a.value === countFilter.value)
  return action ? action.name : '全部'
}

const getTypeFilterText = () => {
  const action = typeActions.find(a => a.value === typeFilter.value)
  return action ? action.name : '全部'
}

const onCountSelect = (action) => {
  countFilter.value = action.value
  showCountSelect.value = false
}

const onTypeSelect = (action) => {
  typeFilter.value = action.value
  showTypeSelect.value = false
}

const filteredList = computed(() => {
  let result = list.value
  
  if (typeFilter.value !== 'all') {
    result = result.filter(item => item.generateType === typeFilter.value)
  }
  
  if (countFilter.value === 'recent10') {
    return result.slice(0, 10)
  }
  
  if (countFilter.value === 'recent20') {
    return result.slice(0, 20)
  }
  
  return result
})

const onLoad = async () => {
  try {
    const res = await getHistoryList(20)
    if (res.data) {
      list.value = res.data
      finished.value = true
    }
  } catch (error) {
    console.error('获取历史失败:', error)
  } finally {
    loading.value = false
  }
}

const onRefresh = async () => {
  finished.value = false
  list.value = []
  await onLoad()
  refreshing.value = false
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getTypeIcon = (type) => {
  const icons = {
    img: '🖼️',
    text2video: '📹',
    img2video: '🎞️'
  }
  return icons[type] || '📝'
}

const getTypeText = (type) => {
  const texts = {
    img: '文生图',
    text2video: '文生视频',
    img2video: '图生视频'
  }
  return texts[type] || '未知'
}

const previewImage = (url) => {
  vantShowImagePreview({
    images: [url],
    startPosition: 0,
    closeable: true,
    closeOnClickOverlay: true,
    onClose: () => {
      console.log('图片预览已关闭')
    }
  })
}

const previewVideo = (item) => {
  currentVideoUrl.value = item.videoUrl
  currentCoverUrl.value = item.coverUrl || item.imageUrl
  showVideoDialog.value = true
}

const closeVideoDialog = () => {
  if (videoRef.value) {
    videoRef.value.pause()
  }
  showVideoDialog.value = false
}

const toggleFullscreen = () => {
  if (!videoRef.value) return
  
  if (!document.fullscreenElement && !document.webkitFullscreenElement && !document.mozFullScreenElement && !document.msFullscreenElement) {
    if (videoRef.value.requestFullscreen) {
      videoRef.value.requestFullscreen()
    } else if (videoRef.value.webkitRequestFullscreen) {
      videoRef.value.webkitRequestFullscreen()
    } else if (videoRef.value.mozRequestFullScreen) {
      videoRef.value.mozRequestFullScreen()
    } else if (videoRef.value.msRequestFullscreen) {
      videoRef.value.msRequestFullscreen()
    }
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
    } else if (document.webkitExitFullscreen) {
      document.webkitExitFullscreen()
    } else if (document.mozCancelFullScreen) {
      document.mozCancelFullScreen()
    } else if (document.msExitFullscreen) {
      document.msExitFullscreen()
    }
  }
}

const copyPrompt = async (item) => {
  try {
    await navigator.clipboard.writeText(item.prompt)
    showSuccessToast('复制成功！')
  } catch (error) {
    showToast('复制失败')
  }
}

const downloadResult = async (item) => {
  // 显示加载提示
  showLoadingToast({
    message: '正在下载到本地...',
    forbidClick: true,
    duration: 0
  })
  
  try {
    // 调用后端接口下载并保存到本地
    const res = await downloadHistoryFile(item.id)
    
    if (res.code === 200) {
      // 更新本地路径
      item.localPath = res.data.localPath
      showSuccessToast('下载成功！已保存到本地')
    } else {
      showToast(res.message || '下载失败')
    }
  } catch (error) {
    console.error('下载失败:', error)
    showToast('下载失败，请重试')
  } finally {
    closeToast()
  }
}

const handleDelete = async (id) => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: '确定要删除这条记录吗？',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    
    await deleteHistory(id)
    list.value = list.value.filter(item => item.id !== id)
    showSuccessToast('删除成功！')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const useThisItem = (item) => {
  router.push({
    path: '/',
    query: {
      fromHistory: 'true',
      generateType: item.generateType,
      modelName: item.modelName,
      prompt: item.prompt,
      imageSize: item.imageSize || '',
      imageUrl: item.imageUrl || '',
      videoUrl: item.videoUrl || ''
    }
  })
}

const goHome = () => {
  router.push('/')
}

onMounted(() => {
  onLoad()
})
</script>

<style>
:root {
  --van-dialog-width: 80% !important;
}

/* 确保 Vant 图片预览正常工作 */
.van-image-preview__cover {
  cursor: pointer !important;
}

.van-image-preview__close-icon {
  font-size: 48px !important;
  z-index: 9999 !important;
  pointer-events: auto !important;
}

/* 确保遮罩层可以点击关闭 */
.van-image-preview__overlay {
  pointer-events: auto !important;
}

/* 确保整个预览容器可以接收点击事件 */
.van-image-preview {
  pointer-events: auto !important;
}
</style>

<style scoped>
/* ================================================
   【关键区域】页面整体布局 - 放大35%
   ================================================ */
.history-page {
  min-height: 100vh;
  padding-bottom: 108px; /* 放大35%：80px * 1.35 */
}

.header-section {
  padding: 54px 22px 32px; /* 放大35%：40*1.35, 16*1.35, 24*1.35 */
  text-align: center;
}

.neon-border {
  position: relative;
  display: inline-block;
}

.logo-container {
  color: white;
  position: relative;
  padding: 27px 54px; /* 放大35%：20*1.35, 40*1.35 */
}

.logo-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 270px; /* 放大35%：200px * 1.35 */
  height: 270px; /* 放大35%：200px * 1.35 */
  background: radial-gradient(circle, rgba(0, 255, 255, 0.3) 0%, transparent 70%);
  filter: blur(54px); /* 放大35%：40px * 1.35 */
  animation: logoGlow 2s ease-in-out infinite;
}

@keyframes logoGlow {
  0%, 100% {
    opacity: 0.6;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.2);
  }
}

.logo-icon {
  font-size: calc(90px * var(--text-scale));
  margin-bottom: 16px; /* 放大35%：12px * 1.35 */
  display: inline-block;
  animation: float 3s ease-in-out infinite;
  position: relative;
  z-index: 1;
  filter: drop-shadow(0 0 27px rgba(0, 255, 255, 0.6)); /* 放大35%：20px * 1.35 */
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(-2deg);
  }
  50% {
    transform: translateY(-16px) rotate(2deg); /* 放大35%：12px * 1.35 */
  }
}

.app-title {
  font-size: calc(38px * var(--text-scale));
  font-weight: 900;
  margin-bottom: 14px; /* 放大35%：10px * 1.35 */
  letter-spacing: 5px; /* 放大35%：4px * 1.35 */
  position: relative;
  z-index: 1;
}

.title-neon {
  background: linear-gradient(135deg, #00ffff 0%, #ff0080 50%, #ffcc00 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 
    0 0 14px rgba(0, 255, 255, 0.8), /* 放大35%：10px * 1.35 */
    0 0 27px rgba(0, 255, 255, 0.6), /* 放大35%：20px * 1.35 */
    0 0 54px rgba(0, 255, 255, 0.4), /* 放大35%：40px * 1.35 */
    0 0 108px rgba(255, 0, 128, 0.3); /* 放大35%：80px * 1.35 */
  animation: neonFlicker 3s ease-in-out infinite;
}

@keyframes neonFlicker {
  0%, 100% {
    filter: brightness(1);
  }
  50% {
    filter: brightness(1.2);
  }
}

.app-subtitle {
  font-size: calc(16px * var(--text-scale));
  opacity: 0.9;
  font-weight: 300;
  color: rgba(255, 255, 255, 0.8);
  letter-spacing: 3px; /* 放大35%：2px * 1.35 */
  position: relative;
  z-index: 1;
}

.filter-section {
  margin: 0 22px 27px; /* 放大35%：16*1.35, 20*1.35 */
  padding: 5px; /* 放大35%：4px * 1.35 */
  background: rgba(10, 10, 20, 0.7) !important;
}

.filter-row {
  display: flex;
  gap: 16px;
}

.filter-box {
  flex: 1;
}

.filter-label {
  font-size: calc(14px * var(--text-scale));
  font-weight: 700;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 11px; /* 放大35%：8px * 1.35 */
  letter-spacing: 1px;
}

.select-box {
  background: rgba(20, 20, 35, 0.8);
  border-radius: 19px; /* 放大35%：14px * 1.35 */
  border: 3px solid rgba(0, 255, 255, 0.3); /* 放大35%：2px * 1.35 */
  padding: 19px 22px; /* 放大35%：14*1.35, 16*1.35 */
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.select-box:active {
  transform: scale(0.98);
}

.select-value {
  font-size: calc(15px * var(--text-scale));
  font-weight: 600;
  color: #ff0080;
}

.select-arrow {
  color: #00ffff;
  font-size: calc(19px * var(--text-scale));
  transition: transform 0.3s ease;
}

.content-area {
  padding: 0 22px; /* 放大35%：16px * 1.35 */
}

.history-item,
.empty-state,
.filter-section {
  position: relative;
}

.history-item {
  padding: 32px; /* 放大35%：24px * 1.35 */
  margin-bottom: 27px; /* 放大35%：20px * 1.35 */
  background: rgba(10, 10, 20, 0.7) !important;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 27px; /* 放大35%：20px * 1.35 */
  flex-wrap: wrap;
  gap: 16px; /* 放大35%：12px * 1.35 */
}

.item-type-badge {
  display: flex;
  align-items: center;
  gap: 11px; /* 放大35%：8px * 1.35 */
  font-size: calc(14px * var(--text-scale));
  font-weight: 700;
  color: #00ffff;
  background: linear-gradient(135deg, rgba(0, 255, 255, 0.15) 0%, rgba(255, 0, 128, 0.15) 100%);
  padding: 14px 27px; /* 放大35%：10*1.35, 20*1.35 */
  border-radius: 38px; /* 放大35%：28px * 1.35 */
  border: 3px solid rgba(0, 255, 255, 0.4); /* 放大35%：2px * 1.35 */
  box-shadow: 0 0 27px rgba(0, 255, 255, 0.2); /* 放大35%：20px * 1.35 */
}

.type-icon {
  font-size: calc(20px * var(--text-scale));
}

.item-model {
  font-size: calc(15px * var(--text-scale));
  font-weight: 700;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 1px;
}

.item-time {
  font-size: calc(13px * var(--text-scale));
  color: rgba(255, 255, 255, 0.5);
}

.item-content {
  display: flex;
  gap: 27px; /* 放大35%：20px * 1.35 */
  margin-bottom: 27px; /* 放大35%：20px * 1.35 */
}

.img-wrapper {
  position: relative;
  flex-shrink: 0;
  cursor: pointer;
  border-radius: 27px; /* 放大35%：20px * 1.35 */
  overflow: hidden;
}

.thumbnail-border {
  border-radius: 27px; /* 放大35%：20px * 1.35 */
  overflow: hidden;
  padding: 4px; /* 放大35%：3px * 1.35 */
  background: linear-gradient(135deg, #ff0080, #00ffff, #ffcc00);
  animation: thumbnailGlow 2s ease-in-out infinite;
}

@keyframes thumbnailGlow {
  0%, 100% {
    box-shadow: 0 0 20px rgba(255, 0, 128, 0.2); /* 放大35%：15px * 1.35 */
  }
  50% {
    box-shadow: 0 0 34px rgba(0, 255, 255, 0.3); /* 放大35%：25px * 1.35 */
  }
}

.img-overlay {
  position: absolute;
  top: 4px; /* 放大35%：3px * 1.35 */
  left: 4px; /* 放大35%：3px * 1.35 */
  right: 4px; /* 放大35%：3px * 1.35 */
  bottom: 4px; /* 放大35%：3px * 1.35 */
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: white;
  border-radius: 22px; /* 放大35%：16px * 1.35 */
}

.img-wrapper:hover .img-overlay {
  opacity: 1;
}

.item-image {
  flex-shrink: 0;
  display: block;
  border-radius: 22px; /* 放大35%：16px * 1.35 */
}

.video-thumbnail {
  position: relative;
  flex-shrink: 0;
  cursor: pointer;
  border-radius: 27px; /* 放大35%：20px * 1.35 */
  overflow: hidden;
}

.item-video {
  border-radius: 22px; /* 放大35%：16px * 1.35 */
  object-fit: cover;
  display: block;
}

.play-overlay {
  position: absolute;
  top: 4px; /* 放大35%：3px * 1.35 */
  left: 4px; /* 放大35%：3px * 1.35 */
  right: 4px; /* 放大35%：3px * 1.35 */
  bottom: 4px; /* 放大35%：3px * 1.35 */
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 22px; /* 放大35%：16px * 1.35 */
}

.play-btn {
  width: 95px; /* 放大35%：70px * 1.35 */
  height: 95px; /* 放大35%：70px * 1.35 */
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(20px); /* 放大35%：15px * 1.35 */
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: all 0.3s ease;
  border: 3px solid rgba(255, 255, 255, 0.3); /* 放大35%：2px * 1.35 */
  box-shadow: 0 0 41px rgba(255, 0, 128, 0.3); /* 放大35%：30px * 1.35 */
}

.play-btn:hover {
  transform: scale(1.15);
  background: rgba(255, 255, 255, 0.35);
  box-shadow: 0 0 68px rgba(0, 255, 255, 0.4); /* 放大35%：50px * 1.35 */
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
}

.item-prompt {
  font-size: calc(15px * var(--text-scale));
  color: rgba(255, 255, 255, 0.95);
  line-height: 1.7;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  font-weight: 500;
}

.item-size {
  font-size: calc(13px * var(--text-scale));
  color: rgba(255, 255, 255, 0.6);
  margin-top: 14px; /* 放大35%：10px * 1.35 */
  letter-spacing: 1px;
}

.item-actions {
  display: flex;
  gap: 16px; /* 放大35%：12px * 1.35 */
}

.action-btn {
  flex: 1;
  height: 62px; /* 放大35%：46px * 1.35 */
  border-radius: 19px; /* 放大35%：14px * 1.35 */
  font-size: calc(14px * var(--text-scale));
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 11px; /* 放大35%：8px * 1.35 */
  border: 3px solid transparent; /* 放大35%：2px * 1.35 */
  transition: all 0.3s ease;
  letter-spacing: 1px;
}

.reuse-btn {
  background: linear-gradient(135deg, #00ffff 0%, #ff0080 100%);
  color: white;
  border: none;
  box-shadow: 0 0 27px rgba(0, 255, 255, 0.3); /* 放大35%：20px * 1.35 */
}

.copy-btn,
.download-btn {
  background: rgba(20, 20, 35, 0.7);
  color: rgba(255, 255, 255, 0.9);
  border-color: rgba(0, 255, 255, 0.4);
}

.delete-btn {
  background: rgba(255, 107, 107, 0.15);
  color: rgba(255, 107, 107, 0.95);
  border-color: rgba(255, 107, 107, 0.4);
}

.btn-icon {
  font-size: calc(18px * var(--text-scale));
}

.empty-state {
  padding: 135px 41px; /* 放大35%：100*1.35, 30*1.35 */
  text-align: center;
  margin-top: 27px; /* 放大35%：20px * 1.35 */
}

.empty-icon {
  font-size: calc(80px * var(--text-scale));
  margin-bottom: 32px; /* 放大35%：24px * 1.35 */
  display: inline-block;
  animation: float 3s ease-in-out infinite;
  filter: drop-shadow(0 0 27px rgba(255, 204, 0, 0.5)); /* 放大35%：20px * 1.35 */
}

.empty-text {
  font-size: calc(20px * var(--text-scale));
  color: rgba(255, 255, 255, 0.9);
  font-weight: 700;
  margin-bottom: 14px; /* 放大35%：10px * 1.35 */
  letter-spacing: 3px; /* 放大35%：2px * 1.35 */
}

.empty-subtext {
  font-size: calc(15px * var(--text-scale));
  color: rgba(255, 255, 255, 0.6);
  letter-spacing: 1px;
}

.video-dialog {
  width: 100%;
  max-width: none !important;
}

.video-dialog :deep(.van-dialog) {
  width: 80% !important;
  max-width: none !important;
  border-radius: 32px;
  background: rgba(10, 10, 20, 0.95);
  overflow: hidden;
}

.video-preview-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.video-preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.video-preview-title {
  font-size: 20px;
  font-weight: 700;
  color: white;
  letter-spacing: 2px;
}

.close-btn {
  background: linear-gradient(135deg, rgba(255, 0, 128, 0.4), rgba(0, 255, 255, 0.4));
  border: 2px solid rgba(255, 255, 255, 0.4);
  color: white;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-wrapper {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20px;
  overflow: hidden;
  background: #000;
}

.dialog-video {
  width: 100%;
  height: auto;
  max-height: 75vh;
  object-fit: contain;
  border-radius: 0;
}

.dialog-video:fullscreen,
.dialog-video:-webkit-full-screen,
.dialog-video:-moz-full-screen,
.dialog-video:-ms-fullscreen {
  width: 100vw !important;
  height: 100vh !important;
  max-height: none !important;
  object-fit: cover !important;
}

.video-preview-footer {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.fullscreen-btn {
  background: linear-gradient(135deg, #ff0080 0%, #00ffff 100%);
  border: none;
  color: white;
  font-size: 16px;
  font-weight: 700;
  padding: 14px 40px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 0 30px rgba(255, 0, 128, 0.4);
}

.btn-icon {
  font-size: 18px;
}

.custom-tabbar {
  background: rgba(8, 8, 15, 0.98);
  backdrop-filter: blur(54px); /* 放大35%：40px * 1.35 */
  -webkit-backdrop-filter: blur(54px); /* 放大35%：40px * 1.35 */
  border-top: 3px solid rgba(0, 255, 255, 0.4); /* 放大35%：2px * 1.35 */
  box-shadow: 0 -14px 54px rgba(0, 255, 255, 0.2); /* 放大35%：10*1.35, 40*1.35 */
}

.custom-tabbar :deep(.van-tabbar-item) {
  font-size: calc(14px * var(--text-scale));
  color: rgba(255, 255, 255, 0.5);
  font-weight: 600;
  letter-spacing: 1px;
}

.custom-tabbar :deep(.van-tabbar-item--active) {
  color: #00ffff;
  text-shadow: 0 0 27px rgba(0, 255, 255, 0.6); /* 放大35%：20px * 1.35 */
}

.custom-tabbar :deep(.van-tabbar-item__icon) {
  font-size: calc(24px * var(--text-scale));
}
</style>
