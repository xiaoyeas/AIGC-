package com.aigc.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 
 * <p>配置Spring MVC相关设置，包括跨域资源共享(CORS)和静态资源映射。</p>
 * 
 * <p>配置内容：</p>
 * <ul>
 *   <li>CORS跨域配置：允许前端应用跨域访问后端API</li>
 *   <li>静态资源映射：将图片存储目录映射为可访问的URL路径</li>
 * </ul>
 * 
 * <p>设计思路：</p>
 * <ul>
 *   <li>开发环境允许所有来源的跨域请求</li>
 *   <li>生产环境应配置具体的允许域名</li>
 *   <li>图片通过文件系统访问，避免占用数据库存储空间</li>
 * </ul>
 * 
 * @author AIGC Platform
 * @version 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    /**
     * 图片保存路径
     * 
     * <p>从配置文件读取，用于配置静态资源映射。
     * 默认值为用户主目录下的 aigc-images 文件夹。</p>
     */
    @Value("${aigc.image.save-path:${user.home}/aigc-images}")
    private String imageSavePath;

    /**
     * 配置跨域资源共享(CORS)
     * 
     * <p>允许前端应用从不同域名、端口访问后端API。
     * 这是前后端分离架构的必要配置。</p>
     * 
     * <p>配置说明：</p>
     * <ul>
     *   <li>路径模式：所有路径 /**</li>
     *   <li>允许来源：所有域名（开发环境）</li>
     *   <li>允许方法：GET、POST、PUT、DELETE、OPTIONS</li>
     *   <li>允许请求头：所有</li>
     *   <li>允许携带凭证：是</li>
     *   <li>预检请求缓存时间：3600秒</li>
     * </ul>
     * 
     * <p>安全提示：</p>
     * <p>生产环境建议将 allowedOriginPatterns 改为具体的前端域名，
     * 如 "https://example.com"，以提高安全性。</p>
     * 
     * @param registry CORS配置注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 上传图片保存路径
     */
    @Value("${aigc.upload.path:./upload/images}")
    private String uploadPath;

    /**
     * 配置静态资源映射
     * 
     * <p>将图片存储目录映射为可访问的URL路径。
     * 前端可以通过 /images/** 路径访问本地存储的图片。</p>
     * 
     * <p>映射规则：
     * <ul>
     *   <li>URL路径：/images/**</li>
     *   <li>文件位置：file:{imageSavePath}/</li>
     *   <li>URL路径：/upload/images/**</li>
     *   <li>文件位置：file:{uploadPath}/</li>
     * </ul>
     * 
     * <p>访问示例：
     * <pre>
     * 本地文件路径：/home/user/aigc-images/20240101/abc.png
     * 访问URL：http://localhost:8080/images/20240101/abc.png
     * 
     * 上传文件路径：./upload/images/20240101/xyz.png
     * 访问URL：http://localhost:8080/upload/images/20240101/xyz.png
     * </pre>
     * 
     * @param registry 资源处理注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + imageSavePath + "/");
        
        registry.addResourceHandler("/upload/images/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    /**
     * 配置JWT认证拦截器
     * 
     * <p>拦截所有非认证相关的API请求，验证JWT token的有效性。</p>
     * 
     * <p>拦截规则：</p>
     * <ul>
     *   <li>放行路径：/auth/**（认证接口）</li>
     *   <li>放行路径：/static/**（静态资源）</li>
     *   <li>放行路径：/images/**（图片资源）</li>
     *   <li>放行路径：/upload/images/**（上传图片资源）</li>
     *   <li>放行路径：/health（健康检查接口）</li>
     *   <li>拦截路径：/**（其他所有API）</li>
     * </ul>
     * 
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/**")
                .excludePathPatterns("/static/**")
                .excludePathPatterns("/images/**")
                .excludePathPatterns("/upload/images/**")
                .excludePathPatterns("/health");
    }
}
