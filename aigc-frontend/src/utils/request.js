import axios from 'axios'
import { showToast } from 'vant'
import { getBackendTunnelUrl } from './config'
import { getToken } from './auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 300000
})

request.interceptors.request.use(
  config => {
    const backendTunnelUrl = getBackendTunnelUrl()
    if (backendTunnelUrl && !import.meta.env.DEV) {
      config.baseURL = backendTunnelUrl + '/api'
    }
    
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      showToast(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    showToast(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
