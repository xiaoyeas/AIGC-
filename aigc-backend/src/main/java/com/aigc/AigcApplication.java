package com.aigc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AIGC后端服务启动类
 * 
 * <p>Spring Boot应用程序的入口点，负责启动整个后端服务。
 * 支持文生图、文生视频、图生视频等多种AI生成功能。</p>
 * 
 * <p>主要功能：
 * <ul>
 *   <li>文生图：使用 CogView-3-Flash 模型生成图片</li>
 *   <li>文生视频：使用 CogVideoX-Flash 模型从文本生成视频</li>
 *   <li>图生视频：使用 CogVideoX-Flash 模型从图片生成视频</li>
 *   <li>历史记录管理：保存和查询生成历史</li>
 * </ul>
 * </p>
 * 
 * @author AIGC Platform
 * @version 2.0.0
 */
@SpringBootApplication
@MapperScan("com.aigc.mapper")
public class AigcApplication {
    /**
     * 应用程序主方法
     * 
     * <p>Spring Boot启动入口，通过SpringApplication.run()启动应用。
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AigcApplication.class, args);
    }
}
