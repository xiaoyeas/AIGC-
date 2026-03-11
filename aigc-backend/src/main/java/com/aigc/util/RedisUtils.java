package com.aigc.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis缓存工具类
 * 
 * <p>提供Redis缓存的统一操作接口，封装常用的缓存读写、删除等功能。
 * 主要用于缓存图片URL、历史记录等数据，提升系统响应速度。</p>
 * 
 * <p>设计思路：</p>
 * <ul>
 *   <li>使用统一的缓存键前缀，便于管理和区分不同类型的数据</li>
 *   <li>设置合理的过期时间，避免缓存数据长期占用内存</li>
 *   <li>提供分布式锁支持，防止缓存击穿</li>
 *   <li>封装常用操作，简化业务代码</li>
 * </ul>
 * 
 * <p>缓存键命名规范：</p>
 * <ul>
 *   <li>图片URL缓存：aigc:image:{promptHash}</li>
 *   <li>历史记录缓存：aigc:history:{id}</li>
 * </ul>
 * 
 * @author AIGC Platform
 * @version 1.0.0
 */
@Slf4j
@Component
public class RedisUtils {

    /**
     * Redis字符串操作模板
     * 
     * <p>Spring Data Redis 提供的字符串操作工具，
     * 支持字符串类型的键值对操作。</p>
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 图片URL缓存键前缀
     * 
     * <p>用于缓存提示词与生成图片URL的映射关系，
     * 避免相同提示词重复调用AI模型。</p>
     */
    private static final String IMAGE_CACHE_PREFIX = "aigc:image:";

    /**
     * 历史记录缓存键前缀
     * 
     * <p>用于缓存历史记录的JSON数据，
     * 减少数据库查询压力。</p>
     */
    private static final String HISTORY_CACHE_PREFIX = "aigc:history:";

    /**
     * 默认缓存过期时间（秒）
     * 
     * <p>图片URL缓存默认1小时过期，
     * 历史记录缓存默认24小时过期。</p>
     */
    private static final long DEFAULT_EXPIRE_TIME = 3600;

    /**
     * 构造函数注入Redis模板
     * 
     * @param stringRedisTemplate Spring提供的Redis字符串操作模板
     */
    public RedisUtils(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 缓存图片URL
     * 
     * <p>将提示词与生成的图片URL建立映射关系并缓存。
     * 使用提示词的hashCode作为缓存键的一部分。</p>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>用户使用相同提示词再次生成时，可直接返回缓存的URL</li>
     *   <li>减少AI模型调用次数，节省API费用</li>
     * </ul>
     * 
     * @param prompt   提示词内容
     * @param imageUrl 生成的图片URL
     */
    public void cacheImageUrl(String prompt, String imageUrl) {
        String key = IMAGE_CACHE_PREFIX + prompt.hashCode();
        stringRedisTemplate.opsForValue().set(key, imageUrl, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
        log.debug("缓存图片URL: {} -> {}", key, imageUrl);
    }

    /**
     * 获取缓存的图片URL
     * 
     * <p>根据提示词查询缓存的图片URL。
     * 如果缓存不存在或已过期，返回null。</p>
     * 
     * @param prompt 提示词内容
     * @return 缓存的图片URL，不存在返回null
     */
    public String getCachedImageUrl(String prompt) {
        String key = IMAGE_CACHE_PREFIX + prompt.hashCode();
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 缓存历史记录
     * 
     * <p>将历史记录的JSON数据缓存到Redis。
     * 过期时间设置为24小时，减少数据库查询。</p>
     * 
     * @param id          历史记录ID
     * @param historyJson 历史记录的JSON字符串
     */
    public void cacheHistory(String id, String historyJson) {
        String key = HISTORY_CACHE_PREFIX + id;
        stringRedisTemplate.opsForValue().set(key, historyJson, DEFAULT_EXPIRE_TIME * 24, TimeUnit.SECONDS);
        log.debug("缓存历史记录: {}", key);
    }

    /**
     * 获取缓存的历史记录
     * 
     * <p>根据ID查询缓存的历史记录JSON数据。</p>
     * 
     * @param id 历史记录ID
     * @return 历史记录的JSON字符串，不存在返回null
     */
    public String getCachedHistory(String id) {
        String key = HISTORY_CACHE_PREFIX + id;
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 删除指定缓存
     * 
     * <p>从Redis中删除指定的缓存键。</p>
     * 
     * @param key 要删除的缓存键
     */
    public void deleteCache(String key) {
        stringRedisTemplate.delete(key);
        log.debug("删除缓存: {}", key);
    }

    /**
     * 检查缓存键是否存在
     * 
     * <p>判断指定的缓存键是否存在于Redis中。</p>
     * 
     * @param key 缓存键
     * @return 存在返回true，否则返回false
     */
    public boolean hasKey(String key) {
        Boolean hasKey = stringRedisTemplate.hasKey(key);
        return hasKey != null && hasKey;
    }

    /**
     * 设置缓存（仅当键不存在时）
     * 
     * <p>实现分布式锁的核心方法。只有当键不存在时才设置值，
     * 确保只有一个客户端能够成功设置。</p>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>防止缓存击穿：多个请求同时查询不存在的数据</li>
     *   <li>分布式锁：确保同一时间只有一个实例执行特定操作</li>
     * </ul>
     * 
     * @param key     缓存键
     * @param value   缓存值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 设置成功返回true，键已存在返回false
     */
    public boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
        return result != null && result;
    }

    /**
     * 设置缓存值
     * 
     * <p>向Redis中设置键值对，并指定过期时间。</p>
     * 
     * @param key     缓存键
     * @param value   缓存值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存值
     * 
     * <p>从Redis中获取指定键的值。</p>
     * 
     * @param key 缓存键
     * @return 缓存值，不存在返回null
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }
}
