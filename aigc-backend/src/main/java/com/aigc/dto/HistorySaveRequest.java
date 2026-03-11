package com.aigc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 历史记录保存请求DTO
 * 
 * <p>该类封装了保存图片生成历史记录所需的请求参数。
 * 用于将用户满意的生成结果持久化到数据库，便于后续查看和管理。</p>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>用户生成图片后，选择保存到历史记录</li>
 *   <li>批量导入历史数据</li>
 *   <li>同步其他平台的生成记录</li>
 * </ul>
 * 
 * @author AIGC Platform
 * @version 1.0.0
 */
@Data
public class HistorySaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 提示词（必填）
     * 
     * <p>生成该图片时使用的提示词内容。
     * 保存提示词便于用户后续复用或修改后重新生成。</p>
     */
    @NotBlank(message = "提示词不能为空")
    private String prompt;

    /**
     * 图片URL（图片类型必填）
     * 
     * <p>生成图片的远程访问地址。
     * 作为历史记录的核心内容，用于展示和下载图片。</p>
     */
    private String imageUrl;

    /**
     * 模型名称（可选）
     * 
     * <p>生成该图片所使用的AI模型名称。
     * 如果不指定，系统将使用默认模型名称 "cogview-3-flash"。</p>
     */
    private String modelName;

    /**
     * 本地保存路径（可选）
     * 
     * <p>如果图片已保存到本地服务器，记录其完整路径。
     * 便于后续的文件管理和清理操作。</p>
     */
    private String localPath;

    /**
     * 图片尺寸（可选）
     * 
     * <p>生成图片的像素尺寸，格式为 "宽x高"。
     * 记录尺寸信息便于用户了解图片规格。</p>
     */
    private String imageSize;

    /**
     * 生成类型（可选）
     * 
     * <p>记录生成内容的类型，支持三种类型：
     * <ul>
     *   <li>img: 文生图（默认）</li>
     *   <li>text2video: 文生视频</li>
     *   <li>img2video: 图生视频</li>
     * </ul>
     * </p>
     */
    private String generateType;

    /**
     * 视频URL（可选，仅视频类型需要）
     * 
     * <p>生成视频的远程访问地址，
     * 仅在生成类型为 text2video 或 img2video 时有值。</p>
     */
    private String videoUrl;

    /**
     * 封面URL（可选）
     * 
     * <p>对于文生图，该值与 imageUrl 相同；
     * 对于视频生成，该值为视频的封面缩略图URL。</p>
     */
    private String coverUrl;
}
