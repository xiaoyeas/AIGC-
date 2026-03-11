package com.aigc.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 图片处理工具类
 * 
 * <p>提供图片下载、保存、删除等核心功能的工具类。
 * 支持从URL下载图片到本地、从字节数组保存图片、文件管理等功能。</p>
 * 
 * <p>设计思路：</p>
 * <ul>
 *   <li>自动按日期创建子目录，便于图片分类管理</li>
 *   <li>使用UUID生成唯一文件名，避免文件名冲突</li>
 *   <li>提供完善的日志记录，便于问题排查</li>
 *   <li>支持自定义保存目录，灵活配置存储位置</li>
 * </ul>
 * 
 * <p>目录结构示例：</p>
 * <pre>
 * /home/user/aigc-images/           # 根目录
 *   ├── 20240101/                   # 日期子目录
 *   │   ├── abc123.png
 *   │   └── def456.png
 *   └── 20240102/
 *       └── ghi789.png
 * </pre>
 * 
 * @author AIGC Platform
 * @version 1.0.0
 */
@Slf4j
@Component
public class ImageUtils {

    /**
     * 默认图片保存路径
     * 
     * <p>从配置文件读取，如果未配置则使用用户主目录下的 aigc-images 文件夹。
     * 配置项：aigc.image.save-path</p>
     */
    @Value("${aigc.image.save-path:${user.home}/aigc-images}")
    private String defaultSavePath;

    /**
     * 日期格式化器
     * 
     * <p>用于生成日期子目录名称，格式为 yyyyMMdd，如 "20240101"。</p>
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 从URL下载并保存图片到本地
     * 
     * <p>该方法会自动创建目标目录（如果不存在），并将图片保存到按日期分类的子目录中。
     * 文件名使用UUID生成，确保唯一性。</p>
     * 
     * <p>实现流程：</p>
     * <ol>
     *   <li>获取或创建目标目录</li>
     *   <li>生成唯一文件名</li>
     *   <li>打开URL连接获取输入流</li>
     *   <li>将输入流内容写入本地文件</li>
     *   <li>返回完整的文件路径</li>
     * </ol>
     * 
     * @param imageUrl      图片的远程URL地址
     * @param saveDirectory 目标保存目录，为null时使用默认路径
     * @return 保存成功后的完整文件路径
     * @throws IOException 当网络连接失败或文件写入失败时抛出
     */
    public String saveImageFromUrl(String imageUrl, String saveDirectory) throws IOException {
        String targetDir = getOrCreateDirectory(saveDirectory);
        String fileName = generateFileName();
        String filePath = Paths.get(targetDir, fileName).toString();
        
        log.info("开始下载图片: {} -> {}", imageUrl, filePath);
        
        try (InputStream inputStream = new URL(imageUrl).openStream()) {
            Files.copy(inputStream, Paths.get(filePath));
            log.info("图片保存成功: {}", filePath);
            return filePath;
        } catch (IOException e) {
            log.error("图片保存失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 从URL下载并保存文件到本地（支持图片和视频）
     * 
     * <p>该方法会自动创建目标目录（如果不存在），并将文件保存到按日期分类的子目录中。
     * 图片和视频会分别保存到不同的子文件夹中。</p>
     * 
     * <p>目录结构：</p>
     * <pre>
     * /home/user/aigc-images/           # 根目录
     *   ├── images/                     # 图片子目录
     *   │   └── 20240101/               # 日期子目录
     *   │       └── xxx_IMG_xxx.png
     *   └── videos/                     # 视频子目录
     *       └── 20240101/               # 日期子目录
     *           └── xxx_VID_xxx.mp4
     * </pre>
     * 
     * @param fileUrl       文件的远程URL地址
     * @param saveDirectory 目标保存目录，为null时使用默认路径
     * @param fileType      文件类型（img/video）
     * @param prompt        提示词，用于生成文件名
     * @return 保存成功后的完整文件路径
     * @throws IOException 当网络连接失败或文件写入失败时抛出
     */
    public String downloadAndSaveFile(String fileUrl, String saveDirectory, String fileType, String prompt) throws IOException {
        String targetDir = getOrCreateDirectory(saveDirectory, fileType);
        String fileName = generateFileName(fileType, prompt);
        String filePath = Paths.get(targetDir, fileName).toString();
        
        log.info("开始下载文件: {} -> {}", fileUrl, filePath);
        
        // 检查文件是否已存在，如果存在则直接返回
        File existingFile = new File(filePath);
        if (existingFile.exists()) {
            log.info("文件已存在，无需重复下载: {}", filePath);
            return filePath;
        }
        
        try {
            URL url = new URL(fileUrl);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            
            try (InputStream inputStream = connection.getInputStream()) {
                Files.copy(inputStream, Paths.get(filePath));
                log.info("文件保存成功: {}", filePath);
                return filePath;
            }
        } catch (IOException e) {
            log.error("文件保存失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 从字节数组保存图片到本地
     * 
     * <p>适用于AI模型返回Base64编码图片的场景。
     * 将解码后的字节数组直接写入文件系统。</p>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>AI模型返回Base64编码的图片数据</li>
     *   <li>前端上传的图片二进制数据</li>
     *   <li>其他需要保存字节数组的场景</li>
     * </ul>
     * 
     * @param imageBytes    图片的字节数组数据
     * @param saveDirectory 目标保存目录，为null时使用默认路径
     * @return 保存成功后的完整文件路径
     * @throws IOException 当文件写入失败时抛出
     */
    public String saveImageFromBytes(byte[] imageBytes, String saveDirectory) throws IOException {
        String targetDir = getOrCreateDirectory(saveDirectory);
        String fileName = generateFileName();
        String filePath = Paths.get(targetDir, fileName).toString();
        
        log.info("开始保存图片到: {}", filePath);
        
        Files.write(Paths.get(filePath), imageBytes);
        log.info("图片保存成功: {}", filePath);
        return filePath;
    }

    /**
     * 获取或创建图片存储目录
     * 
     * <p>该方法会自动在指定目录下创建以当前日期命名的子目录，
     * 便于按日期分类管理图片文件。</p>
     * 
     * <p>目录命名规则：</p>
     * <ul>
     *   <li>根目录：配置的保存路径或默认路径</li>
     *   <li>子目录：当前日期，格式为 yyyyMMdd</li>
     * </ul>
     * 
     * @param directory 指定的目录路径，为null或空时使用默认路径
     * @return 创建完成后的完整目录路径
     */
    public String getOrCreateDirectory(String directory) {
        return getOrCreateDirectory(directory, null);
    }

    /**
     * 获取或创建存储目录（支持按文件类型分类）
     * 
     * <p>该方法会自动在指定目录下创建以文件类型和日期命名的子目录，
     * 便于按类型和日期分类管理文件。</p>
     * 
     * <p>目录命名规则：</p>
     * <ul>
     *   <li>根目录：配置的保存路径或默认路径</li>
     *   <li>类型子目录：images（图片）或 videos（视频）</li>
     *   <li>日期子目录：当前日期，格式为 yyyyMMdd</li>
     * </ul>
     * 
     * @param directory 指定的目录路径，为null或空时使用默认路径
     * @param fileType  文件类型（img/video），为null时不区分类型
     * @return 创建完成后的完整目录路径
     */
    public String getOrCreateDirectory(String directory, String fileType) {
        String targetDir = (directory != null && !directory.isEmpty()) ? directory : defaultSavePath;
        
        // 根据文件类型添加子目录
        if (fileType != null && !fileType.isEmpty()) {
            String typeFolder = "img".equals(fileType) ? "images" : "videos";
            targetDir = Paths.get(targetDir, typeFolder).toString();
        }
        
        // 添加日期子目录
        String dateFolder = LocalDateTime.now().format(DATE_FORMATTER);
        targetDir = Paths.get(targetDir, dateFolder).toString();
        
        File dir = new File(targetDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                log.info("创建目录: {}", targetDir);
            }
        }
        return targetDir;
    }

    /**
     * 生成唯一的图片文件名
     * 
     * <p>使用UUID生成唯一标识符，并添加.png后缀。
     * 确保同一目录下不会出现文件名冲突。</p>
     * 
     * @return 生成的文件名，格式为 "uuid.png"
     */
    private String generateFileName() {
        return UUID.randomUUID().toString().replace("-", "") + ".png";
    }

    /**
     * 生成包含信息的文件名
     * 
     * <p>根据文件类型和提示词生成有意义的文件名。
     * 文件名包含类型标识和提示词摘要（不包含时间戳，避免重复下载）。</p>
     * 
     * @param fileType 文件类型（img/video）
     * @param prompt   提示词内容
     * @return 生成的文件名
     */
    private String generateFileName(String fileType, String prompt) {
        String typePrefix = "img".equals(fileType) ? "IMG" : "VID";
        String promptSummary = extractPromptSummary(prompt);
        String extension = "img".equals(fileType) ? ".png" : ".mp4";
        
        return typePrefix + "_" + promptSummary + extension;
    }

    /**
     * 提取提示词摘要
     * 
     * <p>从提示词中提取前10个字符作为文件名的一部分，
     * 去除特殊字符，便于文件系统识别。</p>
     * 
     * @param prompt 提示词内容
     * @return 处理后的提示词摘要
     */
    private String extractPromptSummary(String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            return "unknown";
        }
        
        // 取前15个字符，去除特殊字符
        String summary = prompt.length() > 15 ? prompt.substring(0, 15) : prompt;
        summary = summary.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", "_");
        summary = summary.replaceAll("_+", "_"); // 合并多个下划线
        summary = summary.replaceAll("^_+|_+$", ""); // 去除首尾下划线
        
        if (summary.isEmpty()) {
            return "unknown";
        }
        
        return summary;
    }

    /**
     * 删除本地图片文件
     * 
     * <p>安全删除指定路径的图片文件。
     * 如果文件不存在或删除失败，不会抛出异常。</p>
     * 
     * @param filePath 要删除的文件完整路径
     * @return 删除成功返回true，文件不存在或删除失败返回false
     */
    public boolean deleteImage(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("图片删除成功: {}", filePath);
                return true;
            }
        } catch (IOException e) {
            log.error("图片删除失败: {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * 获取默认保存路径
     * 
     * @return 默认保存路径
     */
    public String getDefaultSavePath() {
        return defaultSavePath;
    }
}
