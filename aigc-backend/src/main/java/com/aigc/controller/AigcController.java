package com.aigc.controller;

import com.aigc.common.Result;
import com.aigc.config.ModelConfig;
import com.aigc.dto.HistorySaveRequest;
import com.aigc.dto.ImageGenerateRequest;
import com.aigc.dto.ImageGenerateResult;
import com.aigc.entity.AiGenerateHistory;
import com.aigc.manager.TaskManager;
import com.aigc.service.AiGenerateHistoryService;
import com.aigc.service.SystemConfigService;
import com.aigc.util.ImageUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AigcController {

    private final AiGenerateHistoryService aiGenerateHistoryService;
    private final TaskManager taskManager;
    private final SystemConfigService systemConfigService;
    private final ImageUtils imageUtils;

    @Value("${aigc.image.save-path:./upload/images}")
    private String imageSavePath;

    @Value("${aigc.upload.path:./upload/images}")
    private String uploadPath;

    /**
     * 从数据库动态获取后端公网地址
     */
    private String getServerPublicUrl(Long userId) {
        String value;
        if (userId != null) {
            value = systemConfigService.getValueByKeyAndUserId("backend.tunnel.url", userId);
        } else {
            value = systemConfigService.getValueByKey("backend.tunnel.url");
        }
        return value != null && !value.isEmpty() ? value : "http://localhost:8080";
    }

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    @PostMapping("/generate")
    public Result<ImageGenerateResult> generateImage(@Valid @RequestBody ImageGenerateRequest request, @RequestAttribute("userId") Long userId) {
        log.info("收到图片生成请求: prompt={}, userId={}", request.getPrompt(), userId);
        ImageGenerateResult result = aiGenerateHistoryService.generateImage(request, userId);
        if (result.getSuccess()) {
            return Result.success("图片生成成功", result);
        }
        return Result.fail(result.getMessage());
    }

    @PostMapping("/save")
    public Result<Map<String, Object>> saveHistory(@RequestBody HistorySaveRequest request, @RequestAttribute("userId") Long userId) {
        log.info("收到保存历史记录请求: prompt={}, type={}, userId={}", request.getPrompt(), request.getGenerateType(), userId);
        log.info("保存请求数据: localPath={}, imageUrl={}, videoUrl={}", 
                request.getLocalPath(), request.getImageUrl(), request.getVideoUrl());
        
        String generateType = request.getGenerateType() != null ? request.getGenerateType() : ModelConfig.GENERATE_TYPE_IMG;
        
        if (ModelConfig.GENERATE_TYPE_IMG.equals(generateType)) {
            if (request.getImageUrl() == null || request.getImageUrl().isBlank()) {
                return Result.fail("图片URL不能为空");
            }
        } else {
            if (request.getVideoUrl() == null || request.getVideoUrl().isBlank()) {
                return Result.fail("视频URL不能为空");
            }
        }
        
        if (request.getPrompt() == null || request.getPrompt().isBlank()) {
            return Result.fail("提示词不能为空");
        }
        
        Long id = aiGenerateHistoryService.saveHistory(request, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success("保存成功", data);
    }

    @GetMapping("/list")
    public Result<List<AiGenerateHistory>> listHistory(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) String generateType,
            @RequestAttribute("userId") Long userId) {
        log.info("收到查询历史记录请求: limit={}, type={}, userId={}", limit, generateType, userId);
        List<AiGenerateHistory> historyList = aiGenerateHistoryService.listRecentByUserId(limit, generateType, userId);
        return Result.success(historyList);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteHistory(@PathVariable Long id) {
        log.info("收到删除历史记录请求: id={}", id);
        boolean success = aiGenerateHistoryService.deleteById(id);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.fail("删除失败");
    }

    @GetMapping("/detail/{id}")
    public Result<AiGenerateHistory> getDetail(@PathVariable Long id) {
        log.info("收到查询详情请求: id={}", id);
        AiGenerateHistory history = aiGenerateHistoryService.getById(id);
        if (history != null) {
            return Result.success(history);
        }
        return Result.fail("记录不存在");
    }

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "aigc-backend");
        data.put("version", "1.0.0");
        return Result.success(data);
    }

    @PostMapping("/upload")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file, @RequestAttribute("userId") Long userId) {
        log.info("收到图片上传请求: filename={}, size={}, userId={}", file.getOriginalFilename(), file.getSize(), userId);

        if (file.isEmpty()) {
            return Result.fail("请选择要上传的文件");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.fail("文件大小不能超过 20MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            return Result.fail("仅支持 JPG、PNG、WEBP 格式的图片");
        }

        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            File saveDir = new File(uploadPath, datePath);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

            File destFile = new File(saveDir, newFilename);
            file.transferTo(destFile);

            String accessUrl = "/upload/images/" + datePath + "/" + newFilename;
            String fullUrl = getServerPublicUrl(userId) + "/api" + accessUrl;
            Map<String, String> data = new HashMap<>();
            data.put("imgUrl", fullUrl);
            data.put("filename", newFilename);
            data.put("localPath", destFile.getAbsolutePath());

            log.info("图片上传成功: url={}, savePath={}", fullUrl, destFile.getAbsolutePath());
            return Result.success("图片上传成功", data);

        } catch (IOException e) {
            log.error("图片上传失败: {}", e.getMessage(), e);
            return Result.fail("图片上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/models")
    public Result<Map<String, Object>> getModels(
            @RequestParam(required = false) String generateType) {
        Map<String, Object> data = new HashMap<>();
        
        if (generateType != null && !generateType.isEmpty()) {
            data.put("generateType", generateType);
            data.put("models", ModelConfig.getModelsByType(generateType));
            data.put("defaultModel", ModelConfig.getDefaultModel(generateType));
        } else {
            data.put("generateTypes", ModelConfig.getAllGenerateTypes());
            Map<String, List<String>> modelMap = new HashMap<>();
            for (String type : ModelConfig.getAllGenerateTypes()) {
                modelMap.put(type, ModelConfig.getModelsByType(type));
            }
            data.put("modelMap", modelMap);
        }
        
        return Result.success(data);
    }

    @PostMapping("/generate-async")
    public Result<Map<String, String>> generateImageAsync(@Valid @RequestBody ImageGenerateRequest request, @RequestAttribute("userId") Long userId) {
        log.info("收到异步生成请求: prompt={}, type={}, userId={}", request.getPrompt(), request.getGenerateType(), userId);
        
        String taskId = UUID.randomUUID().toString().replace("-", "");
        
        taskManager.submitTask(taskId, () -> {
            return aiGenerateHistoryService.generateImage(request, userId);
        });
        
        Map<String, String> data = new HashMap<>();
        data.put("taskId", taskId);
        
        log.info("异步任务已创建: taskId={}", taskId);
        return Result.success("任务已创建", data);
    }

    @GetMapping("/task-status/{taskId}")
    public Result<Map<String, Object>> getTaskStatus(@PathVariable String taskId) {
        log.info("查询任务状态: taskId={}", taskId);
        
        TaskManager.TaskInfo taskInfo = taskManager.getTaskInfo(taskId);
        
        if (taskInfo == null) {
            return Result.fail("任务不存在");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", taskInfo.getTaskId());
        data.put("status", taskInfo.getStatus().name());
        data.put("result", taskInfo.getResult());
        data.put("errorMessage", taskInfo.getErrorMessage());
        
        return Result.success(data);
    }

    /**
     * 下载历史记录中的图片或视频到本地
     * 
     * <p>将远程URL的文件下载并保存到本地固定位置，更新数据库中的local_path字段。</p>
     * 
     * @param id 历史记录ID
     * @param userId 用户ID
     * @return 下载结果，包含本地保存路径
     */
    @PostMapping("/download/{id}")
    public Result<Map<String, String>> downloadHistoryFile(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        log.info("收到下载历史记录文件请求: id={}, userId={}", id, userId);
        
        try {
            // 查询历史记录
            AiGenerateHistory history = aiGenerateHistoryService.getById(id);
            if (history == null) {
                return Result.fail("记录不存在");
            }
            
            // 如果已有本地路径且文件存在，直接返回
            String existingLocalPath = history.getLocalPath();
            log.info("检查本地路径: localPath={}, isEmpty={}", existingLocalPath, existingLocalPath == null || existingLocalPath.isEmpty());
            
            if (existingLocalPath != null && !existingLocalPath.isEmpty()) {
                File localFile = new File(existingLocalPath);
                log.info("检查文件存在性: path={}, exists={}, absolutePath={}", existingLocalPath, localFile.exists(), localFile.getAbsolutePath());
                if (localFile.exists()) {
                    log.info("文件已存在本地，无需重复下载: {}", existingLocalPath);
                    Map<String, String> data = new HashMap<>();
                    data.put("localPath", existingLocalPath);
                    data.put("message", "文件已存在本地");
                    return Result.success("下载成功", data);
                } else {
                    log.info("文件不存在，需要重新下载");
                }
            } else {
                log.info("localPath为空，需要下载");
            }
            
            // 确定文件类型和URL
            String fileUrl;
            String fileType;
            if (ModelConfig.GENERATE_TYPE_IMG.equals(history.getGenerateType())) {
                fileUrl = history.getImageUrl();
                fileType = "img";
            } else {
                fileUrl = history.getVideoUrl();
                fileType = "video";
            }
            
            if (fileUrl == null || fileUrl.isEmpty()) {
                return Result.fail("文件URL为空，无法下载");
            }
            
            // 下载并保存文件
            String localPath = imageUtils.downloadAndSaveFile(
                    fileUrl, 
                    null, // 使用默认保存路径
                    fileType, 
                    history.getPrompt()
            );
            
            // 更新数据库中的local_path字段
            history.setLocalPath(localPath);
            aiGenerateHistoryService.updateById(history);
            
            log.info("文件下载并保存成功: id={}, localPath={}", id, localPath);
            
            Map<String, String> data = new HashMap<>();
            data.put("localPath", localPath);
            data.put("message", "文件下载并保存成功");
            return Result.success("下载成功", data);
            
        } catch (IOException e) {
            log.error("文件下载失败: id={}, error={}", id, e.getMessage(), e);
            return Result.fail("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 直接下载生成结果到本地
     * 
     * <p>用于在生成结果页面直接下载文件到本地，不需要先保存到历史记录。</p>
     * 
     * @param request 下载请求，包含文件URL、类型和提示词
     * @param userId 用户ID
     * @return 下载结果，包含本地保存路径
     */
    @PostMapping("/download-direct")
    public Result<Map<String, String>> downloadDirect(
            @RequestBody Map<String, String> request,
            @RequestAttribute("userId") Long userId) {
        
        String fileUrl = request.get("fileUrl");
        String fileType = request.get("fileType");
        String prompt = request.get("prompt");
        
        log.info("收到直接下载请求: fileUrl={}, fileType={}, userId={}", fileUrl, fileType, userId);
        
        if (fileUrl == null || fileUrl.isEmpty()) {
            return Result.fail("文件URL不能为空");
        }
        
        if (fileType == null || fileType.isEmpty()) {
            fileType = "img";
        }
        
        try {
            // 下载并保存文件
            String localPath = imageUtils.downloadAndSaveFile(
                    fileUrl,
                    null,
                    fileType,
                    prompt
            );
            
            log.info("文件直接下载成功: localPath={}", localPath);
            
            Map<String, String> data = new HashMap<>();
            data.put("localPath", localPath);
            data.put("message", "文件下载并保存成功");
            return Result.success("下载成功", data);
            
        } catch (IOException e) {
            log.error("文件下载失败: error={}", e.getMessage(), e);
            return Result.fail("文件下载失败: " + e.getMessage());
        }
    }
}
