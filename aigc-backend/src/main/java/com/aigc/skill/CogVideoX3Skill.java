package com.aigc.skill;

import com.aigc.dto.ImageGenerateResult;
import com.aigc.service.SystemConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * CogVideoX-3 视频生成技能类
 * 
 * <p>封装智谱AI CogVideoX-3 模型的调用逻辑，支持文生视频和图生视频两种模式。
 * 使用独立的 aigc.cogvideo 配置进行 API 调用。
 * 采用异步轮询模式：先生成任务，然后轮询查询结果。</p>
 * 
 * <p>主要功能：
 * <ul>
 *   <li>文生视频：根据提示词生成视频</li>
 *   <li>图生视频：根据图片和提示词生成视频</li>
 * </ul>
 * </p>
 * 
 * @author AIGC Platform
 * @version 3.1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CogVideoX3Skill {

    /**
     * 系统配置服务
     */
    private final SystemConfigService systemConfigService;

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
     * 获取视频生成API地址
     */
    private String getVideoApiUrl(Long userId) {
        return getConfig("video.api.url", "https://open.bigmodel.cn/api/paas/v4", userId);
    }

    /**
     * 获取视频生成API密钥
     */
    private String getVideoApiKey(Long userId) {
        return getConfig("video.api.key", "", userId);
    }

    /**
     * 获取视频生成模型名称
     */
    private String getVideoModelName(Long userId) {
        return getConfig("video.model.name", "cogvideox-3", userId);
    }

    /**
     * 最大轮询次数
     * 当前设置：120次 × 5秒 = 600秒 = 10分钟
     * 如需修改超时时间，请调整此数值
     */
    private static final int MAX_RETRIES = 300;

    /**
     * 轮询间隔（毫秒）
     * 当前设置：5000毫秒 = 5秒
     * 如需调整轮询频率，请修改此数值
     */
    private static final long POLL_INTERVAL = 5000;

    /**
     * REST 客户端，用于调用 API
     */
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * JSON 解析器，用于解析 API 响应
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成视频
     * 
     * <p>调用智谱AI CogVideoX-3 模型生成视频，支持两种模式：
     * - text2video: 文生视频（仅使用 prompt）
     * - img2video: 图生视频（使用 prompt + imgUrl）
     * 
     * <p>采用异步轮询模式：
     * 1. 调用生成接口，获取 task_id
     * 2. 轮询查询接口，检查 task_status
     * 3. 等待直到状态变为 SUCCESS 或 FAIL
     * 
     * @param prompt 提示词内容
     * @param imgUrl 参考图片URL（仅图生视频需要）
     * @param generateType 生成类型：text2video 或 img2video
     * @return 视频生成结果对象
     */
    public ImageGenerateResult generateVideo(String prompt, String imgUrl, String generateType) {
        return generateVideo(prompt, imgUrl, generateType, null);
    }
    
    /**
     * 生成视频（带用户ID）
     * 
     * <p>调用智谱AI CogVideoX-3 模型生成视频，支持两种模式：
     * - text2video: 文生视频（仅使用 prompt）
     * - img2video: 图生视频（使用 prompt + imgUrl）
     * 
     * <p>采用异步轮询模式：
     * 1. 调用生成接口，获取 task_id
     * 2. 轮询查询接口，检查 task_status
     * 3. 等待直到状态变为 SUCCESS 或 FAIL
     * 
     * @param prompt 提示词内容
     * @param imgUrl 参考图片URL（仅图生视频需要）
     * @param generateType 生成类型：text2video 或 img2video
     * @param userId 用户ID
     * @return 视频生成结果对象
     */
    public ImageGenerateResult generateVideo(String prompt, String imgUrl, String generateType, Long userId) {
        log.info("CogVideoX-3 开始生成视频, type: {}, prompt: {}, imgUrl: {}, userId: {}", generateType, prompt, imgUrl, userId);
        
        try {
            String taskId = createVideoGenerationTask(prompt, imgUrl, generateType, userId);
            log.info("视频生成任务已创建, taskId: {}", taskId);
            
            JsonNode finalResult = pollForResult(taskId, userId);
            log.info("视频生成任务完成, taskId: {}", taskId);
            
            return parseVideoResult(finalResult, generateType, userId);
            
        } catch (Exception e) {
            log.error("视频生成异常: {}", e.getMessage(), e);
            return ImageGenerateResult.fail("视频生成异常: " + e.getMessage());
        }
    }

    /**
     * 创建视频生成任务
     * 
     * @param prompt 提示词
     * @param imgUrl 参考图片URL
     * @param generateType 生成类型
     * @param userId 用户ID
     * @return 任务ID
     */
    private String createVideoGenerationTask(String prompt, String imgUrl, String generateType, Long userId) {
        String apiUrl = getVideoApiUrl(userId) + "/videos/generations";
        log.info("调用智谱AI视频生成API: {}", apiUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getVideoApiKey(userId));
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", getVideoModelName(userId));
        requestBody.put("prompt", prompt);
        
        if ("img2video".equals(generateType) && imgUrl != null && !imgUrl.isEmpty()) {
            requestBody.put("image_url", imgUrl);
        }
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        String responseBody = restTemplate.postForObject(apiUrl, request, String.class);
        log.debug("生成任务API响应: {}", responseBody);
        
        if (responseBody == null || responseBody.isEmpty()) {
            throw new RuntimeException("视频生成响应为空");
        }
        
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            if (root.has("error")) {
                String errorMsg = root.get("error").has("message") 
                    ? root.get("error").get("message").asText() 
                    : "未知错误";
                throw new RuntimeException("视频生成任务创建失败: " + errorMsg);
            }
            
            String taskId;
            if (root.has("id")) {
                taskId = root.get("id").asText();
            } else if (root.has("data")) {
                JsonNode dataNode = root.get("data");
                if (dataNode.isArray() && dataNode.size() > 0) {
                    taskId = dataNode.get(0).has("id") ? dataNode.get(0).get("id").asText() : null;
                } else {
                    taskId = dataNode.has("id") ? dataNode.get("id").asText() : null;
                }
            } else {
                throw new RuntimeException("无法从响应中获取任务ID");
            }
            
            if (taskId == null || taskId.isEmpty()) {
                throw new RuntimeException("无法获取任务ID");
            }
            
            return taskId;
            
        } catch (Exception e) {
            log.error("解析生成任务响应失败", e);
            throw new RuntimeException("解析生成任务响应失败: " + e.getMessage());
        }
    }

    /**
     * 轮询查询任务结果
     * 
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 最终结果
     */
    private JsonNode pollForResult(String taskId, Long userId) {
        String queryUrl = getVideoApiUrl(userId) + "/async-result/" + taskId;
        log.info("开始轮询查询任务结果, taskId: {}, maxRetries: {}, interval: {}ms", 
                 taskId, MAX_RETRIES, POLL_INTERVAL);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getVideoApiKey(userId));
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        for (int retryCount = 0; retryCount < MAX_RETRIES; retryCount++) {
            try {
                Thread.sleep(POLL_INTERVAL);
                
                ResponseEntity<String> response = restTemplate.exchange(
                    queryUrl,
                    HttpMethod.GET,
                    request,
                    String.class
                );
                String responseBody = response.getBody();
                log.debug("轮询查询响应 ({}次): {}", retryCount + 1, responseBody);
                
                if (responseBody == null || responseBody.isEmpty()) {
                    log.warn("轮询查询响应为空，继续等待...");
                    continue;
                }
                
                JsonNode root = objectMapper.readTree(responseBody);
                
                if (root.has("error")) {
                    String errorMsg = root.get("error").has("message") 
                        ? root.get("error").get("message").asText() 
                        : "未知错误";
                    throw new RuntimeException("查询任务失败: " + errorMsg);
                }
                
                JsonNode targetNode;
                if (root.has("id") && root.has("task_status")) {
                    targetNode = root;
                } else if (root.has("data")) {
                    JsonNode dataNode = root.get("data");
                    if (dataNode.isArray() && dataNode.size() > 0) {
                        targetNode = dataNode.get(0);
                    } else {
                        targetNode = dataNode;
                    }
                } else {
                    log.warn("响应中无法识别数据格式，继续等待...");
                    continue;
                }
                
                String taskStatus = targetNode.has("task_status") 
                    ? targetNode.get("task_status").asText() 
                    : null;
                
                if (taskStatus == null) {
                    log.warn("无法获取任务状态，继续等待...");
                    continue;
                }
                
                log.info("任务状态: {}, 已查询 {} 次", taskStatus, retryCount + 1);
                
                if ("SUCCESS".equals(taskStatus)) {
                    log.info("视频生成成功！");
                    return root;
                } else if ("FAIL".equals(taskStatus)) {
                    throw new RuntimeException("视频生成失败: 任务状态为 FAIL");
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("轮询查询被中断");
            } catch (Exception e) {
                log.warn("轮询查询异常 ({}次): {}", retryCount + 1, e.getMessage());
            }
        }
        
        throw new RuntimeException("视频生成超时: 已超过最大轮询次数 " + MAX_RETRIES);
    }

    /**
     * 解析视频生成结果
     * 
     * @param resultNode API响应节点
     * @param generateType 生成类型
     * @param userId 用户ID
     * @return 视频生成结果对象
     */
    private ImageGenerateResult parseVideoResult(JsonNode resultNode, String generateType, Long userId) {
        JsonNode targetNode;
        if (resultNode.has("id") && (resultNode.has("url") || resultNode.has("video_result"))) {
            targetNode = resultNode;
        } else if (resultNode.has("data")) {
            JsonNode dataNode = resultNode.get("data");
            if (dataNode.isArray() && dataNode.size() > 0) {
                targetNode = dataNode.get(0);
            } else {
                targetNode = dataNode;
            }
        } else {
            targetNode = resultNode;
        }
        
        String videoUrl = null;
        String coverUrl = null;
        
        if (targetNode.has("video_result") && targetNode.get("video_result").isArray()) {
            JsonNode videoResultArray = targetNode.get("video_result");
            if (videoResultArray.size() > 0) {
                JsonNode videoResult = videoResultArray.get(0);
                videoUrl = videoResult.has("url") ? videoResult.get("url").asText() : null;
                coverUrl = videoResult.has("cover_image_url") ? videoResult.get("cover_image_url").asText() : null;
            }
        }
        
        if (videoUrl == null) {
            videoUrl = targetNode.has("url") ? targetNode.get("url").asText() : null;
        }
        
        if (coverUrl == null) {
            coverUrl = targetNode.has("cover_url") ? targetNode.get("cover_url").asText() : null;
        }
        
        if (coverUrl == null || coverUrl.isEmpty()) {
            coverUrl = videoUrl;
        }
        
        log.info("视频生成成功, videoUrl: {}, coverUrl: {}", videoUrl, coverUrl);
        return ImageGenerateResult.successVideo(videoUrl, coverUrl, getVideoModelName(userId), generateType);
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
        return getVideoModelName(userId);
    }
}
