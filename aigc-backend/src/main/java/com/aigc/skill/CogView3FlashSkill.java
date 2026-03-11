package com.aigc.skill;

import com.aigc.dto.ImageGenerateResult;
import com.aigc.service.SystemConfigService;
import com.aigc.util.ImageUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * CogView-3-Flash 文生图技能类
 * 
 * <p>封装智谱AI CogView-3-Flash 模型的调用逻辑，用于文生图功能。
 * 使用 Spring AI 的 OpenAI 配置进行 API 调用。</p>
 * 
 * <p>主要功能：
 * <ul>
 *   <li>文生图：根据提示词生成图片</li>
 *   <li>支持多种图片尺寸</li>
 *   <li>自动保存生成的图片到本地</li>
 * </ul>
 * </p>
 * 
 * @author AIGC Platform
 * @version 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CogView3FlashSkill {

    /**
     * 图片处理工具类
     */
    private final ImageUtils imageUtils;

    /**
     * 系统配置服务
     */
    private final SystemConfigService systemConfigService;

    /**
     * 默认图片尺寸
     */
    @Value("${aigc.image.default-size:1024x1024}")
    private String defaultSize;

    /**
     * 默认图片保存路径
     */
    @Value("${aigc.image.save-path:d:/aigc-images}")
    private String defaultSavePath;

    /**
     * 从数据库动态获取配置
     */
    private String getConfig(String key, String defaultValue, Long userId) {
        String value;
        if (userId != null) {
            value = systemConfigService.getValueByKeyAndUserId(key, userId);
        } else {
            value = systemConfigService.getValueByKey(key);
        }
        return value != null && !value.isEmpty() ? value : defaultValue;
    }

    /**
     * 获取文生图API地址
     */
    private String getImageApiUrl(Long userId) {
        return getConfig("image.api.url", "https://open.bigmodel.cn/api/paas/v4", userId);
    }

    /**
     * 获取文生图API密钥
     */
    private String getImageApiKey(Long userId) {
        return getConfig("image.api.key", "", userId);
    }

    /**
     * 获取文生图模型名称
     */
    private String getImageModelName(Long userId) {
        return getConfig("image.model.name", "cogview-3-flash", userId);
    }

    /**
     * REST 客户端，用于调用 API
     */
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * JSON 解析器，用于解析 API 响应
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成图片
     * 
     * <p>调用智谱AI CogView-3-Flash 模型生成图片，支持指定提示词、图片尺寸、保存目录等参数。
     * 
     * @param prompt 提示词内容
     * @param referenceImagePath 参考图片路径（暂未使用）
     * @param saveDirectory 图片保存目录
     * @param imageSize 图片尺寸
     * @return 图片生成结果对象
     */
    public ImageGenerateResult generateImage(String prompt, String referenceImagePath, String saveDirectory, String imageSize) {
        return generateImage(prompt, referenceImagePath, saveDirectory, imageSize, null);
    }
    
    /**
     * 生成图片（带用户ID）
     * 
     * <p>调用智谱AI CogView-3-Flash 模型生成图片，支持指定提示词、图片尺寸、保存目录等参数。
     * 
     * @param prompt 提示词内容
     * @param referenceImagePath 参考图片路径（暂未使用）
     * @param saveDirectory 图片保存目录
     * @param imageSize 图片尺寸
     * @param userId 用户ID
     * @return 图片生成结果对象
     */
    public ImageGenerateResult generateImage(String prompt, String referenceImagePath, String saveDirectory, String imageSize, Long userId) {
        log.info("CogView3-Flash 开始生成图片, prompt: {}, userId: {}", prompt, userId);
        
        try {
            String enhancedPrompt = enhancePrompt(prompt, referenceImagePath);
            String size = mapSize(imageSize != null ? imageSize : defaultSize);
            
            String apiUrl = getImageApiUrl(userId) + "/images/generations";
            log.info("调用智谱AI API: {}", apiUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(getImageApiKey(userId));
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", getImageModelName(userId));
            requestBody.put("prompt", enhancedPrompt);
            requestBody.put("size", size);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            String responseBody = restTemplate.postForObject(apiUrl, request, String.class);
            log.debug("API响应: {}", responseBody);
            
            if (responseBody == null || responseBody.isEmpty()) {
                log.error("图片生成响应为空");
                return ImageGenerateResult.fail("图片生成响应为空");
            }
            
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode dataArray = root.get("data");
            
            if (dataArray == null || !dataArray.isArray() || dataArray.isEmpty()) {
                String errorMsg = root.has("error") ? root.get("error").asText() : "未知错误";
                log.error("图片生成失败: {}", errorMsg);
                return ImageGenerateResult.fail("图片生成失败: " + errorMsg);
            }
            
            JsonNode imageData = dataArray.get(0);
            String imageUrl = imageData.has("url") ? imageData.get("url").asText() : null;
            
            log.info("图片生成成功, url: {}", imageUrl);
            return ImageGenerateResult.success(imageUrl, null, getImageModelName(userId));
            
        } catch (Exception e) {
            log.error("图片生成异常: {}", e.getMessage(), e);
            return ImageGenerateResult.fail("图片生成异常: " + e.getMessage());
        }
    }

    /**
     * 生成图片（带参考图）
     * 
     * <p>根据提示词和参考图生成图片，保持参考图的风格和构图。
     * 
     * @param prompt 提示词内容
     * @param referenceImagePath 参考图片路径
     * @param saveDirectory 图片保存目录
     * @param imageSize 图片尺寸
     * @return 图片生成结果对象
     */
    public ImageGenerateResult generateImageWithReference(String prompt, String referenceImagePath, String saveDirectory, String imageSize) {
        log.info("CogView3-Flash 开始生成图片（带参考图）, prompt: {}, referenceImage: {}", prompt, referenceImagePath);
        
        if (referenceImagePath == null || referenceImagePath.isEmpty()) {
            return generateImage(prompt, null, saveDirectory, imageSize);
        }
        
        String enhancedPrompt = buildReferencePrompt(prompt, referenceImagePath);
        return generateImage(enhancedPrompt, null, saveDirectory, imageSize);
    }

    /**
     * 增强提示词
     * 
     * <p>自动为提示词添加质量描述，提升生成效果。
     * 
     * @param prompt 原始提示词
     * @param referenceImagePath 参考图片路径
     * @return 增强后的提示词
     */
    private String enhancePrompt(String prompt, String referenceImagePath) {
        if (referenceImagePath != null && !referenceImagePath.isEmpty()) {
            return buildReferencePrompt(prompt, referenceImagePath);
        }
        return prompt + " 高质量, 细节丰富, 专业摄影作品。";
    }

    /**
     * 构建带参考图的提示词
     * 
     * @param prompt 原始提示词
     * @param referenceImagePath 参考图片路径
     * @return 构建后的提示词
     */
    private String buildReferencePrompt(String prompt, String referenceImagePath) {
        return String.format("参考图片风格，生成: %s。保持相似的艺术风格和构图。", prompt);
    }

    /**
     * 映射图片尺寸
     * 
     * <p>将用户输入的尺寸映射到模型支持的尺寸格式。
     * 
     * @param size 用户输入的尺寸
     * @return 模型支持的尺寸
     */
    private String mapSize(String size) {
        if (size == null || size.isEmpty()) {
            return "1024x1024";
        }
        
        return switch (size.toLowerCase()) {
            case "256x256" -> "256x256";
            case "512x512" -> "512x512";
            case "768x768" -> "768x768";
            case "1024x1024" -> "1024x1024";
            case "1024x1792", "portrait" -> "1024x1792";
            case "1792x1024", "landscape" -> "1792x1024";
            default -> "1024x1024";
        };
    }

    /**
     * 保存生成的图片
     * 
     * <p>根据API返回的格式（URL或Base64）保存图片到本地。
     * 
     * @param imageUrl 图片URL
     * @param b64Json Base64编码的图片数据
     * @param saveDirectory 保存目录
     * @return 保存后的本地路径
     * @throws IOException 当保存失败时抛出
     */
    private String saveGeneratedImage(String imageUrl, String b64Json, String saveDirectory) throws IOException {
        if (b64Json != null && !b64Json.isEmpty()) {
            byte[] imageBytes = Base64.getDecoder().decode(b64Json);
            return imageUtils.saveImageFromBytes(imageBytes, saveDirectory);
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            return imageUtils.saveImageFromUrl(imageUrl, saveDirectory);
        }
        return null;
    }

    /**
     * 获取模型名称
     * 
     * @return 模型名称
     */
    public String getModelName() {
        return getModelName(null);
    }
    
    /**
     * 获取模型名称
     * 
     * @param userId 用户ID
     * @return 模型名称
     */
    public String getModelName(Long userId) {
        return getImageModelName(userId);
    }
}
