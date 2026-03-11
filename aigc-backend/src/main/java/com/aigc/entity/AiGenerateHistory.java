package com.aigc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI图片生成历史记录实体类
 * 
 * <p>该实体类对应数据库表 ai_generate_history，用于存储用户通过AI模型生成图片的历史记录。
 * 每条记录包含提示词、生成的图片URL、使用的模型名称、本地存储路径等关键信息。</p>
 * 
 * <p>设计思路：</p>
 * <ul>
 *   <li>使用 MyBatis-Plus 注解实现对象关系映射</li>
 *   <li>主键采用自增策略，确保记录唯一性</li>
 *   <li>创建时间自动记录，便于按时间排序查询</li>
 * </ul>
 * 
 * @author AIGC Platform
 * @version 1.0.0
 */
@Data
@TableName("ai_generate_history")
public class AiGenerateHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * 
     * <p>采用数据库自增策略，确保每条记录的唯一标识。</p>
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 提示词内容
     * 
     * <p>用户输入的用于生成图片的文本描述，是AI模型生成图片的核心输入参数。
     * 支持中英文提示词，建议使用详细、具体的描述以获得更好的生成效果。</p>
     */
    @TableField("prompt")
    private String prompt;

    /**
     * 生成图片的URL地址
     * 
     * <p>AI模型生成图片后返回的远程访问地址，通常是云存储服务的URL。
     * 该URL可能有时效性限制，建议及时下载保存。</p>
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * 使用的AI模型名称
     * 
     * <p>记录生成该图片所使用的AI模型，如 "cogview-3-flash"。
     * 便于后续统计分析不同模型的使用情况和生成效果。</p>
     */
    @TableField("model_name")
    private String modelName;

    /**
     * 创建时间
     * 
     * <p>记录的创建时间戳，用于按时间倒序查询历史记录。
     * 默认值由数据库自动设置为当前时间。</p>
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 图片本地保存路径
     * 
     * <p>如果用户选择将图片保存到本地服务器，该字段记录完整的文件系统路径。
     * 格式示例：/home/user/aigc-images/20240101/abc123.png</p>
     */
    @TableField("local_path")
    private String localPath;

    /**
     * 图片尺寸
     * 
     * <p>生成图片的像素尺寸，格式为 "宽x高"，如 "1024x1024"。
     * 支持的尺寸取决于所使用的AI模型。</p>
     */
    @TableField("image_size")
    private String imageSize;

    /**
     * 用户ID
     * 
     * <p>关联的用户ID，用于数据隔离。</p>
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 生成类型
     * 
     * <p>记录生成内容的类型，支持三种类型：
     * <ul>
     *   <li>img: 文生图</li>
     *   <li>text2video: 文生视频</li>
     *   <li>img2video: 图生视频</li>
     * </ul>
     * </p>
     */
    @TableField("generate_type")
    private String generateType;

    /**
     * 生成视频的URL地址
     * 
     * <p>AI模型生成视频后返回的远程访问地址，
     * 仅在生成类型为 text2video 或 img2video 时有值。</p>
     */
    @TableField("video_url")
    private String videoUrl;

    /**
     * 视频封面/图片缩略图地址
     * 
     * <p>对于文生图，该值与 image_url 相同；
     * 对于视频生成，该值为视频的封面缩略图URL。</p>
     */
    @TableField("cover_url")
    private String coverUrl;
}
