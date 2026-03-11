package com.aigc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 图片生成请求DTO
 * 
 * <p>该类封装了调用AI模型生成图片所需的所有请求参数。
 * 作为前端与后端交互的数据传输对象，确保请求参数的完整性和有效性。</p>
 * 
 * <p>使用示例：</p>
 * <pre>
 * ImageGenerateRequest request = new ImageGenerateRequest();
 * request.setPrompt("一只可爱的猫咪在阳光下打盹");
 * request.setModelName("cogview-3-flash");
 * request.setImageSize("1024x1024");
 * </pre>
 * 
 * @author AIGC Platform
 * @version 1.0.0
 */
@Data
public class ImageGenerateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 提示词（必填）
     * 
     * <p>用于描述期望生成图片内容的文本，是AI模型生成图片的核心输入。
     * 建议使用详细、具体的描述，包括主体、风格、颜色、构图等要素。</p>
     * 
     * <p>示例：</p>
     * <ul>
     *   <li>"一只橙色的猫咪在阳光下打盹，毛茸茸的，温馨的氛围"</li>
     *   <li>"未来科技城市夜景，霓虹灯闪烁，赛博朋克风格"</li>
     * </ul>
     */
    @NotBlank(message = "提示词不能为空")
    private String prompt;

    /**
     * 模型名称（可选）
     * 
     * <p>指定使用的AI模型，如 "cogview-3-flash"。
     * 如果不指定，将使用系统默认配置的模型。</p>
     */
    private String modelName;

    /**
     * 参考图片路径（可选）
     * 
     * <p>提供参考图片的本地路径，AI模型将参考该图片的风格进行生成。
     * 适用于风格迁移、图生图等场景。</p>
     */
    private String referenceImagePath;

    /**
     * 图片保存目录（可选）
     * 
     * <p>指定生成图片的本地保存目录。如果不指定，图片仅返回URL不保存到本地。
     * 系统会自动在指定目录下创建日期子目录进行分类存储。</p>
     */
    private String saveDirectory;

    /**
     * 图片尺寸（可选）
     * 
     * <p>指定生成图片的像素尺寸，格式为 "宽x高"。</p>
     * <p>支持的尺寸：</p>
     * <ul>
     *   <li>"256x256" - 小尺寸</li>
     *   <li>"512x512" - 中等尺寸</li>
     *   <li>"768x768" - 较大尺寸</li>
     *   <li>"1024x1024" - 标准尺寸（默认）</li>
     *   <li>"1024x1792" - 竖版</li>
     *   <li>"1792x1024" - 横版</li>
     * </ul>
     */
    private String imageSize;

    /**
     * 生成类型（可选）
     * 
     * <p>指定生成内容的类型，支持三种类型：
     * <ul>
     *   <li>img: 文生图（默认）</li>
     *   <li>text2video: 文生视频</li>
     *   <li>img2video: 图生视频</li>
     * </ul>
     * </p>
     */
    private String generateType;

    /**
     * 参考图片URL（可选，仅图生视频需要）
     * 
     * <p>图生视频功能需要上传的参考图片URL，
     * 通过 /api/upload 接口上传图片后获得该URL。</p>
     */
    private String imgUrl;

    /**
     * 参考图片本地路径（可选，仅图生视频需要）
     * 
     * <p>图生视频功能上传的参考图片在服务器上的本地路径，
     * 用于视频生成完成后删除临时文件。</p>
     */
    private String imgLocalPath;
}
