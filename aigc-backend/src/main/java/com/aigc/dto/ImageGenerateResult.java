package com.aigc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 图片生成结果DTO
 * 
 * <p>该类封装了AI模型生成图片后的返回结果，包含生成状态、图片URL、本地路径等信息。
 * 采用建造者模式，便于构建复杂对象。</p>
 * 
 * <p>设计思路：</p>
 * <ul>
 *   <li>使用 success 字段快速判断生成是否成功</li>
 *   <li>提供静态工厂方法 success() 和 fail() 简化对象创建</li>
 *   <li>支持链式调用，提高代码可读性</li>
 * </ul>
 * 
 * @author AIGC Platform
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerateResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 生成是否成功
     * 
     * <p>true 表示图片生成成功，false 表示生成失败。
     * 失败时请查看 message 字段获取具体错误信息。</p>
     */
    private Boolean success;

    /**
     * 结果消息
     * 
     * <p>成功时返回 "图片生成成功"，失败时返回具体的错误原因。
     * 前端可直接展示该消息给用户。</p>
     */
    private String message;

    /**
     * 生成图片的URL地址
     * 
     * <p>AI模型返回的图片远程访问地址。
     * 注意：该URL可能有时效性限制，建议及时下载保存。</p>
     */
    private String imageUrl;

    /**
     * 图片本地保存路径
     * 
     * <p>如果请求中指定了 saveDirectory，该字段返回图片在服务器上的完整路径。
     * 可用于后续的图片管理、删除等操作。</p>
     */
    private String localPath;

    /**
     * 使用的模型名称
     * 
     * <p>记录实际生成图片所使用的AI模型名称，
     * 便于用户了解图片来源和进行统计分析。</p>
     */
    private String modelName;

    /**
     * 生成类型
     * 
     * <p>记录生成内容的类型：img-文生图，text2video-文生视频，img2video-图生视频。</p>
     */
    private String generateType;

    /**
     * 生成视频的URL地址
     * 
     * <p>AI模型返回的视频远程访问地址，
     * 仅在生成类型为 text2video 或 img2video 时有值。</p>
     */
    private String videoUrl;

    /**
     * 视频封面/图片缩略图地址
     * 
     * <p>对于文生图，该值与 imageUrl 相同；
     * 对于视频生成，该值为视频的封面缩略图URL。</p>
     */
    private String coverUrl;

    /**
     * 创建成功结果（图片）
     * 
     * <p>静态工厂方法，用于快速创建成功的图片生成结果对象。</p>
     * 
     * @param imageUrl  生成图片的URL地址
     * @param localPath 图片本地保存路径（可为null）
     * @param modelName 使用的模型名称
     * @return 构建完成的 ImageGenerateResult 对象
     */
    public static ImageGenerateResult success(String imageUrl, String localPath, String modelName) {
        return ImageGenerateResult.builder()
                .success(true)
                .message("图片生成成功")
                .imageUrl(imageUrl)
                .localPath(localPath)
                .modelName(modelName)
                .generateType("img")
                .coverUrl(imageUrl)
                .build();
    }

    /**
     * 创建成功结果（视频）
     * 
     * <p>静态工厂方法，用于快速创建成功的视频生成结果对象。</p>
     * 
     * @param videoUrl  生成视频的URL地址
     * @param coverUrl  视频封面URL地址
     * @param modelName 使用的模型名称
     * @param generateType 生成类型
     * @return 构建完成的 ImageGenerateResult 对象
     */
    public static ImageGenerateResult successVideo(String videoUrl, String coverUrl, String modelName, String generateType) {
        return ImageGenerateResult.builder()
                .success(true)
                .message("视频生成成功")
                .videoUrl(videoUrl)
                .coverUrl(coverUrl)
                .modelName(modelName)
                .generateType(generateType)
                .build();
    }

    /**
     * 创建失败结果
     * 
     * <p>静态工厂方法，用于快速创建失败的生成结果对象。</p>
     * 
     * @param message 失败原因描述
     * @return 构建完成的 ImageGenerateResult 对象
     */
    public static ImageGenerateResult fail(String message) {
        return ImageGenerateResult.builder()
                .success(false)
                .message(message)
                .build();
    }
}
