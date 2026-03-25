package com.example.backend.config;

import com.example.backend.interceptor.PermissionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private PermissionInterceptor permissionInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/user/login",
                    "/api/user/register",
                    "/api/video/list",
                    "/api/video/categories",
                    "/api/video/detail/**",
                    "/api/banner/list",
                    "/api/image/**",
                    "/uploads/**",
                    "/backend/**"
                );
    }
}
