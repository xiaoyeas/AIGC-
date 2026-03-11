package com.aigc.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 * 
 * <p>配置MyBatis-Plus框架的相关插件和功能。
 * MyBatis-Plus是MyBatis的增强工具，简化了CRUD操作。</p>
 * 
 * <p>配置内容：</p>
 * <ul>
 *   <li>分页插件：支持数据库分页查询</li>
 * </ul>
 * 
 * <p>设计思路：</p>
 * <ul>
 *   <li>使用分页插件避免一次性加载大量数据</li>
 *   <li>指定MySQL数据库类型，优化分页SQL</li>
 * </ul>
 * 
 * @author AIGC Platform
 * @version 1.0.0
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 配置MyBatis-Plus拦截器
     * 
     * <p>创建并配置MyBatisPlusInterceptor，添加分页插件支持。
     * 分页插件会自动拦截分页查询，生成对应数据库的分页SQL。</p>
     * 
     * <p>分页插件功能：</p>
     * <ul>
     *   <li>自动计算分页参数（LIMIT、OFFSET）</li>
     *   <li>支持多种数据库类型</li>
     *   <li>返回分页结果对象，包含总记录数等信息</li>
     * </ul>
     * 
     * <p>使用示例：</p>
     * <pre>
     * // 在Service中使用分页
     * Page&lt;AiGenerateHistory&gt; page = new Page&lt;&gt;(1, 10);
     * page = baseMapper.selectPage(page, queryWrapper);
     * </pre>
     * 
     * @return 配置好的MybatisPlusInterceptor实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
