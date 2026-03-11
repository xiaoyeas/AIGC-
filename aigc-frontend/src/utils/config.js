import { getConfigMap } from '@/api'

const CONFIG_KEY = 'aigc_frontend_config'

let configCache = null

export const getConfigCache = () => configCache

export const setConfigCache = (config) => {
  configCache = config
  try {
    localStorage.setItem(CONFIG_KEY, JSON.stringify(config))
  } catch (e) {
    console.warn('保存配置到本地失败', e)
  }
}

export const loadConfigFromLocal = () => {
  try {
    const saved = localStorage.getItem(CONFIG_KEY)
    if (saved) {
      configCache = JSON.parse(saved)
      return configCache
    }
  } catch (e) {
    console.warn('读取本地配置失败', e)
  }
  return null
}

export const fetchSystemConfig = async () => {
  try {
    const res = await getConfigMap()
    if (res && res.data) {
      const configMap = res.data
      setConfigCache(configMap)
      return configMap
    }
  } catch (e) {
    console.warn('获取系统配置失败，使用本地缓存或默认值', e)
  }
  return null
}

export const getFrontendTunnelUrl = () => {
  const config = configCache || loadConfigFromLocal()
  if (config && config['frontend.tunnel.url']) {
    let url = config['frontend.tunnel.url']
    if (!url.startsWith('http://') && !url.startsWith('https://')) {
      url = 'http://' + url
    }
    return url
  }
  return ''
}

export const getBackendTunnelUrl = () => {
  const config = configCache || loadConfigFromLocal()
  if (config && config['backend.tunnel.url']) {
    let url = config['backend.tunnel.url']
    if (!url.startsWith('http://') && !url.startsWith('https://')) {
      url = 'http://' + url
    }
    return url
  }
  return ''
}
