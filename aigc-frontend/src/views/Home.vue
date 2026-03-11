<template>
  <div class="home-page page">
    <div class="settings-btn" @click="openConfigPanel">
      <van-icon name="setting-o" size="28" />
    </div>
    
    <div class="header-section">
      <div class="neon-border">
        <div class="logo-container">
          <div class="logo-glow"></div>
          <div class="logo-icon">⚡</div>
          <h1 class="app-title">
            <span class="title-neon">AI 创意工坊</span>
          </h1>
          <p class="app-subtitle">用想象力创造无限可能</p>
        </div>
      </div>
    </div>
    
    <div class="content-area">
      <div class="input-section glass-card">
        <div class="card-header">
          <div class="header-line"></div>
          <span class="section-title">
            <span class="title-icon">🎨</span>
            开始创作
          </span>
          <div class="header-line"></div>
        </div>
        
        <div class="type-selector">
          <div class="type-buttons">
            <van-button
              :class="['type-btn', { active: generateType === 'img' }]"
              @click="generateType = 'img'; onTypeChange()"
            >
              <span class="btn-emoji">🖼️</span>
              <span class="btn-text">文生图</span>
              <div class="btn-glow"></div>
            </van-button>
            <van-button
              :class="['type-btn', { active: generateType === 'text2video' }]"
              @click="generateType = 'text2video'; onTypeChange()"
            >
              <span class="btn-emoji">📹</span>
              <span class="btn-text">文生视频</span>
              <div class="btn-glow"></div>
            </van-button>
            <van-button
              :class="['type-btn', { active: generateType === 'img2video' }]"
              @click="generateType = 'img2video'; onTypeChange()"
            >
              <span class="btn-emoji">🎞️</span>
              <span class="btn-text">图生视频</span>
              <div class="btn-glow"></div>
            </van-button>
          </div>
        </div>
        
        <div class="model-selector" v-if="availableModels.length > 0">
          <div class="model-label">🤖 选择模型</div>
          <div class="model-select-box" @click="showModelSelect = true">
            <span class="model-value">{{ modelName || '请选择模型' }}</span>
            <van-icon name="arrow-down" class="model-arrow" />
          </div>
          
          <van-action-sheet
            :show="showModelSelect"
            :actions="modelActions"
            close-on-click-action
            @select="onModelSelect"
            @close="showModelSelect = false"
          />
        </div>
        
        <div class="upload-section" v-if="generateType === 'img2video'">
          <van-uploader
            v-model="fileList"
            :max-count="1"
            accept="image/*"
            :after-read="afterRead"
            :before-delete="beforeDelete"
            :preview-full-image="true"
            :deletable="true"
            class="uploader"
          >
            <div class="upload-box">
              <div class="upload-inner">
                <div class="upload-icon-box">
                  <van-icon name="plus" size="40" />
                </div>
                <div class="upload-text">上传参考图片</div>
                <div class="upload-hint">支持 JPG、PNG、WEBP</div>
              </div>
            </div>
          </van-uploader>
        </div>
        
        <div class="prompt-wrapper">
          <div class="prompt-label">💭 描述你的创意</div>
          <van-field
            v-model="prompt"
            type="textarea"
            :placeholder="getPromptPlaceholder()"
            :rows="4"
            maxlength="500"
            show-word-limit
            class="prompt-field"
          />
        </div>
        
        <div class="size-selector" v-if="generateType === 'img'">
          <div class="size-label">📐 选择尺寸</div>
          <div class="size-buttons">
            <van-button
              :class="['size-btn', { active: imageSize === '1024x1024' }]"
              @click="imageSize = '1024x1024'"
            >
              1:1
            </van-button>
            <van-button
              :class="['size-btn', { active: imageSize === '1024x1792' }]"
              @click="imageSize = '1024x1792'"
            >
              竖版
            </van-button>
            <van-button
              :class="['size-btn', { active: imageSize === '1792x1024' }]"
              @click="imageSize = '1792x1024'"
            >
              横版
            </van-button>
          </div>
        </div>
      </div>
      
      <div class="result-section glass-card" v-if="hasResult">
        <div class="card-header">
          <div class="header-line"></div>
          <span class="section-title">
            <span class="title-icon">{{ generateType === 'img' ? '🖼️' : '🎬' }}</span>
            生成结果
          </span>
          <div class="header-line"></div>
        </div>
        
        <div class="result-wrapper">
          <div class="image-container" v-if="generateType === 'img' && generatedImage">
            <div class="result-border" @click="previewResultImage">
              <img
                :src="generatedImage"
                class="result-image"
                alt="生成图片"
              />
              <div class="preview-hint">点击预览</div>
            </div>
          </div>
          
          <div class="video-container" v-if="generateType !== 'img' && generatedVideo">
            <div class="result-border" @click="previewResultVideo">
              <video
                :src="generatedVideo"
                class="result-video"
                :poster="generatedCover"
                playsinline
                preload="metadata"
              ></video>
              <div class="preview-hint">点击预览</div>
            </div>
          </div>
        </div>
        
        <div class="action-buttons">
          <van-button class="action-btn save-btn" @click="saveToHistory" :loading="saving">
            <span class="btn-icon">💾</span>
            保存到历史
          </van-button>
          <van-button class="action-btn copy-btn" @click="copyPrompt">
            <span class="btn-icon">📋</span>
            复制提示词
          </van-button>
          <van-button class="action-btn download-btn" @click="downloadResult" v-if="hasResult">
            <span class="btn-icon">⬇️</span>
            下载结果
          </van-button>
        </div>
      </div>
      
      <div class="bottom-action-bar">
        <van-button
          :class="['generate-btn', { glowing: !loading && canGenerate }]"
          round
          block
          size="large"
          :loading="loading"
          :disabled="!canGenerate || loading"
          @click="handleGenerate"
        >
          <template #icon>
            <span v-if="!loading">🚀</span>
          </template>
          {{ loading ? '正在创作中...' : '开始生成' }}
        </van-button>
      </div>
    </div>
    
    <van-tabbar v-model="activeTab" class="custom-tabbar">
      <van-tabbar-item name="home" icon="home-o">
        <span>首页</span>
      </van-tabbar-item>
      <van-tabbar-item name="history" icon="clock-o" @click="goHistory">
        <span>历史</span>
      </van-tabbar-item>
    </van-tabbar>
    
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
      </div>
    </van-dialog>
    
    <van-dialog
      :show="showConfigPanel"
      @update:show="val => showConfigPanel = val"
      title="系统设置"
      show-cancel-button
      show-confirm-button
      confirm-button-text="保存配置"
      cancel-button-text="取消"
      :style="{ width: '90%', maxWidth: '500px', '--van-dialog-width': '90%', '--van-dialog-max-width': '500px' }"
      @confirm="handleSaveConfig"
    >
      <div class="config-panel-content">
        <div class="config-section">
          <div class="section-title">
            <span class="title-icon">🤖</span>
            模型配置
          </div>
          
          <div class="config-group">
            <div class="config-item">
              <label class="config-label">文生图 API 地址</label>
              <van-field
                v-model="configForm.imageApiUrl"
                placeholder="请输入文生图 API 地址"
                class="config-field"
              />
            </div>
            
            <div class="config-item">
              <label class="config-label">文生图 API Key</label>
              <van-field
                v-model="configForm.imageApiKey"
                :type="showImageKey ? 'text' : 'password'"
                placeholder="请输入文生图 API Key"
                class="config-field"
              >
                <template #button>
                  <van-icon
                    :name="showImageKey ? 'eye' : 'eye-o'"
                    size="20"
                    @click.stop="showImageKey = !showImageKey"
                  />
                </template>
              </van-field>
            </div>
            
            <div class="config-item">
              <label class="config-label">文生图模型名称</label>
              <van-field
                v-model="configForm.imageModelName"
                placeholder="请输入文生图模型名称"
                class="config-field"
              />
            </div>
          </div>
          
          <div class="config-group">
            <div class="config-item">
              <label class="config-label">视频生成 API 地址</label>
              <van-field
                v-model="configForm.videoApiUrl"
                placeholder="请输入视频生成 API 地址"
                class="config-field"
              />
            </div>
            
            <div class="config-item">
              <label class="config-label">视频生成 API Key</label>
              <van-field
                v-model="configForm.videoApiKey"
                :type="showVideoKey ? 'text' : 'password'"
                placeholder="请输入视频生成 API Key"
                class="config-field"
              >
                <template #button>
                  <van-icon
                    :name="showVideoKey ? 'eye' : 'eye-o'"
                    size="20"
                    @click.stop="showVideoKey = !showVideoKey"
                  />
                </template>
              </van-field>
            </div>
            
            <div class="config-item">
              <label class="config-label">视频生成模型名称</label>
              <van-field
                v-model="configForm.videoModelName"
                placeholder="请输入视频生成模型名称"
                class="config-field"
              />
            </div>
          </div>
        </div>
        
        <div class="config-section">
          <div class="section-title">
            <span class="title-icon">🌐</span>
            网络配置
          </div>
          
          <div class="config-group">
            <div class="config-item">
              <label class="config-label">前端内网穿透地址</label>
              <van-field
                v-model="configForm.frontendTunnelUrl"
                placeholder="请输入前端内网穿透地址"
                class="config-field"
              />
            </div>
            
            <div class="config-item">
              <label class="config-label">后端内网穿透地址</label>
              <van-field
                v-model="configForm.backendTunnelUrl"
                placeholder="请输入后端内网穿透地址"
                class="config-field"
              />
            </div>
          </div>
        </div>
        
        <!-- 退出登录按钮 -->
        <div class="logout-section">
          <div class="section-divider"></div>
          <div class="logout-container">
            <van-button 
              round 
              block 
              class="logout-btn"
              @click="handleLogout"
            >
              <span class="logout-icon">🚪</span>
              退出登录
            </van-button>
          </div>
        </div>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showSuccessToast, showImagePreview } from 'vant'
import { generateImage, generateImageAsync, getTaskStatus, saveHistory, uploadImage, getModels, getConfigMap, batchUpdateConfig, downloadDirect } from '@/api'
import { setConfigCache, loadConfigFromLocal } from '@/utils/config'
import { getUserInfo, logout } from '@/utils/auth'
import { getUserConfig } from '@/api/auth'

const router = useRouter()
const route = useRoute()

const generateType = ref('img')
const modelName = ref('')
const prompt = ref('')
const imageSize = ref('1024x1024')
const loading = ref(false)
const saving = ref(false)
const generatedImage = ref('')
const generatedVideo = ref('')
const generatedCover = ref('')
const activeTab = ref('home')
const lastGenerateResult = ref(null)
const fileList = ref([])
const uploadedImgUrl = ref('')
const availableModels = ref([])
const showModelSelect = ref(false)

const showConfigPanel = ref(false)
const showImageKey = ref(false)
const showVideoKey = ref(false)
const savingConfig = ref(false)

const showVideoDialog = ref(false)
const currentVideoUrl = ref('')
const currentCoverUrl = ref('')
const videoRef = ref(null)
const uploadedImgLocalPath = ref('')
const downloadedLocalPath = ref('')

const configForm = ref({
  imageApiUrl: '',
  imageApiKey: '',
  imageModelName: '',
  videoApiUrl: '',
  videoApiKey: '',
  videoModelName: '',
  frontendTunnelUrl: '',
  backendTunnelUrl: ''
})

const openConfigPanel = () => {
  console.log('打开配置面板被调用')
  console.log('showConfigPanel 当前值:', showConfigPanel.value)
  showConfigPanel.value = true
  console.log('showConfigPanel 设置后:', showConfigPanel.value)
}

const hasResult = computed(() => {
  return (generateType.value === 'img' && generatedImage.value) ||
         (generateType.value !== 'img' && generatedVideo.value)
})

const canGenerate = computed(() => {
  if (!prompt.value.trim()) return false
  if (generateType.value === 'img2video' && !uploadedImgUrl.value) return false
  return true
})

const modelActions = computed(() => {
  return availableModels.value.map(model => ({
    name: model,
    value: model
  }))
})

const onModelSelect = (action) => {
  modelName.value = action.value
  showModelSelect.value = false
}

const getPromptPlaceholder = () => {
  switch (generateType.value) {
    case 'img':
      return '描述你想要的图片... 例如：一只可爱的柯基在彩虹下奔跑'
    case 'text2video':
      return '描述你想要的视频... 例如：一片飘落的樱花，微风轻拂'
    case 'img2video':
      return '描述视频的动作... 例如：让图片中的花朵慢慢绽放'
    default:
      return '描述你想要的内容...'
  }
}

const onTypeChange = async () => {
  generatedImage.value = ''
  generatedVideo.value = ''
  generatedCover.value = ''
  lastGenerateResult.value = null
  uploadedImgUrl.value = ''
  uploadedImgLocalPath.value = ''
  fileList.value = []
  await loadModels()
}

const loadModels = async () => {
  try {
    const res = await getModels(generateType.value)
    if (res.data && res.data.models) {
      availableModels.value = res.data.models
      if (res.data.defaultModel) {
        modelName.value = res.data.defaultModel
      } else if (availableModels.value.length > 0) {
        modelName.value = availableModels.value[0]
      }
    }
  } catch (error) {
    console.error('加载模型失败:', error)
  }
}

const afterRead = async (file) => {
  try {
    const res = await uploadImage(file.file)
    if (res.data && res.data.imgUrl) {
      uploadedImgUrl.value = res.data.imgUrl
      uploadedImgLocalPath.value = res.data.localPath || ''
      showSuccessToast('图片上传成功！')
    } else {
      showToast('图片上传失败，请重试')
      fileList.value = []
    }
  } catch (error) {
    console.error('上传失败:', error)
    showToast('图片上传失败，请重试')
    fileList.value = []
  }
}

const beforeDelete = () => {
  uploadedImgUrl.value = ''
  uploadedImgLocalPath.value = ''
  return true
}

const handleGenerate = async () => {
  if (!prompt.value.trim()) {
    showToast('请输入你的创意描述～')
    return
  }
  
  if (generateType.value === 'img2video' && !uploadedImgUrl.value) {
    showToast('请先上传参考图片～')
    return
  }
  
  loading.value = true
  try {
    const requestData = {
      prompt: prompt.value,
      generateType: generateType.value,
      modelName: modelName.value
    }
    
    if (generateType.value === 'img') {
      requestData.imageSize = imageSize.value
    }
    
    if (generateType.value === 'img2video') {
      requestData.imgUrl = uploadedImgUrl.value
      requestData.imgLocalPath = uploadedImgLocalPath.value
    }
    
    if (generateType.value === 'img') {
      const res = await generateImage(requestData)
      console.log('后端返回完整响应:', res)
      console.log('res.data:', res.data)
      
      if (res.data && res.data.success) {
        generatedImage.value = res.data.imageUrl
        generatedCover.value = res.data.coverUrl || res.data.imageUrl
        console.log('设置图片结果:', { generatedImage: generatedImage.value, generatedCover: generatedCover.value })
        lastGenerateResult.value = res.data
        console.log('hasResult 计算结果:', hasResult.value)
        showSuccessToast('哇！生成成功啦 ✨')
      } else {
        showToast(res.data?.message || '生成失败了，再试一次吧')
      }
    } else {
      const asyncRes = await generateImageAsync(requestData)
      console.log('异步任务创建成功:', asyncRes)
      const taskId = asyncRes.data.taskId
      
      await pollTaskStatus(taskId)
    }
  } catch (error) {
    console.error('生成失败:', error)
    showToast('网络有点小问题，稍后再试～')
  } finally {
    loading.value = false
  }
}

const pollTaskStatus = async (taskId) => {
  const maxRetries = 300
  const interval = 5000
  
  for (let i = 0; i < maxRetries; i++) {
    try {
      await new Promise(resolve => setTimeout(resolve, interval))
      
      const statusRes = await getTaskStatus(taskId)
      console.log(`查询任务状态 (${i + 1}次):`, statusRes)
      
      const status = statusRes.data.status
      
      if (status === 'SUCCESS') {
        const result = statusRes.data.result
        if (result && result.success) {
          generatedVideo.value = result.videoUrl
          generatedCover.value = result.coverUrl || result.videoUrl
          lastGenerateResult.value = result
          showSuccessToast('哇！生成成功啦 ✨')
        } else {
          showToast(result?.message || '生成失败了')
        }
        return
      } else if (status === 'FAILED') {
        const errorMsg = statusRes.data.errorMessage || '生成失败了'
        showToast(errorMsg)
        return
      }
    } catch (error) {
      console.error('查询任务状态失败:', error)
    }
  }
  
  showToast('生成超时了，请稍后再试')
}

const saveToHistory = async () => {
  if (!lastGenerateResult.value) {
    showToast('请先生成内容')
    return
  }
  
  saving.value = true
  try {
    const saveData = {
      prompt: prompt.value,
      generateType: generateType.value,
      modelName: lastGenerateResult.value.modelName || modelName.value
    }
    
    if (generateType.value === 'img') {
      saveData.imageUrl = lastGenerateResult.value.imageUrl
      saveData.localPath = downloadedLocalPath.value || lastGenerateResult.value.localPath || ''
      saveData.imageSize = imageSize.value
      saveData.coverUrl = lastGenerateResult.value.coverUrl || lastGenerateResult.value.imageUrl
    } else {
      saveData.videoUrl = lastGenerateResult.value.videoUrl
      saveData.coverUrl = lastGenerateResult.value.coverUrl
      saveData.localPath = downloadedLocalPath.value || lastGenerateResult.value.localPath || ''
    }
    
    console.log('保存历史记录数据:', saveData)
    console.log('downloadedLocalPath:', downloadedLocalPath.value)
    console.log('lastGenerateResult.localPath:', lastGenerateResult.value.localPath)
    
    await saveHistory(saveData)
    showSuccessToast('已成功保存到历史记录！')
  } catch (error) {
    console.error('保存失败:', error)
    showToast('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const copyPrompt = () => {
  if (!prompt.value.trim()) {
    showToast('没有可复制的提示词')
    return
  }
  
  navigator.clipboard.writeText(prompt.value)
    .then(() => {
      showSuccessToast('提示词已复制到剪贴板！')
    })
    .catch(() => {
      showToast('复制失败，请手动复制')
    })
}

const downloadResult = async () => {
  let url = ''
  let fileType = ''
  
  if (generateType.value === 'img') {
    url = generatedImage.value
    fileType = 'img'
  } else {
    url = generatedVideo.value
    fileType = 'video'
  }
  
  if (!url) {
    showToast('没有可下载的内容')
    return
  }
  
  try {
    showSuccessToast('正在下载到本地...')
    
    const res = await downloadDirect({
      fileUrl: url,
      fileType: fileType,
      prompt: prompt.value
    })
    
    console.log('下载响应:', res)
    console.log('res.code:', res.code)
    console.log('res.data:', res.data)
    
    if (res.code === 200) {
      const localPath = res.data?.localPath
      console.log('localPath:', localPath)
      console.log('lastGenerateResult.value:', lastGenerateResult.value)
      if (localPath) {
        downloadedLocalPath.value = localPath
        if (lastGenerateResult.value) {
          lastGenerateResult.value.localPath = localPath
          console.log('已更新 lastGenerateResult.localPath:', lastGenerateResult.value.localPath)
        }
      }
      showSuccessToast('下载成功！已保存到本地')
    } else {
      showToast(res.message || '下载失败')
    }
  } catch (error) {
    console.error('下载失败:', error)
    showToast('下载失败，请重试')
  }
}

const previewResultImage = () => {
  if (!generatedImage.value) return
  showImagePreview({
    images: [generatedImage.value],
    startPosition: 0,
    closeable: true,
    closeOnClickOverlay: true
  })
}

const previewResultVideo = () => {
  if (!generatedVideo.value) return
  currentVideoUrl.value = generatedVideo.value
  currentCoverUrl.value = generatedCover.value || generatedVideo.value
  showVideoDialog.value = true
}

const closeVideoDialog = () => {
  if (videoRef.value) {
    videoRef.value.pause()
  }
  showVideoDialog.value = false
  currentVideoUrl.value = ''
  currentCoverUrl.value = ''
}

const fillFromHistory = () => {
  if (route.query.fromHistory === 'true') {
    if (route.query.generateType) {
      generateType.value = route.query.generateType
    }
    if (route.query.prompt) {
      prompt.value = route.query.prompt
    }
    if (route.query.imageSize) {
      imageSize.value = route.query.imageSize
    }
    if (route.query.imageUrl) {
      generatedImage.value = route.query.imageUrl
    }
    if (route.query.videoUrl) {
      generatedVideo.value = route.query.videoUrl
    }
    if (route.query.modelName) {
      modelName.value = route.query.modelName
    }
  }
}

const goHistory = () => {
  router.push('/history')
}

const loadConfig = async () => {
  try {
    const res = await getConfigMap()
    if (res.data) {
      const configMap = res.data
      configForm.value = {
        imageApiUrl: configMap['image.api.url'] || '',
        imageApiKey: configMap['image.api.key'] || '',
        imageModelName: configMap['image.model.name'] || '',
        videoApiUrl: configMap['video.api.url'] || '',
        videoApiKey: configMap['video.api.key'] || '',
        videoModelName: configMap['video.model.name'] || '',
        frontendTunnelUrl: configMap['frontend.tunnel.url'] || '',
        backendTunnelUrl: configMap['backend.tunnel.url'] || ''
      }
      setConfigCache(configMap)
      
      const isEmpty = !configForm.value.imageApiUrl || !configForm.value.imageApiKey || !configForm.value.videoApiUrl || !configForm.value.videoApiKey
      if (isEmpty) {
        showConfigPanel.value = true
      }
    }
  } catch (error) {
    console.error('加载配置失败:', error)
    const localConfig = loadConfigFromLocal()
    if (localConfig) {
      configForm.value = {
        imageApiUrl: localConfig['image.api.url'] || '',
        imageApiKey: localConfig['image.api.key'] || '',
        imageModelName: localConfig['image.model.name'] || '',
        videoApiUrl: localConfig['video.api.url'] || '',
        videoApiKey: localConfig['video.api.key'] || '',
        videoModelName: localConfig['video.model.name'] || '',
        frontendTunnelUrl: localConfig['frontend.tunnel.url'] || '',
        backendTunnelUrl: localConfig['backend.tunnel.url'] || ''
      }
    }
  }
}

const handleSaveConfig = async () => {
  savingConfig.value = true
  try {
    const configMap = {
      'image.api.url': configForm.value.imageApiUrl,
      'image.api.key': configForm.value.imageApiKey,
      'image.model.name': configForm.value.imageModelName,
      'video.api.url': configForm.value.videoApiUrl,
      'video.api.key': configForm.value.videoApiKey,
      'video.model.name': configForm.value.videoModelName,
      'frontend.tunnel.url': configForm.value.frontendTunnelUrl,
      'backend.tunnel.url': configForm.value.backendTunnelUrl
    }
    
    await batchUpdateConfig(configMap)
    setConfigCache(configMap)
    showSuccessToast('配置保存成功！')
    showConfigPanel.value = false
  } catch (error) {
    console.error('保存配置失败:', error)
    showToast('保存配置失败，请重试')
  } finally {
    savingConfig.value = false
  }
}

const handleLogout = () => {
  showConfigPanel.value = false
  
  // 显示确认对话框
  showToast({
    message: '确定要退出登录吗？',
    duration: 2000,
    overlay: true,
    onClose: () => {
      logout()
      showSuccessToast('已退出登录')
      
      setTimeout(() => {
        router.push('/login')
      }, 1000)
    }
  })
}

onMounted(async () => {
  fillFromHistory()
  loadModels()
  await loadConfig()
  
  // 检查用户配置，如果用户已登录但无配置，则自动弹出配置页
  const userInfo = getUserInfo()
  if (userInfo) {
    try {
      const configResponse = await getUserConfig()
      if (configResponse.code === 200 && configResponse.data) {
        // 用户有配置，检查是否完整
        const userConfig = configResponse.data
        const hasUserConfig = userConfig.imageApiUrl && userConfig.imageApiKey && 
                            userConfig.videoApiUrl && userConfig.videoApiKey
        
        if (!hasUserConfig) {
          // 用户配置不完整，自动弹出配置页
          setTimeout(() => {
            showConfigPanel.value = true
          }, 1000)
        }
      } else {
        // 获取用户配置失败，自动弹出配置页
        setTimeout(() => {
          showConfigPanel.value = true
        }, 1000)
      }
    } catch (error) {
      console.log('获取用户配置失败，将使用默认配置')
    }
  }
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  padding-bottom: 135px;
  overflow-y: auto;
}

.settings-btn {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 0, 128, 0.5), rgba(0, 255, 255, 0.5));
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  z-index: 9999;
  border: 2px solid rgba(255, 0, 128, 0.8);
  box-shadow: 0 0 30px rgba(255, 0, 128, 0.5);
  transition: all 0.3s ease;
  pointer-events: auto;
}

.settings-btn:active {
  transform: scale(0.95);
}

.header-section {
  padding: 54px 22px 41px;
  text-align: center;
}

.neon-border {
  position: relative;
  display: inline-block;
}

.logo-container {
  color: white;
  position: relative;
  padding: 27px 54px;
}

.logo-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 270px;
  height: 270px;
  background: radial-gradient(circle, rgba(255, 0, 128, 0.3) 0%, transparent 70%);
  filter: blur(54px);
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
  font-size: calc(100px * 1.35);
  margin-bottom: 22px;
  display: inline-block;
  animation: float 3s ease-in-out infinite;
  position: relative;
  z-index: 1;
  filter: drop-shadow(0 0 27px rgba(255, 0, 128, 0.6));
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(-2deg);
  }
  50% {
    transform: translateY(-20px) rotate(2deg);
  }
}

.app-title {
  font-size: calc(42px * 1.35);
  font-weight: 900;
  margin-bottom: 16px;
  letter-spacing: 5px;
  position: relative;
  z-index: 1;
}

.title-neon {
  background: linear-gradient(135deg, #ff0080 0%, #00ffff 50%, #ffcc00 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 
    0 0 14px rgba(255, 0, 128, 0.8),
    0 0 27px rgba(255, 0, 128, 0.6),
    0 0 54px rgba(255, 0, 128, 0.4),
    0 0 108px rgba(0, 255, 255, 0.3);
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
  font-size: calc(16px * 1.35);
  opacity: 0.9;
  font-weight: 300;
  color: rgba(255, 255, 255, 0.8);
  letter-spacing: 3px;
  position: relative;
  z-index: 1;
}

.content-area {
  padding: 0 22px;
}

.input-section,
.result-section {
  padding: 38px 32px;
  margin-bottom: 27px;
  position: relative;
  background: rgba(10, 10, 20, 0.7) !important;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 22px;
  margin-bottom: 38px;
}

.header-line {
  flex: 1;
  height: 3px;
  background: linear-gradient(90deg, transparent, #ff0080, #00ffff, transparent);
  border-radius: 3px;
}

.section-title {
  font-size: calc(32px * 1.35);
  font-weight: 800;
  color: white;
  letter-spacing: 4px;
  display: flex;
  align-items: center;
  gap: 14px;
  white-space: nowrap;
}

.title-icon {
  font-size: calc(28px * 1.35);
}

.type-selector {
  margin-bottom: 38px;
}

.type-buttons {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 19px;
}

.type-btn {
  flex: 1;
  min-width: 0;
  height: 122px;
  border-radius: 27px;
  background: rgba(20, 20, 35, 0.8);
  border: 3px solid rgba(255, 0, 128, 0.3);
  color: rgba(255, 255, 255, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 11px;
  transition: all 0.3s ease;
  padding: 0;
  position: relative;
  overflow: hidden;
}

.type-btn .btn-emoji {
  font-size: calc(32px * 1.35);
  position: relative;
  z-index: 2;
}

.type-btn .btn-text {
  font-size: calc(18px * 1.35);
  font-weight: 700;
  letter-spacing: 1px;
  position: relative;
  z-index: 2;
}

.type-btn .btn-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255, 0, 128, 0.3) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.type-btn.active {
  background: linear-gradient(135deg, rgba(255, 0, 128, 0.2) 0%, rgba(0, 255, 255, 0.2) 100%);
  border-color: #ff0080;
  color: white;
  box-shadow: 
    0 0 41px rgba(255, 0, 128, 0.4),
    0 0 81px rgba(0, 255, 255, 0.2),
    inset 0 0 41px rgba(255, 0, 128, 0.1);
  transform: translateY(-4px);
}

.type-btn.active .btn-glow {
  opacity: 1;
}

.type-btn:active {
  transform: scale(0.95);
}

.model-selector {
  margin-bottom: 38px;
  position: relative;
  z-index: 100;
}

.model-label {
  font-size: calc(25px * 1.35);
  font-weight: 700;
  color: white;
  margin-bottom: 16px;
  letter-spacing: 1px;
}

.model-select-box {
  background: rgba(20, 20, 35, 0.8);
  border-radius: 22px;
  border: 3px solid rgba(0, 255, 255, 0.3);
  padding: 22px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.model-select-box:active {
  transform: scale(0.98);
}

.model-value {
  font-size: calc(16px * 1.35);
  font-weight: 600;
  color: #ff0080;
}

.model-arrow {
  color: #00ffff;
  font-size: calc(20px * 1.35);
  transition: transform 0.3s ease;
}

.upload-section {
  margin-bottom: 38px;
}

.uploader {
  width: 100%;
}

.upload-box {
  width: 100%;
}

.upload-inner {
  width: 100%;
  height: 270px;
  border: 4px dashed rgba(0, 255, 255, 0.5);
  border-radius: 32px;
  background: rgba(20, 20, 35, 0.4);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.upload-inner::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(0, 255, 255, 0.1), transparent);
  animation: shimmer 2s infinite;
}

.upload-icon-box {
  width: 108px;
  height: 108px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 0, 128, 0.2) 0%, rgba(0, 255, 255, 0.2) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  border: 3px solid rgba(0, 255, 255, 0.3);
  color: #00ffff;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 27px rgba(0, 255, 255, 0.3);
  }
  50% {
    box-shadow: 0 0 54px rgba(0, 255, 255, 0.5);
  }
}

.upload-text {
  font-size: calc(16px * 1.35);
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 1px;
}

.upload-hint {
  font-size: calc(13px * 1.35);
  opacity: 0.7;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 8px;
}

.prompt-wrapper {
  margin-bottom: 38px;
}

.prompt-label {
  font-size: calc(25px * 1.35);
  font-weight: 700;
  color: white;
  margin-bottom: 19px;
  letter-spacing: 1px;
}

.prompt-field {
  background: rgba(20, 20, 35, 0.6);
  border-radius: 22px;
  overflow: hidden;
  border: 1px solid rgba(255, 0, 128, 0.3);
}

.prompt-field :deep(.van-field__control) {
  font-size: calc(26px * 1.35);
  line-height: 1.8;
  color: white;
  min-height: 162px;
}

.prompt-field :deep(.van-field__control::placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

.size-selector {
  margin-top: 11px;
}

.size-label {
  font-size: calc(15px * 1.35);
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 19px;
  letter-spacing: 1px;
}

.size-buttons {
  display: flex;
  gap: 19px;
}

.size-btn {
  flex: 1;
  height: 76px;
  border-radius: 22px;
  background: rgba(20, 20, 35, 0.7);
  border: 3px solid rgba(0, 255, 255, 0.3);
  color: rgba(255, 255, 255, 0.8);
  font-size: calc(16px * 1.35);
  font-weight: 700;
  transition: all 0.3s ease;
  letter-spacing: 1px;
}

.size-btn.active {
  background: linear-gradient(135deg, rgba(255, 0, 128, 0.3) 0%, rgba(0, 255, 255, 0.3) 100%);
  border-color: #ff0080;
  color: white;
  box-shadow: 0 0 41px rgba(255, 0, 128, 0.3);
}

.result-wrapper {
  margin-bottom: 32px;
  max-height: none;
  overflow: visible;
}

.result-border {
  border-radius: 27px;
  overflow: hidden;
  padding: 5px;
  background: linear-gradient(135deg, #ff0080, #00ffff, #ffcc00);
  animation: resultBorderGlow 3s ease-in-out infinite;
}

@keyframes resultBorderGlow {
  0%, 100% {
    box-shadow: 0 0 41px rgba(255, 0, 128, 0.3), 0 0 81px rgba(0, 255, 255, 0.2);
  }
  50% {
    box-shadow: 0 0 68px rgba(255, 0, 128, 0.5), 0 0 135px rgba(0, 255, 255, 0.3);
  }
}

.image-container,
.video-container {
  border-radius: 22px;
  overflow: hidden;
}

.result-image {
  display: block;
  width: 100%;
  height: auto;
  border-radius: 22px;
}

.result-video {
  width: 100%;
  border-radius: 22px;
  display: block;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 19px;
}

.action-btn {
  height: 76px;
  border-radius: 22px;
  font-size: calc(16px * 1.35);
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  border: 3px solid transparent;
  transition: all 0.3s ease;
  letter-spacing: 1px;
}

.save-btn {
  background: linear-gradient(135deg, #ff0080 0%, #00ffff 100%);
  color: white;
  border: none;
  box-shadow: 0 0 41px rgba(255, 0, 128, 0.3);
}

.copy-btn,
.download-btn {
  background: rgba(20, 20, 35, 0.7);
  color: rgba(255, 255, 255, 0.9);
  border-color: rgba(0, 255, 255, 0.4);
}

.btn-icon {
  font-size: calc(20px * 1.35);
}

.bottom-action-bar {
  position: relative;
  padding: 22px;
  z-index: 50;
}

.generate-btn {
  height: 97px;
  font-size: calc(20px * 1.35);
  font-weight: 800;
  background: linear-gradient(135deg, #ff0080 0%, #00ffff 50%, #ffcc00 100%);
  border: none;
  letter-spacing: 5px;
  position: relative;
  overflow: hidden;
  box-shadow: 
    0 0 41px rgba(255, 0, 128, 0.4),
    0 0 81px rgba(0, 255, 255, 0.3),
    0 0 135px rgba(255, 204, 0, 0.2);
}

.generate-btn::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 30%,
    rgba(255, 255, 255, 0.1) 50%,
    transparent 70%
  );
  animation: btnShimmer 2s infinite;
}

@keyframes btnShimmer {
  0% {
    transform: translateX(-100%) rotate(0deg);
  }
  100% {
    transform: translateX(100%) rotate(0deg);
  }
}

.generate-btn.glowing {
  animation: generateGlow 1.5s ease-in-out infinite;
}

@keyframes generateGlow {
  0%, 100% {
    box-shadow: 
      0 0 41px rgba(255, 0, 128, 0.4),
      0 0 81px rgba(0, 255, 255, 0.3),
      0 0 135px rgba(255, 204, 0, 0.2);
  }
  50% {
    box-shadow: 
      0 0 68px rgba(255, 0, 128, 0.6),
      0 0 135px rgba(0, 255, 255, 0.5),
      0 0 203px rgba(255, 204, 0, 0.3);
  }
}

.generate-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.generate-btn:disabled {
  opacity: 0.5;
}

.custom-tabbar {
  background: rgba(8, 8, 15, 0.98);
  backdrop-filter: blur(40px);
  -webkit-backdrop-filter: blur(40px);
  border-top: 2px solid rgba(255, 0, 128, 0.4);
  box-shadow: 0 -10px 40px rgba(255, 0, 128, 0.2);
}

.custom-tabbar :deep(.van-tabbar-item) {
  font-size: calc(14px * 1.35);
  color: rgba(255, 255, 255, 0.5);
  font-weight: 600;
  letter-spacing: 1px;
}

.custom-tabbar :deep(.van-tabbar-item--active) {
  color: #ff0080;
  text-shadow: 0 0 20px rgba(255, 0, 128, 0.6);
}

.custom-tabbar :deep(.van-tabbar-item__icon) {
  font-size: calc(24px * 1.35);
}

.config-panel-content {
  padding: 20px 0;
  max-height: 60vh;
  overflow-y: auto;
  overflow-x: hidden;
}

.config-panel-content::-webkit-scrollbar {
  width: 8px;
}

.config-panel-content::-webkit-scrollbar-track {
  background: rgba(20, 20, 35, 0.3);
  border-radius: 4px;
}

.config-panel-content::-webkit-scrollbar-thumb {
  background: rgba(255, 0, 128, 0.5);
  border-radius: 4px;
}

.config-panel-content::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 0, 128, 0.7);
}

.config-section {
  margin-bottom: 24px;
}

.config-section:last-child {
  margin-bottom: 0;
}

.config-section .section-title {
  font-size: calc(24px * 1.35);
  font-weight: 900;
  color: white;
  margin-bottom: 20px;
  text-align: center;
  letter-spacing: 2px;
  text-shadow: 0 0 15px rgba(255, 0, 128, 0.8), 0 0 30px rgba(0, 255, 255, 0.6);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: rgba(255, 0, 128, 0.2);
  padding: 12px 24px;
  border-radius: 16px;
  border: 1px solid rgba(255, 0, 128, 0.4);
  box-shadow: 0 0 20px rgba(255, 0, 128, 0.3);
}

.config-section .section-title::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, transparent, #ff0080, #00ffff, transparent);
  border-radius: 2px;
  box-shadow: 0 0 10px rgba(255, 0, 128, 0.5);
}

.config-section .section-title .title-icon {
  font-size: calc(24px * 1.35);
  filter: drop-shadow(0 0 5px rgba(255, 0, 128, 0.5));
}

.config-group {
  background: rgba(20, 20, 35, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 20px;
  border: 2px solid rgba(255, 0, 128, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  position: relative;
  overflow: hidden;
}

.config-group::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #ff0080, #00ffff, transparent);
  border-radius: 20px 20px 0 0;
}

.config-group:last-child {
  margin-bottom: 0;
}

.config-item {
  margin-bottom: 20px;
}

.config-item:last-child {
  margin-bottom: 0;
}

.config-label {
  display: block;
  font-size: calc(18px * 1.35);
  font-weight: 800;
  color: white;
  margin-bottom: 12px;
  letter-spacing: 1px;
  text-shadow: 0 0 10px rgba(255, 0, 128, 0.6);
  background: rgba(255, 0, 128, 0.15);
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid rgba(255, 0, 128, 0.3);
  box-shadow: 0 0 10px rgba(255, 0, 128, 0.2);
}

.config-field {
  background: rgba(40, 40, 60, 0.9);
  border-radius: 16px;
  border: 2px solid rgba(255, 0, 128, 0.4);
  padding: 8px 16px;
  transition: all 0.3s ease;
  box-shadow: 0 0 15px rgba(255, 0, 128, 0.2);
}

.config-field:focus-within {
  border-color: #ff0080;
  box-shadow: 0 0 25px rgba(255, 0, 128, 0.4);
  background: rgba(50, 50, 70, 0.95);
}

.config-field :deep(.van-field__control) {
  font-size: calc(18px * 1.35);
  color: white;
  padding: 14px 0;
  min-height: 52px;
  font-weight: 600;
  text-shadow: 0 0 5px rgba(255, 255, 255, 0.3);
}

.config-field :deep(.van-field__control::placeholder) {
  color: rgba(255, 255, 255, 0.6);
  font-size: calc(16px * 1.35);
  text-shadow: 0 0 5px rgba(255, 255, 255, 0.2);
}

.config-field :deep(.van-field__button) {
  color: rgba(255, 255, 255, 0.6);
  padding: 0 8px;
}

.config-field :deep(.van-field__button:hover) {
  color: rgba(255, 255, 255, 0.8);
}

:deep(.van-dialog) {
  background: rgba(10, 10, 20, 0.95) !important;
  backdrop-filter: blur(40px);
  -webkit-backdrop-filter: blur(40px);
  border: 2px solid rgba(255, 0, 128, 0.4);
  box-shadow: 0 0 60px rgba(255, 0, 128, 0.3);
}

:deep(.van-dialog__header) {
  background: linear-gradient(135deg, rgba(255, 0, 128, 0.2) 0%, rgba(0, 255, 255, 0.2) 100%);
  padding: 24px 20px;
  border-bottom: 2px solid rgba(255, 0, 128, 0.3);
  background: linear-gradient(135deg, #ff0080, #00ffff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: calc(36px * 1.35);
  font-weight: 900;
  text-align: center;
  letter-spacing: 4px;
  text-shadow: 0 0 20px rgba(255, 0, 128, 0.8), 0 0 40px rgba(0, 255, 255, 0.6);
}

:deep(.van-dialog__title) {
  background: linear-gradient(135deg, #ff0080, #00ffff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: calc(36px * 1.35);
  font-weight: 900;
  letter-spacing: 4px;
  text-shadow: 0 0 30px rgba(255, 0, 128, 0.8), 0 0 60px rgba(0, 255, 255, 0.6);
  padding: 20px 0;
  margin-bottom: 10px;
  text-align: center;
  border-bottom: 2px solid rgba(255, 0, 128, 0.4);
  box-shadow: 0 4px 20px rgba(255, 0, 128, 0.3);
  border-radius: 12px;
  background: rgba(255, 0, 128, 0.1);
}

:deep(.van-dialog__content) {
  padding: 24px 20px;
  background: transparent;
}

:deep(.van-dialog__footer) {
  padding: 20px;
  border-top: 2px solid rgba(255, 0, 128, 0.3);
  background: rgba(20, 20, 35, 0.8);
}

::deep(.van-dialog__confirm) {
  background: linear-gradient(135deg, #ff0080, #00ffff);
}

/* 退出登录按钮样式 */
.logout-section {
  margin-top: 30px;
  padding-top: 20px;
}

.section-divider {
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(255, 0, 128, 0.5), transparent);
  margin-bottom: 25px;
}

.logout-container {
  padding: 0 10px;
}

.logout-btn {
  background: linear-gradient(135deg, rgba(255, 0, 128, 0.8), rgba(255, 69, 0, 0.8));
  border: 2px solid rgba(255, 0, 128, 0.6);
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  height: 48px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 0, 128, 0.3);
}

.logout-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.logout-btn:hover::before {
  left: 100%;
}

.logout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 25px rgba(255, 0, 128, 0.5);
  border-color: rgba(255, 0, 128, 0.8);
}

.logout-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 10px rgba(255, 0, 128, 0.3);
}

.logout-icon {
  margin-right: 8px;
  font-size: 18px;
  filter: drop-shadow(0 0 5px rgba(255, 255, 255, 0.5));
}

:deep(.van-dialog__confirm) {
  background: linear-gradient(135deg, #ff0080, #00ffff);
  border: none;
  border-radius: 16px;
  padding: 14px 32px;
  font-size: calc(18px * 1.35);
  font-weight: 800;
  color: white;
  text-shadow: 0 0 15px rgba(0, 0, 0, 0.7);
  box-shadow: 0 6px 20px rgba(255, 0, 128, 0.6), 0 0 30px rgba(0, 255, 255, 0.4);
  transition: all 0.3s ease;
  min-width: 140px;
}

:deep(.van-dialog__confirm:hover) {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(255, 0, 128, 0.8), 0 0 40px rgba(0, 255, 255, 0.6);
}

:deep(.van-dialog__cancel) {
  background: rgba(255, 255, 255, 0.15);
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-radius: 16px;
  padding: 14px 32px;
  font-size: calc(18px * 1.35);
  font-weight: 800;
  color: white;
  text-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
  transition: all 0.3s ease;
  min-width: 140px;
}

:deep(.van-dialog__cancel:hover) {
  background: rgba(255, 255, 255, 0.25);
  box-shadow: 0 6px 20px rgba(255, 255, 255, 0.3);
}

.preview-hint {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: calc(14px * 1.35);
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.result-border {
  position: relative;
  cursor: pointer;
}

.result-border:hover .preview-hint {
  opacity: 1;
}

.video-dialog :deep(.van-dialog__content) {
  padding: 0;
}

.video-dialog :deep(.van-dialog) {
  width: 95vw !important;
  max-width: 900px;
}

.video-preview-container {
  background: rgba(15, 15, 25, 0.98);
  border-radius: 20px;
  overflow: hidden;
}

.video-preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: rgba(255, 0, 128, 0.2);
  border-bottom: 2px solid rgba(0, 255, 255, 0.3);
}

.video-preview-title {
  font-size: calc(18px * 1.35);
  font-weight: 700;
  color: white;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
}

.video-preview-header .close-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  color: white;
}

.video-wrapper {
  padding: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #000;
}

.dialog-video {
  width: 100%;
  max-width: 100%;
  max-height: 70vh;
  border-radius: 12px;
  background: #000;
  object-fit: contain;
}
</style>
