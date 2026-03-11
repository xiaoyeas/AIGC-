package com.aigc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * 
 * <p>配置Redis连接和序列化策略。
 * 使用StringRedisTemplate进行字符串类型的键值对操作。</p>
 * 
 * <p>配置内容：</p>
 * <ul>
 *   <li>设置Redis连接工厂</li>
 *   <li>配置键和值的序列化器为StringRedisSerializer</li>
 *   <li>配置Hash结构的键和值序列化器</li>
 * </ul>
 * 
 * <p>设计思路：</p>
 * <ul>
 *   <li>使用字符串序列化器，便于在Redis客户端中直接查看数据</li>
 *   <li>避免使用默认的JDK序列化器，提高可读性</li>
 * </ul>
 * 
 * @author AIGC Platform
 * @version 1.0.0
 */
@Configuration
public class RedisConfig {

    /**
     * 配置StringRedisTemplate Bean
     * 
     * <p>创建并配置StringRedisTemplate实例，设置统一的字符串序列化器。
     * 确保Redis中存储的数据可读性好，便于调试和维护。</p>
     * 
     * <p>序列化配置：</p>
     * <ul>
     *   <li>Key序列化器：StringRedisSerializer</li>
     *   <li>Value序列化器：StringRedisSerializer</li>
     *   <li>HashKey序列化器：StringRedisSerializer</li>
     *   <li>HashValue序列化器：StringRedisSerializer</li>
     * </ul>
     * 
     * @param connectionFactory Redis连接工厂，由Spring Boot自动配置
     * @return 配置好的StringRedisTemplate实例
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
