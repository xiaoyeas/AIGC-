package com.aigc.util;

import cn.hutool.core.util.IdUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 通用工具类
 * 
 * <p>提供项目中常用的工具方法，包括ID生成、日期格式化、字符串处理等功能。
 * 所有方法均为静态方法，可直接调用。</p>
 * 
 * <p>设计思路：</p>
 * <ul>
 *   <li>统一ID生成策略，使用UUID确保唯一性</li>
 *   <li>提供统一的日期时间格式化方法</li>
 *   <li>封装常用的字符串处理逻辑</li>
 *   <li>使用Hutool工具库简化实现</li>
 * </ul>
 * 
 * @author AIGC Platform
 * @version 1.0.0
 */
public class CommonUtils {

    /**
     * 默认日期时间格式化器
     * 
     * <p>格式：yyyy-MM-dd HH:mm:ss</p>
     * <p>示例：2024-01-01 12:30:45</p>
     */
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 文件名日期时间格式化器
     * 
     * <p>格式：yyyyMMdd_HHmmss</p>
     * <p>示例：20240101_123045</p>
     * <p>适用于生成带时间戳的文件名</p>
     */
    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * 生成唯一ID
     * 
     * <p>使用Hutool工具库生成UUID，去除横线，返回32位小写字符串。
     * 适用于生成记录ID、文件名等需要唯一标识的场景。</p>
     * 
     * <p>特点：</p>
     * <ul>
     *   <li>全局唯一性</li>
     *   <li>无序性，不暴露业务信息</li>
     *   <li>32位字符串，便于存储和传输</li>
     * </ul>
     * 
     * @return 32位UUID字符串
     */
    public static String generateId() {
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 格式化日期时间
     * 
     * <p>将LocalDateTime格式化为标准字符串格式。
     * 格式：yyyy-MM-dd HH:mm:ss</p>
     * 
     * @param dateTime 要格式化的日期时间对象
     * @return 格式化后的字符串，如果参数为null则返回空字符串
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }

    /**
     * 格式化为文件名时间戳
     * 
     * <p>将LocalDateTime格式化为适用于文件名的时间戳字符串。
     * 格式：yyyyMMdd_HHmmss</p>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>生成带时间戳的导出文件名</li>
     *   <li>日志文件命名</li>
     *   <li>备份文件命名</li>
     * </ul>
     * 
     * @param dateTime 要格式化的日期时间对象，为null时使用当前时间
     * @return 格式化后的时间戳字符串
     */
    public static String formatFileName(LocalDateTime dateTime) {
        if (dateTime == null) {
            dateTime = LocalDateTime.now();
        }
        return dateTime.format(FILE_NAME_FORMATTER);
    }

    /**
     * 截断字符串
     * 
     * <p>将字符串截断到指定长度，超出部分用省略号代替。
     * 适用于在UI中显示长文本的场景。</p>
     * 
     * <p>示例：</p>
     * <ul>
     *   <li>truncate("这是一段很长的文本", 5) -> "这是一段很..."</li>
     *   <li>truncate("短文本", 10) -> "短文本"</li>
     * </ul>
     * 
     * @param str       要截断的字符串
     * @param maxLength 最大长度（不包含省略号）
     * @return 截断后的字符串，如果原字符串为null则返回空字符串
     */
    public static String truncate(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    /**
     * 判断字符串是否为空
     * 
     * <p>检查字符串是否为null或仅包含空白字符。</p>
     * 
     * <p>示例：</p>
     * <ul>
     *   <li>isEmpty(null) -> true</li>
     *   <li>isEmpty("") -> true</li>
     *   <li>isEmpty("  ") -> true</li>
     *   <li>isEmpty("text") -> false</li>
     * </ul>
     * 
     * @param str 要检查的字符串
     * @return 为空返回true，否则返回false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空
     * 
     * <p>isEmpty方法的反向判断，便于在条件语句中使用。</p>
     * 
     * @param str 要检查的字符串
     * @return 不为空返回true，为空返回false
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 返回默认值（如果字符串为空）
     * 
     * <p>如果字符串为空（null或空白），则返回指定的默认值。
     * 常用于配置项读取、参数处理等场景。</p>
     * 
     * <p>示例：</p>
     * <ul>
     *   <li>defaultIfEmpty(null, "default") -> "default"</li>
     *   <li>defaultIfEmpty("", "default") -> "default"</li>
     *   <li>defaultIfEmpty("value", "default") -> "value"</li>
     * </ul>
     * 
     * @param str          原字符串
     * @param defaultValue 默认值
     * @return 原字符串（如果不为空）或默认值
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }
}
