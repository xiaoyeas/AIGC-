package com.aigc.service;

import com.aigc.dto.HistorySaveRequest;
import com.aigc.dto.ImageGenerateRequest;
import com.aigc.dto.ImageGenerateResult;
import com.aigc.entity.AiGenerateHistory;

import java.util.List;

/**
 * AI图片生成历史记录服务接口
 * 
 * <p>定义了AI图片生成和历史记录管理的核心业务方法。
 * 包括图片生成、历史记录保存、查询和删除等功能。</p>
 * 
 * <p>设计思路：</p>
 * <ul>
 *   <li>接口与实现分离，便于扩展和测试</li>
 *   <li>使用DTO对象封装请求参数，提高代码可维护性</li>
 *   <li>统一返回结果格式，便于前端处理</li>
 * </ul>
 * 
 * @author AIGC Platform
 * @version 1.0.0
 */
public interface AiGenerateHistoryService {

    /**
     * 生成图片
     * 
     * <p>根据请求参数调用AI模型生成图片。
     * 支持指定模型、参考图、保存目录和图片尺寸。</p>
     * 
     * @param request 图片生成请求参数
     * @return ImageGenerateResult 生成结果，包含状态、URL、本地路径等信息
     */
    ImageGenerateResult generateImage(ImageGenerateRequest request);
    
    /**
     * 生成图片（带用户ID）
     * 
     * <p>根据请求参数调用AI模型生成图片。
     * 支持指定模型、参考图、保存目录和图片尺寸。</p>
     * 
     * @param request 图片生成请求参数
     * @param userId 用户ID
     * @return ImageGenerateResult 生成结果，包含状态、URL、本地路径等信息
     */
    ImageGenerateResult generateImage(ImageGenerateRequest request, Long userId);

    /**
     * 保存历史记录
     * 
     * <p>将图片生成结果保存到数据库，便于后续查看和管理。</p>
     * 
     * @param request 历史记录保存请求参数
     * @return 保存成功后返回记录ID
     */
    Long saveHistory(HistorySaveRequest request);
    
    /**
     * 保存用户历史记录
     * 
     * <p>将图片生成结果保存到数据库，关联用户ID。</p>
     * 
     * @param request 历史记录保存请求参数
     * @param userId 用户ID
     * @return 保存成功后返回记录ID
     */
    Long saveHistory(HistorySaveRequest request, Long userId);

    /**
     * 查询最近的历史记录
     * 
     * <p>按创建时间倒序查询指定数量的历史记录。</p>
     * 
     * @param limit 查询数量限制
     * @return 历史记录列表
     */
    List<AiGenerateHistory> listRecent(int limit);

    /**
     * 查询最近的历史记录（按生成类型筛选）
     * 
     * <p>按创建时间倒序查询指定数量的历史记录，支持按生成类型筛选。</p>
     * 
     * @param limit 查询数量限制
     * @param generateType 生成类型筛选（可选）
     * @return 历史记录列表
     */
    List<AiGenerateHistory> listRecent(int limit, String generateType);
    
    /**
     * 查询用户最近的历史记录（按生成类型筛选）
     * 
     * <p>按创建时间倒序查询指定用户的历史记录，支持按生成类型筛选。</p>
     * 
     * @param limit 查询数量限制
     * @param generateType 生成类型筛选（可选）
     * @param userId 用户ID
     * @return 历史记录列表
     */
    List<AiGenerateHistory> listRecentByUserId(int limit, String generateType, Long userId);

    /**
     * 根据ID删除历史记录
     * 
     * <p>删除指定的历史记录，同时可考虑删除关联的本地图片文件。</p>
     * 
     * @param id 历史记录ID
     * @return 删除成功返回true，否则返回false
     */
    boolean deleteById(Long id);

    /**
     * 根据ID查询历史记录
     * 
     * <p>查询单条历史记录的详细信息。</p>
     * 
     * @param id 历史记录ID
     * @return 历史记录对象，不存在返回null
     */
    AiGenerateHistory getById(Long id);

    /**
     * 根据ID更新历史记录
     * 
     * <p>更新历史记录信息，如本地路径等。</p>
     * 
     * @param history 历史记录对象
     * @return 更新成功返回true，否则返回false
     */
    boolean updateById(AiGenerateHistory history);
}
