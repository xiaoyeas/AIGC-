import request from '@/utils/request'

export function generateImage(data) {
  return request({
    url: '/generate',
    method: 'post',
    data
  })
}

export function generateImageAsync(data) {
  return request({
    url: '/generate-async',
    method: 'post',
    data
  })
}

export function getTaskStatus(taskId) {
  return request({
    url: `/task-status/${taskId}`,
    method: 'get'
  })
}

export function saveHistory(data) {
  return request({
    url: '/save',
    method: 'post',
    data
  })
}

export function getHistoryList(limit = 20, generateType) {
  return request({
    url: '/list',
    method: 'get',
    params: { limit, generateType }
  })
}

export function getHistoryDetail(id) {
  return request({
    url: `/detail/${id}`,
    method: 'get'
  })
}

export function deleteHistory(id) {
  return request({
    url: `/delete/${id}`,
    method: 'delete'
  })
}

export function healthCheck() {
  return request({
    url: '/health',
    method: 'get'
  })
}

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function getModels(generateType) {
  return request({
    url: '/models',
    method: 'get',
    params: { generateType }
  })
}

export function getConfigList() {
  return request({
    url: '/config/list',
    method: 'get'
  })
}

export function getConfigMap() {
  return request({
    url: '/config/map',
    method: 'get'
  })
}

export function saveConfig(config) {
  return request({
    url: '/config/save',
    method: 'post',
    data: config
  })
}

export function batchUpdateConfig(configMap) {
  return request({
    url: '/config/batch-update',
    method: 'post',
    data: configMap
  })
}

/**
 * 下载历史记录中的图片或视频到本地
 * @param {number} id - 历史记录ID
 * @returns {Promise} 下载结果，包含本地保存路径
 */
export function downloadHistoryFile(id) {
  return request({
    url: `/download/${id}`,
    method: 'post'
  })
}

/**
 * 直接下载生成结果到本地（不需要历史记录ID）
 * @param {Object} data - 下载参数
 * @param {string} data.fileUrl - 文件URL
 * @param {string} data.fileType - 文件类型（img/video）
 * @param {string} data.prompt - 提示词
 * @returns {Promise} 下载结果，包含本地保存路径
 */
export function downloadDirect(data) {
  return request({
    url: '/download-direct',
    method: 'post',
    data
  })
}
