# AIGC 多模态生成平台 - 前端项目

## 项目简介

基于 Vue 3 + Vite 构建的 AIGC 多模态生成平台前端应用，支持**文生图、文生视频、图生视频**三种生成方式，采用移动端优先的响应式设计，提供流畅的用户体验。

## 技术栈

### 核心框架
- **Vue 3.4.21** - 渐进式 JavaScript 框架
- **Vue Router 4.3.0** - 官方路由管理器
- **Vite 5.2.0** - 新一代前端构建工具

### UI 组件库
- **Vant 4.8.10** - 移动端 Vue 组件库

### HTTP 客户端
- **Axios 1.6.8** - Promise based HTTP 客户端

### 样式处理
- **PostCSS 8.4.38** - CSS 转换工具
- **postcss-pxtorem 6.1.0** - 自动将 px 转换为 rem
- **autoprefixer 10.4.19** - 自动添加浏览器前缀

## 项目结构

```
aigc-frontend/
├── public/                      # 静态资源
├── src/
│   ├── api/                     # API 接口封装
│   │   ├── index.js             # API 接口定义
│   │   └── auth.js             # 认证相关接口
│   ├── router/                   # 路由配置
│   │   └── index.js            # 路由定义和守卫
│   ├── styles/                   # 全局样式
│   │   └── global.css          # 全局 CSS 样式
│   ├── utils/                    # 工具函数
│   │   ├── auth.js             # Token 管理
│   │   ├── config.js           # 系统配置管理
│   │   └── request.js         # Axios 请求封装
│   ├── views/                    # 页面组件
│   │   ├── Home.vue            # 首页（生成页面）
│   │   ├── History.vue          # 历史记录页面
│   │   ├── Login.vue           # 登录页面
│   │   ├── Register.vue        # 注册页面
│   │   └── NotFound.vue        # 404 页面
│   ├── App.vue                   # 根组件
│   └── main.js                   # 应用入口
├── index.html                   # HTML 模板
├── package.json                 # 项目依赖配置
├── vite.config.js              # Vite 配置文件
├── postcss.config.js           # PostCSS 配置
└── .gitignore                 # Git 忽略文件配置
```

## 功能特性

### 1. 用户认证
- ✅ 用户注册
- ✅ 用户登录
- ✅ JWT Token 认证
- ✅ Token 自动刷新
- ✅ 路由权限控制

### 2. 内容生成
- ✅ **文生图**：根据文字描述生成图片
- ✅ **文生视频**：根据文字描述生成视频
- ✅ **图生视频**：根据图片生成视频
- ✅ 模型选择：支持多模型切换
- ✅ 图片上传：支持 JPG、PNG、WEBP 格式
- ✅ 异步生成：视频生成支持异步任务
- ✅ 任务状态查询：实时查询生成进度

### 3. 历史记录管理
- ✅ 历史记录列表：查看所有生成记录
- ✅ 类型筛选：按生成类型筛选记录
- ✅ 详情查看：查看生成详情
- ✅ 删除记录：删除不需要的记录
- ✅ 本地下载：将图片/视频下载到本地服务器
- ✅ 路径显示：显示本地保存路径

### 4. 系统配置
- ✅ 动态配置：从后端获取系统配置
- ✅ 本地缓存：配置信息缓存到 LocalStorage
- ✅ 内网穿透：支持配置前后端内网穿透地址

### 5. 用户体验
- ✅ 响应式设计：适配移动端和桌面端
- ✅ 加载状态：生成过程中显示加载动画
- ✅ 错误提示：友好的错误提示信息
- ✅ 成功提示：操作成功后的提示反馈
- ✅ 视频预览：支持视频封面预览
- ✅ 图片预览：支持图片放大查看

## 快速开始

### 环境要求
- Node.js >= 16.0.0
- npm >= 8.0.0 或 pnpm >= 7.0.0

### 安装依赖

```bash
# 使用 npm
npm install

# 使用 pnpm（推荐）
pnpm install

# 使用 yarn
yarn install
```

### 开发模式

```bash
npm run dev
```

启动后访问：http://localhost:5173

### 生产构建

```bash
npm run build
```

构建产物会生成在 `dist/` 目录。

### 预览构建结果

```bash
npm run preview
```

## 配置说明

### 后端 API 地址配置

前端通过 `src/utils/request.js` 配置后端 API 地址：

```javascript
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
```

**环境变量配置**：

创建 `.env.development` 文件（开发环境）：
```env
VITE_API_BASE_URL=http://localhost:8080/api
```

创建 `.env.production` 文件（生产环境）：
```env
VITE_API_BASE_URL=https://your-domain.com/api
```

### 系统配置管理

前端启动时会自动从后端获取系统配置，包括：
- `frontend.tunnel.url` - 前端内网穿透地址
- `backend.tunnel.url` - 后端内网穿透地址

配置信息会缓存到 LocalStorage，避免重复请求。

### 移动端适配

项目使用 `postcss-pxtorem` 自动将 px 转换为 rem，适配不同屏幕尺寸。

**设计稿尺寸**：375px（iPhone SE）

## API 接口

### 认证接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/auth/login` | POST | 用户登录 |
| `/auth/register` | POST | 用户注册 |
| `/auth/refresh` | POST | 刷新 Token |

### 生成接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/generate` | POST | 同步生成内容 |
| `/generate-async` | POST | 异步生成内容（推荐用于视频） |
| `/task-status/{taskId}` | GET | 查询任务状态 |
| `/upload` | POST | 上传图片（图生视频） |
| `/models` | GET | 获取模型列表 |

### 历史记录接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | GET | 获取历史记录列表 |
| `/detail/{id}` | GET | 获取历史记录详情 |
| `/delete/{id}` | DELETE | 删除历史记录 |
| `/save` | POST | 保存生成结果到历史记录 |
| `/download/{id}` | POST | 下载历史记录文件到本地 |
| `/download-direct` | POST | 直接下载生成结果 |

### 配置接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/config/list` | GET | 获取配置列表 |
| `/config/map` | GET | 获取配置映射 |
| `/config/save` | POST | 保存配置 |
| `/config/batch-update` | POST | 批量更新配置 |

## 页面说明

### 1. 首页（Home.vue）

**功能**：
- 选择生成类型（文生图/文生视频/图生视频）
- 选择 AI 模型
- 输入提示词
- 上传参考图片（图生视频）
- 生成内容
- 预览生成结果
- 下载结果到本地
- 保存到历史记录

**使用流程**：
```
1. 选择生成类型
      ↓
2. 选择模型
      ↓
3. 输入提示词（图生视频需上传图片）
      ↓
4. 点击生成按钮
      ↓
5. 等待生成完成
      ↓
6. 预览结果
      ↓
7. 下载结果（可选）
      ↓
8. 保存到历史记录
```

### 2. 历史记录页面（History.vue）

**功能**：
- 查看历史记录列表
- 按类型筛选记录
- 查看记录详情
- 下载文件到本地
- 删除记录

**使用流程**：
```
1. 进入历史记录页面
      ↓
2. 选择生成类型筛选（可选）
      ↓
3. 查看记录列表
      ↓
4. 点击下载按钮下载文件
      ↓
5. 点击删除按钮删除记录
```

### 3. 登录页面（Login.vue）

**功能**：
- 用户登录
- 跳转注册页面

### 4. 注册页面（Register.vue）

**功能**：
- 用户注册
- 跳转登录页面

### 5. 404 页面（NotFound.vue）

**功能**：
- 显示页面未找到提示
- 返回首页

## 路由配置

| 路径 | 名称 | 组件 | 需要认证 |
|------|------|------|---------|
| `/` | Home | Home.vue | 是 |
| `/history` | History | History.vue | 是 |
| `/login` | Login | Login.vue | 否 |
| `/register` | Register | Register.vue | 否 |
| `/:pathMatch(.*)*` | NotFound | NotFound.vue | 否 |

**路由守卫**：
- 需要认证的页面：未登录或 Token 过期时跳转到登录页
- 不需要认证的页面：已登录时跳转到首页

## 工具函数说明

### auth.js - Token 管理

```javascript
// 获取 Token
getToken()

// 设置 Token
setToken(token)

// 移除 Token
removeToken()

// 检查 Token 是否过期
isTokenExpired()
```

### config.js - 系统配置管理

```javascript
// 获取前端内网穿透地址
getFrontendTunnelUrl()

// 获取后端内网穿透地址
getBackendTunnelUrl()

// 从后端获取系统配置
fetchSystemConfig()
```

### request.js - Axios 请求封装

- 自动添加 Token 到请求头
- 统一错误处理
- 请求/响应拦截器
- 超时处理

## 样式规范

### 全局样式

项目使用全局 CSS 变量定义主题色：

```css
:root {
  --primary-color: #4facfe;
  --secondary-color: #00f2fe;
  --text-color: #333;
  --bg-color: #f5f5f5;
}
```

### 组件样式

每个页面组件使用 scoped 样式，避免样式冲突。

### 响应式设计

- 使用 Flexbox 和 Grid 布局
- 使用相对单位（rem、%）
- 媒体查询适配不同屏幕

## 常见问题

### Q: 如何修改后端 API 地址？

A: 修改 `.env.development` 或 `.env.production` 文件中的 `VITE_API_BASE_URL` 变量。

### Q: 如何适配不同屏幕尺寸？

A: 项目使用 `postcss-pxtorem` 自动转换 px 为 rem，设计稿尺寸为 375px。

### Q: Token 过期怎么办？

A: 系统会自动检测 Token 是否过期，过期后会跳转到登录页面。

### Q: 如何添加新的页面？

A: 在 `src/views/` 目录下创建新组件，然后在 `src/router/index.js` 中添加路由配置。

## 开发建议

1. **代码规范**：遵循 Vue 3 Composition API 编写规范
2. **组件复用**：提取公共组件到 `src/components/` 目录
3. **样式管理**：使用 CSS 变量管理主题色
4. **错误处理**：使用 try-catch 捕获异常，使用 Vant Toast 提示用户
5. **性能优化**：使用路由懒加载，按需引入 Vant 组件

## 浏览器支持

- Chrome >= 87
- Firefox >= 78
- Safari >= 14
- Edge >= 88

## 许可证

MIT
