package com.aigc.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI模型配置管理类
 * 
 * <p>该类用于维护按生成类型分类的模型集合，实现多模型统一管理。
 * 后续新增或切换模型仅需修改此配置类，无需改动业务代码。</p>
 * 
 * <p>支持的生成类型：
 * <ul>
 *   <li>img: 文生图</li>
 *   <li>text2video: 文生视频</li>
 *   <li>img2video: 图生视频</li>
 * </ul>
 * </p>
 * 
 * @author AIGC Platform
 * @version 2.0.0
 */
public class ModelConfig {

    /**
     * 文生图类型常量
     */
    public static final String GENERATE_TYPE_IMG = "img";

    /**
     * 文生视频类型常量
     */
    public static final String GENERATE_TYPE_TEXT2VIDEO = "text2video";

    /**
     * 图生视频类型常量
     */
    public static final String GENERATE_TYPE_IMG2VIDEO = "img2video";

    /**
     * 文生图模型列表
     */
    public static final List<String> IMG_MODELS = Arrays.asList(
            "cogview-3-flash"
    );

    /**
     * 文生视频模型列表
     */
    public static final List<String> TEXT2VIDEO_MODELS = Arrays.asList(
            "cogvideox-3"
    );

    /**
     * 图生视频模型列表
     */
    public static final List<String> IMG2VIDEO_MODELS = Arrays.asList(
            "cogvideox-3"
    );

    /**
     * 所有模型的映射关系
     * key: 生成类型
     * value: 该类型支持的模型列表
     */
    private static final Map<String, List<String>> MODEL_MAP = new HashMap<>();

    static {
        MODEL_MAP.put(GENERATE_TYPE_IMG, IMG_MODELS);
        MODEL_MAP.put(GENERATE_TYPE_TEXT2VIDEO, TEXT2VIDEO_MODELS);
        MODEL_MAP.put(GENERATE_TYPE_IMG2VIDEO, IMG2VIDEO_MODELS);
    }

    /**
     * 获取指定生成类型的模型列表
     * 
     * @param generateType 生成类型
     * @return 模型列表，如果类型不存在返回空列表
     */
    public static List<String> getModelsByType(String generateType) {
        return MODEL_MAP.getOrDefault(generateType, List.of());
    }

    /**
     * 检查模型是否支持指定的生成类型
     * 
     * @param generateType 生成类型
     * @param modelName 模型名称
     * @return true-支持，false-不支持
     */
    public static boolean isModelSupported(String generateType, String modelName) {
        List<String> models = getModelsByType(generateType);
        return models.contains(modelName);
    }

    /**
     * 获取指定生成类型的默认模型
     * 
     * @param generateType 生成类型
     * @return 默认模型名称，如果类型不存在返回null
     */
    public static String getDefaultModel(String generateType) {
        List<String> models = getModelsByType(generateType);
        return models.isEmpty() ? null : models.get(0);
    }

    /**
     * 获取所有支持的生成类型
     * 
     * @return 生成类型列表
     */
    public static List<String> getAllGenerateTypes() {
        return Arrays.asList(GENERATE_TYPE_IMG, GENERATE_TYPE_TEXT2VIDEO, GENERATE_TYPE_IMG2VIDEO);
    }

    private ModelConfig() {
    }
}
