package com.example.backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/bilibili")
public class BilibiliVideoController {

    @Value("${ml.service.url:http://localhost:5001}")
    private String mlServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/videos")
    public Map<String, Object> getVideos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "") String keyword) {
        Map<String, Object> response = new HashMap<>();
        try {
            String url = mlServiceUrl + "/api/bilibili/videos?page=" + page + 
                        "&pageSize=" + pageSize + "&keyword=" + keyword;
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.getForObject(url, Map.class);
            
            if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                response.put("success", true);
                response.put("data", result.get("data"));
                response.put("total", result.get("total"));
                response.put("page", result.get("page"));
                response.put("pageSize", result.get("pageSize"));
            } else {
                response.put("success", false);
                response.put("message", result != null ? result.get("error") : "获取失败");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取B站视频列表失败: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/check-exists")
    public Map<String, Object> checkExists(@RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            
            org.springframework.http.HttpEntity<Map<String, Object>> request = 
                new org.springframework.http.HttpEntity<>(params, headers);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.postForObject(
                mlServiceUrl + "/api/import/check-exists", 
                request, 
                Map.class
            );
            
            if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                response.put("success", true);
                response.put("data", result.get("data"));
            } else {
                response.put("success", false);
                response.put("message", result != null ? result.get("error") : "检查失败");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "检查失败: " + e.getMessage());
        }
        return response;
    }
}
