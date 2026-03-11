package com.aigc.service.impl;

import com.aigc.config.ModelConfig;
import com.aigc.dto.HistorySaveRequest;
import com.aigc.dto.ImageGenerateRequest;
import com.aigc.dto.ImageGenerateResult;
import com.aigc.entity.AiGenerateHistory;
import com.aigc.mapper.AiGenerateHistoryMapper;
import com.aigc.service.AiGenerateHistoryService;
import com.aigc.skill.CogView3FlashSkill;
import com.aigc.skill.CogVideoX3Skill;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AI图片生成历史记录服务实现类
 * 
 * <p>实现了图片生成、历史记录管理等核心业务逻辑。
 * 根据生成类型自动分发到对应的AI模型进行处理。</p>
 * 
 * <p>业务流程：
 * <ul>
 *   <li>generateImage: 根据生成类型调用对应的模型技能</li>
 *   <li>saveHistory: 将生成结果持久化到数据库</li>
 *   <li>listRecent: 查询最近的历史记录</li>
 * </ul>
 * </p>
 * 
 * @author AIGC Platform
 * @version 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiGenerateHistoryServiceImpl extends ServiceImpl<AiGenerateHistoryMapper, AiGenerateHistory> implements AiGenerateHistoryService {

    /**
     * 文生图模型技能
     * 
     * <p>用于处理文生图生成请求，使用 CogView-3-Flash 模型。
     */
    private final CogView3FlashSkill cogView3FlashSkill;

    /**
     * 视频生成模型技能
     * 
     * <p>用于处理文生视频和图生视频生成请求，使用 CogVideoX-3 模型。
     */
    private final CogVideoX3Skill cogVideoX3Skill;

    /**
     * 生成图片或视频
     * 
     * <p>根据请求参数中的 generateType 自动分发到对应的模型：
     * - img: 调用 CogView3FlashSkill 生成图片
     * - text2video/img2video: 调用 CogVideoXFlashSkill 生成视频
     * 
     * @param request 生成请求参数
     * @return 生成结果对象
     */
    @Override
    public ImageGenerateResult generateImage(ImageGenerateRequest request) {
        return generateImage(request, null);
    }
    
    /**
     * 生成图片或视频（带用户ID）
     * 
     * <p>根据请求参数中的 generateType 自动分发到对应的模型：
     * - img: 调用 CogView3FlashSkill 生成图片
     * - text2video/img2video: 调用 CogVideoXFlashSkill 生成视频
     * 
     * @param request 生成请求参数
     * @param userId 用户ID
     * @return 生成结果对象
     */
    @Override
    public ImageGenerateResult generateImage(ImageGenerateRequest request, Long userId) {
        String generateType = StringUtils.defaultIfBlank(request.getGenerateType(), ModelConfig.GENERATE_TYPE_IMG);
        String modelName = StringUtils.defaultIfBlank(request.getModelName(), ModelConfig.getDefaultModel(generateType));
        
        log.info("开始生成, type: {}, prompt: {}, modelName: {}, userId: {}", generateType, request.getPrompt(), modelName, userId);
        
        try {
            if (ModelConfig.GENERATE_TYPE_IMG.equals(generateType)) {
                ImageGenerateResult result = cogView3FlashSkill.generateImage(
                        request.getPrompt(),
                        request.getReferenceImagePath(),
                        request.getSaveDirectory(),
                        request.getImageSize(),
                        userId
                );
                log.info("图片生成完成, success: {}, localPath: {}", result.getSuccess(), result.getLocalPath());
                return result;
            } else {
                ImageGenerateResult result = cogVideoX3Skill.generateVideo(
                        request.getPrompt(),
                        request.getImgUrl(),
                        generateType,
                        userId
                );
                log.info("视频生成完成, success: {}", result.getSuccess());
                
                // 如果是图生视频且生成成功，延迟删除临时上传的图片
                if (result.getSuccess() && "img2video".equals(generateType) && StringUtils.isNotBlank(request.getImgLocalPath())) {
                    scheduleTempFileDeletion(request.getImgLocalPath());
                }
                
                return result;
            }
        } catch (Exception e) {
            log.error("生成失败: {}", e.getMessage(), e);
            return ImageGenerateResult.fail("生成失败: " + e.getMessage());
        }
    }

    /**
     * 延迟删除临时上传的图片文件
     * 
     * <p>在视频生成完成后，延迟5秒删除临时上传的参考图片，
     * 确保AI模型有足够时间处理完成。</p>
     * 
     * @param filePath 要删除的文件路径
     */
    private void scheduleTempFileDeletion(String filePath) {
        CompletableFuture.runAsync(() -> {
            try {
                // 延迟5秒后删除
                Thread.sleep(5000);
                
                File file = new File(filePath);
                if (file.exists() && file.delete()) {
                    log.info("临时图片已删除: {}", filePath);
                } else if (!file.exists()) {
                    log.warn("临时图片不存在: {}", filePath);
                } else {
                    log.warn("临时图片删除失败: {}", filePath);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("删除临时图片任务被中断: {}", filePath);
            } catch (Exception e) {
                log.error("删除临时图片异常: {}", filePath, e);
            }
        });
    }

    /**
     * 保存历史记录到数据库
     * 
     * <p>将用户的生成结果持久化，便于后续查看和管理。
     * 自动设置创建时间为当前时间。
     * 
     * @param request 历史记录保存请求
     * @return 保存成功后的记录ID
     */
    @Override
    public Long saveHistory(HistorySaveRequest request) {
        log.info("保存历史记录, prompt: {}, type: {}", request.getPrompt(), request.getGenerateType());
        AiGenerateHistory history = new AiGenerateHistory();
        history.setPrompt(request.getPrompt());
        history.setImageUrl(request.getImageUrl());
        history.setModelName(request.getModelName() != null ? request.getModelName() : "cogview-3-flash");
        history.setLocalPath(request.getLocalPath());
        history.setImageSize(request.getImageSize());
        history.setGenerateType(StringUtils.defaultIfBlank(request.getGenerateType(), ModelConfig.GENERATE_TYPE_IMG));
        history.setVideoUrl(request.getVideoUrl());
        history.setCoverUrl(StringUtils.defaultIfBlank(request.getCoverUrl(), request.getImageUrl()));
        history.setCreateTime(LocalDateTime.now());
        this.save(history);
        log.info("历史记录保存成功, id: {}", history.getId());
        return history.getId();
    }

    /**
     * 查询最近的历史记录（无筛选）
     * 
     * @param limit 查询数量限制
     * @return 历史记录列表
     */
    @Override
    public List<AiGenerateHistory> listRecent(int limit) {
        return listRecent(limit, null);
    }

    /**
     * 查询最近的历史记录（按类型筛选）
     * 
     * <p>按创建时间倒序查询，支持按生成类型筛选。
     * 
     * @param limit 查询数量限制
     * @param generateType 生成类型筛选（可选）
     * @return 历史记录列表
     */
    @Override
    public List<AiGenerateHistory> listRecent(int limit, String generateType) {
        log.info("查询最近 {} 条历史记录, type: {}", limit, generateType);
        LambdaQueryWrapper<AiGenerateHistory> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.isNotBlank(generateType)) {
            queryWrapper.eq(AiGenerateHistory::getGenerateType, generateType);
        }
        
        queryWrapper.orderByDesc(AiGenerateHistory::getId)
                .last("LIMIT " + limit);
        return this.list(queryWrapper);
    }

    /**
     * 根据ID删除历史记录
     * 
     * @param id 历史记录ID
     * @return 删除成功返回true
     */
    @Override
    public boolean deleteById(Long id) {
        log.info("删除历史记录, id: {}", id);
        return this.removeById(id);
    }

    /**
     * 根据ID查询历史记录
     * 
     * @param id 历史记录ID
     * @return 历史记录对象，不存在返回null
     */
    @Override
    public AiGenerateHistory getById(Long id) {
        return super.getById(id);
    }

    /**
     * 根据ID更新历史记录
     * 
     * @param history 历史记录对象
     * @return 更新成功返回true
     */
    @Override
    public boolean updateById(AiGenerateHistory history) {
        log.info("更新历史记录, id: {}", history.getId());
        return super.updateById(history);
    }

    /**
     * 保存用户历史记录到数据库
     * 
     * <p>将用户的生成结果持久化，关联用户ID。
     * 自动设置创建时间为当前时间。
     * 
     * @param request 历史记录保存请求
     * @param userId 用户ID
     * @return 保存成功后的记录ID
     */
    @Override
    public Long saveHistory(HistorySaveRequest request, Long userId) {
        log.info("保存用户历史记录, prompt: {}, type: {}, userId: {}", request.getPrompt(), request.getGenerateType(), userId);
        AiGenerateHistory history = new AiGenerateHistory();
        history.setPrompt(request.getPrompt());
        history.setImageUrl(request.getImageUrl());
        history.setModelName(request.getModelName() != null ? request.getModelName() : "cogview-3-flash");
        history.setLocalPath(request.getLocalPath());
        history.setImageSize(request.getImageSize());
        history.setGenerateType(StringUtils.defaultIfBlank(request.getGenerateType(), ModelConfig.GENERATE_TYPE_IMG));
        history.setVideoUrl(request.getVideoUrl());
        history.setCoverUrl(StringUtils.defaultIfBlank(request.getCoverUrl(), request.getImageUrl()));
        history.setUserId(userId);
        history.setCreateTime(LocalDateTime.now());
        this.save(history);
        log.info("用户历史记录保存成功, id: {}, userId: {}", history.getId(), userId);
        return history.getId();
    }

    /**
     * 查询用户最近的历史记录（按类型筛选）
     * 
     * <p>按创建时间倒序查询指定用户的历史记录，支持按生成类型筛选。
     * 
     * @param limit 查询数量限制
     * @param generateType 生成类型筛选（可选）
     * @param userId 用户ID
     * @return 历史记录列表
     */
    @Override
    public List<AiGenerateHistory> listRecentByUserId(int limit, String generateType, Long userId) {
        log.info("查询用户最近 {} 条历史记录, type: {}, userId: {}", limit, generateType, userId);
        LambdaQueryWrapper<AiGenerateHistory> queryWrapper = new LambdaQueryWrapper<>();
        
        queryWrapper.eq(AiGenerateHistory::getUserId, userId);
        
        if (StringUtils.isNotBlank(generateType)) {
            queryWrapper.eq(AiGenerateHistory::getGenerateType, generateType);
        }
        
        queryWrapper.orderByDesc(AiGenerateHistory::getId)
                .last("LIMIT " + limit);
        return this.list(queryWrapper);
    }
}
