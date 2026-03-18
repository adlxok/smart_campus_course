package com.example.backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/admin/crawler")
public class CrawlerController {

    @Value("${ml.service.url:http://localhost:5001}")
    private String mlServiceUrl;

    private static final Map<String, CrawlerTask> runningTasks = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/start")
    public Map<String, Object> startCrawler(@RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            String url = (String) params.get("url");
            Integer maxVideos = params.get("maxVideos") != null ? (Integer) params.get("maxVideos") : 100;

            if (url == null || url.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "请输入视频URL");
                return response;
            }

            String taskId = "task_" + System.currentTimeMillis();
            CrawlerTask task = new CrawlerTask(taskId, url, maxVideos);
            runningTasks.put(taskId, task);

            Thread crawlerThread = new Thread(() -> {
                try {
                    task.setStatus("running");
                    task.setProgress("正在启动爬虫...");

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("url", url);
                    requestBody.put("maxVideos", maxVideos);
                    
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = restTemplate.postForObject(
                        mlServiceUrl + "/api/crawler/start", 
                        request, 
                        Map.class
                    );
                    
                    if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) result.get("data");
                        task.setStatus("completed");
                        task.setProgress("爬取完成");
                        task.setOutput(String.format("成功: %s 条, 失败: %s 条", 
                            data.get("successCount"), data.get("failCount")));
                    } else {
                        task.setStatus("failed");
                        task.setOutput("爬虫执行失败: " + (result != null ? result.get("error") : "未知错误"));
                    }

                } catch (Exception e) {
                    task.setStatus("failed");
                    task.setOutput("爬虫执行失败: " + e.getMessage());
                }
            });

            crawlerThread.start();

            response.put("success", true);
            response.put("taskId", taskId);
            response.put("message", "爬虫任务已启动");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "启动爬虫失败: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/status/{taskId}")
    public Map<String, Object> getTaskStatus(@PathVariable String taskId) {
        Map<String, Object> response = new HashMap<>();
        CrawlerTask task = runningTasks.get(taskId);

        if (task == null) {
            response.put("success", false);
            response.put("message", "任务不存在");
            return response;
        }

        response.put("success", true);
        response.put("status", task.getStatus());
        response.put("progress", task.getProgress());
        response.put("logs", task.getLogs());
        response.put("output", task.getOutput());
        return response;
    }

    @GetMapping("/tasks")
    public Map<String, Object> getAllTasks() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", runningTasks.values());
        return response;
    }

    @DeleteMapping("/task/{taskId}")
    public Map<String, Object> deleteTask(@PathVariable String taskId) {
        Map<String, Object> response = new HashMap<>();
        CrawlerTask removed = runningTasks.remove(taskId);
        if (removed != null) {
            response.put("success", true);
            response.put("message", "任务已删除");
        } else {
            response.put("success", false);
            response.put("message", "任务不存在");
        }
        return response;
    }

    static class CrawlerTask {
        private String taskId;
        private String url;
        private int maxVideos;
        private String status;
        private String progress;
        private StringBuilder logs;
        private String output;
        private long createTime;

        public CrawlerTask(String taskId, String url, int maxVideos) {
            this.taskId = taskId;
            this.url = url;
            this.maxVideos = maxVideos;
            this.status = "pending";
            this.progress = "等待执行...";
            this.logs = new StringBuilder();
            this.createTime = System.currentTimeMillis();
        }

        public void addLog(String log) {
            if (logs.length() > 10000) {
                logs = new StringBuilder(logs.substring(logs.length() - 5000));
            }
            logs.append(log).append("\n");
        }

        public String getTaskId() { return taskId; }
        public String getUrl() { return url; }
        public int getMaxVideos() { return maxVideos; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getProgress() { return progress; }
        public void setProgress(String progress) { this.progress = progress; }
        public String getLogs() { return logs.toString(); }
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
        public long getCreateTime() { return createTime; }
    }
}
