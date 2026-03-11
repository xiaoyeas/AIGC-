import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import Vant from 'vant'
import 'vant/lib/index.css'
import './styles/global.css'
import { fetchSystemConfig } from './utils/config'

async function initApp() {
  try {
    await fetchSystemConfig()
  } catch (e) {
    console.warn('初始化配置失败，继续启动应用', e)
  }

  const app = createApp(App)
  app.use(router)
  app.use(Vant)
  app.mount('#app')
}

initApp()
