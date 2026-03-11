-- AIGC 多模态平台数据库统一初始化脚本
-- 执行此脚本可完成所有表的创建和配置

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS videodb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE videodb;

-- 1. 创建用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入默认管理员用户（密码：admin123）
INSERT IGNORE INTO user (username, password) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVwE.');

-- 2. 创建系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_desc VARCHAR(500) COMMENT '配置描述',
    user_id BIGINT COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入默认配置
INSERT IGNORE INTO system_config (config_key, config_value, config_desc, user_id) VALUES
('image.api.url', 'https://open.bigmodel.cn/api/paas/v4', '文生图模型API地址', 1),
('image.api.key', '', '文生图API密钥', 1),
('image.model.name', 'cogview-3-flash', '文生图模型名称', 1),
('video.api.url', 'https://open.bigmodel.cn/api/paas/v4', '视频生成模型API地址', 1),
('video.api.key', '', '视频生成API密钥', 1),
('video.model.name', 'cogvideox-3', '视频生成模型名称', 1),
('frontend.tunnel.url', '', '前端内网穿透地址', 1),
('backend.tunnel.url', '', '后端内网穿透地址', 1),
('server.public.url', '', '服务公网访问地址', 1);

-- 3. 创建AI生成历史记录表
CREATE TABLE IF NOT EXISTS ai_generate_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    prompt TEXT NOT NULL COMMENT '提示词内容',
    image_url VARCHAR(500) NOT NULL COMMENT '生成图片URL地址',
    model_name VARCHAR(100) NOT NULL DEFAULT 'cogview-3-flash' COMMENT '使用的模型名称',
    local_path VARCHAR(500) DEFAULT NULL COMMENT '图片本地保存路径',
    image_size VARCHAR(50) DEFAULT '1024x1024' COMMENT '图片尺寸',
    generate_type VARCHAR(50) DEFAULT 'img' COMMENT '生成类型：img-文生图，text2video-文生视频，img2video-图生视频',
    video_url VARCHAR(500) DEFAULT '' COMMENT '生成视频地址',
    cover_url VARCHAR(500) DEFAULT '' COMMENT '视频封面/图片缩略图地址',
    user_id BIGINT COMMENT '用户ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_create_time (create_time DESC),
    INDEX idx_model_name (model_name),
    INDEX idx_generate_type (generate_type),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI图片生成历史记录表';

-- 插入测试数据
INSERT IGNORE INTO ai_generate_history (prompt, image_url, model_name, image_size, generate_type, cover_url, user_id) VALUES
('一只可爱的猫咪在阳光下打盹', 'https://example.com/images/cat.png', 'cogview-3-flash', '1024x1024', 'img', 'https://example.com/images/cat.png', 1),
('未来城市的夜景，霓虹灯闪烁', 'https://example.com/images/city.png', 'cogview-3-flash', '1024x1024', 'img', 'https://example.com/images/city.png', 1),
('山水画风格的瀑布风景', 'https://example.com/images/waterfall.png', 'cogview-3-flash', '1024x1024', 'img', 'https://example.com/images/waterfall.png', 1);

-- 4. 更新现有数据（如果存在）
-- 为已有文生图数据更新 cover_url 为 image_url 的值
UPDATE ai_generate_history 
SET cover_url = image_url 
WHERE cover_url = '' OR cover_url IS NULL;

-- 更新现有数据的用户ID
UPDATE ai_generate_history SET user_id = 1 WHERE user_id IS NULL;

-- 更新现有配置数据的用户ID
UPDATE system_config SET user_id = 1 WHERE user_id IS NULL;

-- 完成初始化
SELECT '数据库初始化完成！' AS message;