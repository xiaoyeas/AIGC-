# AIGC 多模态生成平台后端服务

## 项目简介
基于 Spring Boot 3 + Spring AI 实现的 AIGC 多模态生成平台后端服务，支持**文生图、文生视频、图生视频**三种生成方式，使用智谱 AI 免费模型，支持历史记录管理、多模型切换等功能。

## 技术栈
- Java 17
- Spring Boot 3.2.5
- Spring AI 1.0.0-M4
- MyBatis-Plus 3.5.5
- MySQL 8.0+
- Redis

## AI 模型说明
本项目使用 **智谱 AI (ZhiPu AI)** 的免费模型：
- **cogview-3-flash** - 文生图模型
- **cogvideox-3** - 文生视频/图生视频模型（支持两种模式）

模型特点：
- 免费使用，无需付费
- 国内可直接访问，无需翻墙
- 生成速度快，质量优秀
- 后端统一管理模型集合，新增/切换模型仅改配置

## 生成类型说明
| 类型 | 说明 | 模型 |
|------|------|------|
| img | 文生图 | cogview-3-flash |
| text2video | 文生视频 | cogvideox-3 |
| img2video | 图生视频 | cogvideox-3 |

## 项目结构
```
aigc-backend/
├── src/main/java/com/aigc/
│   ├── AigcApplication.java      # 启动类
│   ├── common/                   # 通用模块
│   │   ├── Result.java           # 统一响应封装
│   │   └── GlobalExceptionHandler.java  # 全局异常处理
│   ├── config/                   # 配置类
│   │   ├── ModelConfig.java      # 模型配置管理
│   │   ├── RedisConfig.java      # Redis配置
│   │   ├── MybatisPlusConfig.java # MyBatis-Plus配置
│   │   └── WebConfig.java        # Web配置（CORS、静态资源映射）
│   ├── controller/               # 控制器层
│   │   └── AigcController.java   # 核心接口
│   ├── dto/                      # 数据传输对象
│   │   ├── ImageGenerateRequest.java
│   │   ├── ImageGenerateResult.java
│   │   └── HistorySaveRequest.java
│   ├── entity/                   # 实体类
│   │   └── AiGenerateHistory.java
│   ├── mapper/                   # Mapper层
│   │   └── AiGenerateHistoryMapper.java
│   ├── service/                  # 服务层
│   │   ├── AiGenerateHistoryService.java
│   │   └── impl/AiGenerateHistoryServiceImpl.java
│   ├── skill/                    # AI技能
│   │   ├── CogView3FlashSkill.java   # CogView3-Flash技能
│   │   └── CogVideoX3Skill.java       # CogVideoX-3技能
│   ├── manager/                  # 管理器
│   │   └── TaskManager.java      # 异步任务管理器
│   └── util/                     # 工具类
│       ├── ImageUtils.java
│       ├── RedisUtils.java
│       └── CommonUtils.java
└── src/main/resources/
    ├── application.yml           # 配置文件
    └── init_database.sql         # 数据库统一初始化脚本（包含所有表结构和初始数据）
```

## API 接口

### 1. 生成内容（文生图/文生视频/图生视频）
- **URL**: `POST /api/generate`
- **请求体**:
```json
{
    "prompt": "一只可爱的猫咪",
    "modelName": "cogview-3-flash",
    "generateType": "img",
    "imgUrl": null,
    "referenceImagePath": null,
    "saveDirectory": null,
    "imageSize": "1024x1024"
}
```
**参数说明：**
- `generateType`: 生成类型，可选值 `img`（文生图）、`text2video`（文生视频）、`img2video`（图生视频）
- `imgUrl`: 图生视频专用，上传图片的URL地址
- **响应**:
```json
{
    "code": 200,
    "message": "生成成功",
    "data": {
        "success": true,
        "message": "图片生成成功",
        "imageUrl": "https://...",
        "videoUrl": null,
        "coverUrl": "https://...",
        "generateType": "img",
        "localPath": "/path/to/image.png",
        "modelName": "cogview-3-flash"
    }
}
```

### 2. 异步生成（推荐用于视频生成）
- **URL**: `POST /api/generate-async`
- **请求体**: 同 `/api/generate`
- **响应**:
```json
{
    "code": 200,
    "message": "任务已创建",
    "data": {
        "taskId": "abc123xyz"
    }
}
```

### 3. 查询任务状态
- **URL**: `GET /api/task-status/{taskId}`
- **响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "taskId": "abc123xyz",
        "status": "PENDING|PROCESSING|COMPLETED|FAILED",
        "result": {...},
        "errorMessage": "错误信息（失败时）"
    }
}
```

### 4. 图片上传（图生视频专用）
- **URL**: `POST /api/upload`
- **Content-Type**: `multipart/form-data`
- **参数**: `file`（文件）
- **限制**: 
  - 支持格式：jpg、png、webp
  - 大小限制：20MB
- **响应**:
```json
{
    "code": 200,
    "message": "图片上传成功",
    "data": {
        "imgUrl": "http://your-domain.com/api/upload/images/20260220/abc123.png",
        "filename": "abc123.png"
    }
}
```
**说明**：返回的 `imgUrl` 是完整的公网可访问 URL，用于图生视频接口。

### 5. 获取模型列表
- **URL**: `GET /api/models?generateType=img`
- **参数**: `generateType`（可选，不传返回所有类型的模型）
- **响应**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "generateType": "img",
        "models": ["cogview-3-flash"],
        "defaultModel": "cogview-3-flash"
    }
}
```

### 6. 保存历史记录
- **URL**: `POST /api/save`
- **请求体**:
```json
{
    "prompt": "一只可爱的猫咪",
    "imageUrl": "https://...",
    "videoUrl": null,
    "coverUrl": "https://...",
    "modelName": "cogview-3-flash",
    "generateType": "img",
    "localPath": "/path/to/image.png",
    "imageSize": "1024x1024"
}
```

### 7. 查询历史记录
- **URL**: `GET /api/list?limit=20&generateType=img`
- **参数**:
  - `limit`: 查询数量，默认20
  - `generateType`: 生成类型筛选（可选）
- **排序**: 按 ID 倒序排序（最新记录在前）
- **响应**: 返回最近的历史记录列表

### 8. 删除历史记录
- **URL**: `DELETE /api/delete/{id}`

### 9. 查询详情
- **URL**: `GET /api/detail/{id}`

### 10. 健康检查
- **URL**: `GET /api/health`

### 11. 下载历史记录文件到本地
- **URL**: `POST /api/download/{id}`
- **说明**: 将历史记录中的图片或视频下载保存到本地服务器
- **响应**:
```json
{
    "code": 200,
    "message": "下载成功",
    "data": {
        "localPath": "D:/aigc-images/images/20260311/IMG_生成一只小狗.png",
        "message": "文件下载并保存成功"
    }
}
```
- **逻辑说明**:
  - 如果 `local_path` 已有值且文件存在，直接返回现有路径，不重复下载
  - 如果 `local_path` 为空或文件不存在，重新下载并更新数据库

### 12. 直接下载生成结果到本地
- **URL**: `POST /api/download-direct`
- **请求体**:
```json
{
    "fileUrl": "https://...",
    "fileType": "img",
    "prompt": "生成一只小狗"
}
```
- **说明**: 用于生成结果页面直接下载，不需要历史记录ID
- **响应**:
```json
{
    "code": 200,
    "message": "下载成功",
    "data": {
        "localPath": "D:/aigc-images/images/20260311/IMG_生成一只小狗.png",
        "message": "文件下载并保存成功"
    }
}
```

## 使用流程

### 生成结果保存流程

生成图片或视频后，有两种保存方式：

#### 方式一：先下载再保存（推荐）
```
1. 生成一张图片或视频
      ↓
2. 点击"下载结果"按钮 → 文件保存到本地服务器
      ↓
3. 点击"保存到历史记录" → local_path 字段自动填入
```
**优点**: `local_path` 字段会自动保存，后续可在历史记录中查看本地路径

#### 方式二：直接保存历史记录
```
1. 生成一张图片或视频
      ↓
2. 直接点击"保存到历史记录" → local_path 字段为空
      ↓
3. 如需本地文件，需在历史记录页面点击"下载"按钮
```
**说明**: 此方式 `local_path` 字段为空，需要后续在历史记录中单独下载

### 历史记录下载逻辑

在历史记录页面点击下载按钮时：
```
检查 local_path 是否有值？
    ├─ 有值 → 检查文件是否存在？
    │           ├─ 存在 → 直接返回，不重复下载
    │           └─ 不存在 → 重新下载并更新数据库
    └─ 无值 → 下载文件并更新数据库
```

### 文件保存路径

文件按类型和日期分类存储：
```
${user.home}/aigc-images/
├── images/                          # 图片目录
│   └── 20260311/                    # 日期子目录
│       ├── IMG_生成一只小狗.png      # 文件名：类型_提示词
│       └── IMG_生成一只小猫.png
└── videos/                          # 视频目录
    └── 20260311/
        └── VID_生成一个小狗.mp4
```

**文件命名规则**:
- 图片: `IMG_提示词摘要.png`
- 视频: `VID_提示词摘要.mp4`
- 提示词摘要取前15个字符，去除特殊字符

**避免重复下载**:
- 相同提示词生成的文件名相同
- 文件已存在时不会重复下载

## 快速启动

### 1. 获取智谱 AI API Key
1. 访问 [智谱 AI 开放平台](https://open.bigmodel.cn/)
2. 注册账号并登录
3. 在控制台获取 API Key

### 2. 初始化数据库
```bash
# 首次安装 - 执行统一初始化脚本
mysql -u root -p < src/main/resources/init_database.sql
```

**说明**：`init_database.sql` 是统一的数据库初始化脚本，包含：
- 创建所有表结构（user、system_config、ai_generate_history）
- 初始化默认配置和测试数据
- 添加所有必要的字段和索引
- 自动关联用户ID到默认管理员用户

**脚本功能**：
- 创建数据库（如果不存在）
- 创建用户表并添加默认管理员用户（用户名：admin，密码：admin123）
- 创建系统配置表并初始化默认配置项
- 创建AI生成历史记录表并添加测试数据
- 自动更新现有数据的关联字段

### 3. 配置环境变量（可选，优先级低于数据库配置）
**说明**：如果已在数据库 `system_config` 表中配置了 API Key 和公网地址，则无需设置环境变量。

```powershell
# Windows PowerShell
# 文生图模型配置
$env:ZHIPU_API_KEY="your-cogview-api-key"
$env:ZHIPU_BASE_URL="https://open.bigmodel.cn/api/paas/v4"
# 视频生成模型配置（可与文生图使用相同的 Key，也可单独配置）
$env:ZHIPU_VIDEO_API_KEY="your-cogvideo-api-key"
$env:ZHIPU_VIDEO_BASE_URL="https://open.bigmodel.cn/api/paas/v4"
```

```bash
# Linux/Mac
# 文生图模型配置
export ZHIPU_API_KEY="your-cogview-api-key"
export ZHIPU_BASE_URL="https://open.bigmodel.cn/api/paas/v4"
# 视频生成模型配置（可与文生图使用相同的 Key，也可单独配置）
export ZHIPU_VIDEO_API_KEY="your-cogvideo-api-key"
export ZHIPU_VIDEO_BASE_URL="https://open.bigmodel.cn/api/paas/v4"
```

### 4. 配置数据库（推荐，优先级最高）
**说明**：系统优先从 `system_config` 表读取配置，这是最推荐的配置方式。

**步骤**：
1. 启动服务后，系统会自动创建 `system_config` 表
2. 登录数据库，插入或更新配置：

```sql
-- 插入文生图配置
INSERT INTO system_config (config_key, config_value) VALUES 
('image.api.key', 'your-cogview-api-key'),
('image.api.url', 'https://open.bigmodel.cn/api/paas/v4'),
('image.model.name', 'cogview-3-flash'),
('video.api.key', 'your-cogvideo-api-key'),
('video.api.url', 'https://open.bigmodel.cn/api/paas/v4'),
('video.model.name', 'cogvideox-3'),
('server.public.url', 'http://your-domain.com');
```

### 5. 修改配置文件（可选，优先级最低）
编辑 `application.yml`，配置数据库、Redis 连接信息。**注意**：API Key 和公网地址会优先从数据库读取。

### 6. 启动服务
```bash
# cd aigc-backend
# mvn spring-boot:run
启动后端.bat # 启动后端服务
启动前端.bat # 启动前端服务
```

## 数据库配置说明

### 数据库表结构

#### 1. `user` 表
**用途**：存储系统用户信息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键，自增 |
| username | varchar(50) | 用户名（唯一） |
| password | varchar(255) | 密码（BCrypt加密） |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

**默认数据**：
- 用户名：admin
- 密码：admin123

#### 2. `system_config` 表
**用途**：存储系统配置信息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键，自增 |
| config_key | varchar(100) | 配置键名（唯一） |
| config_value | text | 配置值 |
| config_desc | varchar(500) | 配置描述 |
| user_id | bigint | 用户ID（关联到 user 表） |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

#### 3. `ai_generate_history` 表
**用途**：存储AI生成历史记录

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键，自增 |
| prompt | text | 提示词内容 |
| image_url | varchar(500) | 生成图片URL地址 |
| model_name | varchar(100) | 使用的模型名称 |
| local_path | varchar(500) | 图片本地保存路径 |
| image_size | varchar(50) | 图片尺寸 |
| generate_type | varchar(50) | 生成类型 |
| video_url | varchar(500) | 生成视频地址 |
| cover_url | varchar(500) | 视频封面/图片缩略图地址 |
| user_id | bigint | 用户ID（关联到 user 表） |
| create_time | datetime | 创建时间 |

### 核心配置项

| 配置键 | 说明 | 示例值 | 配置来源 |
|--------|------|--------|---------|
| `image.api.key` | 文生图 API Key | `your-cogview-api-key` | 数据库/环境变量 |
| `image.api.url` | 文生图 API 地址 | `https://open.bigmodel.cn/api/paas/v4` | 数据库/环境变量 |
| `image.model.name` | 文生图模型名称 | `cogview-3-flash` | 数据库/环境变量 |
| `video.api.key` | 视频生成 API Key | `your-cogvideo-api-key` | 数据库/环境变量 |
| `video.api.url` | 视频生成 API 地址 | `https://open.bigmodel.cn/api/paas/v4` | 数据库/环境变量 |
| `video.model.name` | 视频生成模型名称 | `cogvideox-3` | 数据库/环境变量 |
| `server.public.url` | 服务公网访问地址 | `http://your-domain.com` | 数据库/环境变量 |
| `frontend.tunnel.url` | 前端内网穿透地址 | `http://your-ngrok-url` | 数据库/环境变量 |
| `backend.tunnel.url` | 后端内网穿透地址 | `http://your-ngrok-url` | 数据库/环境变量 |

**注意**：以下配置项直接从 `application.yml` 读取，不走数据库配置：

| 配置项 | 说明 | 配置键 | 默认值 |
|--------|------|--------|--------|
| `image.save.path` | 图片/视频保存根路径 | `aigc.image.save-path` | `${user.home}/aigc-images` |
| `upload.path` | 上传图片保存路径 | `aigc.upload.path` | `./upload/images` |

**文件保存目录结构**：
```
${aigc.image.save-path}/          # 根目录（默认：用户主目录/aigc-images）
├── images/                        # 图片子目录
│   └── 20260311/                  # 日期子目录
│       └── IMG_提示词摘要.png      # 生成的图片文件
└── videos/                        # 视频子目录
    └── 20260311/
        └── VID_提示词摘要.mp4      # 生成的视频文件
```

**上传图片保存路径**：
```
${aigc.upload.path}/               # 上传图片根目录
└── 20260311/                      # 日期子目录
    └── xxxxxx.png                 # 上传的图片文件
```

### 配置优先级

**配置读取顺序（从高到低）：**
1. **数据库配置**（优先）：系统会先从 `system_config` 表读取配置
2. **环境变量**：如果数据库未配置，会读取系统环境变量
3. **application.yml**：如果以上都未配置，使用配置文件中的默认值

**注意**：数据库配置会覆盖环境变量和配置文件中的设置，这是最优先的配置方式。

## 配置说明

### application.yml 核心配置
```yaml
# ==================== 服务配置 ====================
server:
  port: 8080
  servlet:
    context-path: /api

# ==================== Spring 基础配置 ====================
spring:
  # 文件上传配置
  servlet:
    multipart:
      enabled: true
      max-file-size: 50MB
      max-request-size: 50MB
      file-size-threshold: 0

  # 数据源配置 - MySQL
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/videodb?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your-password
  
  # Redis 缓存配置
  data:
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0
      timeout: 10000ms
  
  # Spring AI 配置 - 用于 CogView-3-Flash 文生图
  ai:
    openai:
      api-key: ${ZHIPU_API_KEY:your-api-key}
      base-url: ${ZHIPU_BASE_URL:https://open.bigmodel.cn/api/paas/v4}
      image:
        options:
          model: cogview-3-flash
          size: 1024x1024

# ==================== AIGC 自定义配置 ====================
aigc:
  # 文生图相关配置
  image:
    # 图片和视频保存的根路径
    # 生成的图片会保存在：${save-path}/images/日期/ 目录下
    # 生成的视频会保存在：${save-path}/videos/日期/ 目录下
    save-path: d:/path/to/aigc-images
    default-size: 1024x1024
    model: cogview-3-flash
  
  # 图片上传配置（图生视频时使用）
  # 上传的图片会保存在：${path}/日期/ 目录下
  upload:
    path: d:/path/to/upload/images # 图片上传保存路径
  
  # 服务公网访问地址（用于生成外部可访问的图片URL）
  # 注意：请将此地址替换为你的实际公网访问地址或内网穿透地址
  # 例如：https://your-domain.com 或 http://xxx.ngrok.io
  # 优先级：数据库配置 > 环境变量 > 此配置文件
  server:
    public-url: http://your-domain.com
  
  # CogView 文生图模型独立配置（备用）
  # 优先级：数据库配置 > 环境变量 > 此配置文件
  cogview:
    api-key: ${ZHIPU_API_KEY:your-api-key}
    base-url: ${ZHIPU_BASE_URL:https://open.bigmodel.cn/api/paas/v4}
  
  # CogVideo 视频生成模型独立配置
  # 优先级：数据库配置 > 环境变量 > 此配置文件
  cogvideo:
    api-key: ${ZHIPU_VIDEO_API_KEY:your-api-key}
    base-url: ${ZHIPU_VIDEO_BASE_URL:https://open.bigmodel.cn/api/paas/v4}
```

### 重要配置说明

#### 1. 公网访问地址配置（图生视频必需）

**配置方式**（按优先级）：
1. **数据库配置**（推荐）：在 `system_config` 表中设置 `server.public.url`
2. **环境变量**：设置 `AIGC_SERVER_PUBLIC_URL` 环境变量
3. **application.yml**：在配置文件中设置 `aigc.server.public-url`

**application.yml 配置示例**：
```yaml
aigc:
  server:
    public-url: http://your-domain.com
```

**说明**：
- 智谱 AI 的图生视频接口需要公网可访问的图片 URL
- 如果在本地开发，需要使用内网穿透工具（如 ngrok、花生壳等）
- 配置示例：`http://1ov163849ga82.vicp.fun:59798`

#### 2. 内网穿透配置（开发环境）
**使用 ngrok**：
```bash
# 下载并安装 ngrok
# 运行命令，将 8080 端口暴露到公网
ngrok http 8080

# ngrok 会给你一个公网地址，例如：https://abc123.ngrok.io
# 将这个地址配置到 application.yml 中的 aigc.server.public-url
```

**使用花生壳**：
1. 下载并安装花生壳客户端
2. 配置内网映射，将本地 8080 端口映射到公网
3. 将分配的公网地址配置到 `aigc.server.public-url`

#### 3. 文件上传配置
```yaml
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 50MB        # 单个文件最大 50MB
      max-request-size: 50MB     # 请求最大 50MB
```

#### 4. 静态资源映射
```java
// WebConfig.java 中配置
registry.addResourceHandler("/upload/images/**")
        .addResourceLocations("file:" + uploadPath + "/");
```
**访问路径**：`http://localhost:8080/api/upload/images/20260225/abc.png`

### 各模型独立配置说明

**配置优先级**：数据库配置 > 环境变量 > 配置文件

| 技能类 | 配置前缀 | 说明 | 数据库配置键 |
|--------|----------|------|-------------|
| `CogView3FlashSkill` | `spring.ai.openai.` | 文生图模型，使用 Spring AI 默认配置 | `image.api.key`, `image.api.url` |
| `CogVideoX3Skill` | `aigc.cogvideo.` | 视频生成模型，使用独立配置分支 | `video.api.key`, `video.api.url` |

| 配置项 | 说明 | 使用位置 | 数据库配置键 |
|--------|------|----------|-------------|
| `spring.ai.openai.api-key` | 文生图模型的 API Key | CogView3FlashSkill | `image.api.key` |
| `spring.ai.openai.base-url` | 文生图模型的 API 地址 | CogView3FlashSkill | `image.api.url` |
| `aigc.cogvideo.api-key` | 视频生成模型的 API Key | CogVideoX3Skill | `video.api.key` |
| `aigc.cogvideo.base-url` | 视频生成模型的 API 地址 | CogVideoX3Skill | `video.api.url` |
| `aigc.server.public-url` | 服务公网访问地址 | AigcController（上传接口） | `server.public.url` |

### 环境变量配置
```powershell
# Windows PowerShell
# 文生图模型配置（给 CogView3FlashSkill 使用）
$env:ZHIPU_API_KEY="your-cogview-api-key"
$env:ZHIPU_BASE_URL="https://open.bigmodel.cn/api/paas/v4"
# 视频生成模型配置（给 CogVideoX3Skill 使用，可与文生图相同或不同）
$env:ZHIPU_VIDEO_API_KEY="your-cogvideo-api-key"
$env:ZHIPU_VIDEO_BASE_URL="https://open.bigmodel.cn/api/paas/v4"
```

```bash
# Linux/Mac
# 文生图模型配置（给 CogView3FlashSkill 使用）
export ZHIPU_API_KEY="your-cogview-api-key"
export ZHIPU_BASE_URL="https://open.bigmodel.cn/api/paas/v4"
# 视频生成模型配置（给 CogVideoX3Skill 使用，可与文生图相同或不同）
export ZHIPU_VIDEO_API_KEY="your-cogvideo-api-key"
export ZHIPU_VIDEO_BASE_URL="https://open.bigmodel.cn/api/paas/v4"
```

## 支持的图片尺寸
- 256x256
- 512x512
- 768x768
- 1024x1024 (默认)
- 1024x1792 (竖版)
- 1792x1024 (横版)

## 技能类功能

### CogView3FlashSkill（文生图）
- ✅ 支持提示词生成图片
- ✅ 支持提示词 + 参考图生成图片
- ✅ 支持保存图片到指定目录
- ✅ 支持多种图片尺寸
- ✅ 完善的异常处理和日志记录
- ✅ 中文提示词优化

### CogVideoX3Skill（视频生成）
- ✅ 支持文生视频（text2video）
- ✅ 支持图生视频（img2video）
- ✅ 返回视频URL和封面URL
- ✅ 完善的异常处理和日志记录
- ✅ 异步任务支持，避免超时
- ✅ 轮询超时时间：10分钟（可配置）

## ModelConfig 模型配置管理
- ✅ 按生成类型分类管理模型集合
- ✅ 提供获取模型列表、默认模型、检查模型是否支持等工具方法
- ✅ 新增/切换模型仅修改此配置类，无需改动业务代码

## 数据库表结构

### ai_generate_history 表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键，自增 |
| prompt | text | 提示词内容 |
| image_url | varchar(500) | 生成图片地址 |
| video_url | varchar(500) | 生成视频地址 |
| cover_url | varchar(500) | 视频封面/图片缩略图地址 |
| model_name | varchar(100) | 使用的模型名称 |
| generate_type | varchar(50) | 生成类型 |
| local_path | varchar(500) | 图片本地保存路径 |
| image_size | varchar(50) | 图片尺寸 |
| create_time | datetime | 创建时间 |

## 项目亮点

### 原有亮点（保留）
- 使用免费的智谱 AI 模型，无需付费
- 国内可直接访问，无需翻墙
- 基于 Spring Boot + Spring AI 实现 AI 模型统一接入
- 实现图片生成、历史记录、回填、删除完整业务
- 前后端分离，接口标准化，可直接部署演示
- 技术栈主流，符合企业 AI 应用开发岗位要求

### 新增亮点
- ✨ **多模态一站式生成**：支持文生图、文生视频、图生视频三种方式
- ✨ **多模型动态管理**：后端维护模型集合，前端下拉框联动渲染
- ✨ **独立配置分支**：每个模型独立 API Key 和 Base URL，互不干扰，便于扩展
- ✨ **无侵入式升级**：在已完成文生图功能基础上轻量扩展，复用原有核心代码
- ✨ **模型精准适配**：生成类型与模型自动匹配，前端无冗余操作
- ✨ **兼容式数据存储**：原有文生图数据无丢失，新增字段自动赋值
- ✨ **异步任务支持**：视频生成使用异步任务，避免请求超时
- ✨ **公网 URL 生成**：自动生成公网可访问的图片 URL，支持图生视频
- ✅ **静态资源映射**：支持本地图片通过 HTTP 访问
- ✅ **文件上传优化**：支持最大 20MB 图片上传
- ✅ **超时配置优化**：视频生成轮询超时时间设置为 10 分钟

## 常见问题

### Q: 图生视频时提示"图片URL无法访问"怎么办？
A: 需要配置 `aigc.server.public-url` 为公网可访问的地址。如果是本地开发，使用内网穿透工具（ngrok、花生壳等）。

### Q: 上传图片大小限制是多少？
A: 支持最大 20MB 的图片上传，格式支持 jpg、png、webp。

### Q: 视频生成超时怎么办？
A: 视频生成使用异步任务，轮询超时时间设置为 10 分钟。如需调整，修改 `CogVideoX3Skill.java` 中的 `MAX_RETRIES` 和 `POLL_INTERVAL`。

### Q: 如何切换模型？
A: 修改 `ModelConfig.java` 中的模型配置，无需改动业务代码。
