package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/backend/image/**")
                .addResourceLocations("file:d:/A_Graduation_Project/project/p2_0/backend/image/");
        registry.addResourceHandler("/backend/video/**")
                .addResourceLocations("file:d:/A_Graduation_Project/project/p2_0/backend/video/");
        registry.addResourceHandler("/backend/cover/**")
                .addResourceLocations("file:d:/A_Graduation_Project/project/p2_0/backend/cover/");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:d:/A_Graduation_Project/project/p2_0/uploads/");
    }
}
