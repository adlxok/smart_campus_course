package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射头像图片
        registry.addResourceHandler("/backend/image/**")
                .addResourceLocations("file:d:/A_Graduation_Project/project/p2_0/backend/image/");
        
        // 映射视频文件
        registry.addResourceHandler("/backend/video/**")
                .addResourceLocations("file:d:/A_Graduation_Project/project/p2_0/backend/video/");
        
        // 映射视频封面图片
        registry.addResourceHandler("/backend/cover/**")
                .addResourceLocations("file:d:/A_Graduation_Project/project/p2_0/backend/cover/");
    }
}
