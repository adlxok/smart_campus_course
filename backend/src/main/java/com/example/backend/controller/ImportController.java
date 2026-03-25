package com.example.backend.controller;

import com.example.backend.annotation.RequirePermission;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/import")
public class ImportController {

    @Value("${ml.service.url:http://localhost:5001}")
    private String mlServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/start")
    @RequirePermission("data:import:manage")
    public Map<String, Object> startImport(@RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.postForObject(
                mlServiceUrl + "/api/import/start", 
                request, 
                Map.class
            );
            
            if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                response.put("success", true);
                response.put("taskId", result.get("taskId"));
                response.put("message", result.get("message"));
            } else {
                response.put("success", false);
                response.put("message", result != null ? result.get("error") : "启动失败");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "启动导入任务失败: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/status/{taskId}")
    public Map<String, Object> getTaskStatus(@PathVariable String taskId) {
        Map<String, Object> response = new HashMap<>();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.getForObject(
                mlServiceUrl + "/api/import/status/" + taskId, 
                Map.class
            );
            
            if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                response.put("success", true);
                response.put("status", data.get("status"));
                response.put("progress", data.get("progress"));
                response.put("totalCount", data.get("totalCount"));
                response.put("successCount", data.get("successCount"));
                response.put("failCount", data.get("failCount"));
                response.put("skipCount", data.get("skipCount"));
                response.put("result", data.get("result"));
            } else {
                response.put("success", false);
                response.put("message", result != null ? result.get("error") : "获取状态失败");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取状态失败: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/logs/{taskId}")
    public Map<String, Object> getTaskLogs(@PathVariable String taskId, @RequestParam(defaultValue = "0") int since) {
        Map<String, Object> response = new HashMap<>();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.getForObject(
                mlServiceUrl + "/api/import/logs/" + taskId + "?since=" + since, 
                Map.class
            );
            
            if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                response.put("success", true);
                response.put("logs", data.get("logs"));
                response.put("nextIndex", data.get("nextIndex"));
            } else {
                response.put("success", false);
                response.put("message", result != null ? result.get("error") : "获取日志失败");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取日志失败: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/tasks")
    public Map<String, Object> getAllTasks() {
        Map<String, Object> response = new HashMap<>();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.getForObject(
                mlServiceUrl + "/api/import/tasks", 
                Map.class
            );
            
            if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                response.put("success", true);
                response.put("data", result.get("data"));
            } else {
                response.put("success", true);
                response.put("data", java.util.Collections.emptyList());
            }
        } catch (Exception e) {
            response.put("success", true);
            response.put("data", java.util.Collections.emptyList());
        }
        return response;
    }

    @DeleteMapping("/task/{taskId}")
    public Map<String, Object> deleteTask(@PathVariable String taskId) {
        Map<String, Object> response = new HashMap<>();
        try {
            restTemplate.delete(mlServiceUrl + "/api/import/delete/" + taskId);
            response.put("success", true);
            response.put("message", "任务已删除");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除任务失败");
        }
        return response;
    }
}
