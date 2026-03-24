package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.User;
import com.example.backend.entity.Video;
import com.example.backend.entity.Category;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.VideoMapper;
import com.example.backend.mapper.CategoryMapper;
import com.example.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/video")
public class VideoController {
    
    @Autowired
    private VideoMapper videoMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @GetMapping("/list")
    public Map<String, Object> getVideoList(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) String sortBy,
                                           @RequestParam(required = false) String sortOrder) {
        Map<String, Object> response = new HashMap<>();
        
        Page<Video> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like("title", keyword).or().like("description", keyword).or().like("username", keyword);
        }
        if (categoryId != null) {
            queryWrapper.eq("category_id", categoryId);
        }
        
        if ("viewCount".equals(sortBy)) {
            if ("desc".equals(sortOrder)) {
                queryWrapper.orderByDesc("view_count");
            } else {
                queryWrapper.orderByAsc("view_count");
            }
        } else {
            queryWrapper.orderByDesc("create_time");
        }
        
        IPage<Video> videoPage = videoMapper.selectPage(page, queryWrapper);
        
        response.put("success", true);
        response.put("data", videoPage.getRecords());
        response.put("total", videoPage.getTotal());
        response.put("pages", videoPage.getPages());
        response.put("current", videoPage.getCurrent());
        
        return response;
    }
    
    @GetMapping("/categories")
    public Map<String, Object> getCategories() {
        Map<String, Object> response = new HashMap<>();
        
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort");
        List<Category> categories = categoryMapper.selectList(queryWrapper);
        
        response.put("success", true);
        response.put("data", categories);
        return response;
    }
    
    @GetMapping("/detail/{id}")
    public Map<String, Object> getVideoDetail(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        Video video = videoMapper.selectById(id);
        
        if (video == null) {
            response.put("success", false);
            response.put("message", "视频不存在");
            return response;
        }
        
        response.put("success", true);
        response.put("data", video);
        return response;
    }
    
    @GetMapping("/my")
    public Map<String, Object> getMyVideos(@RequestHeader("Authorization") String authorization,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "12") Integer pageSize) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            Page<Video> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", user.getId());
            queryWrapper.orderByDesc("create_time");
            
            IPage<Video> videoPage = videoMapper.selectPage(page, queryWrapper);
            
            response.put("success", true);
            response.put("data", videoPage.getRecords());
            response.put("total", videoPage.getTotal());
            response.put("pages", videoPage.getPages());
            response.put("current", videoPage.getCurrent());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取视频列表失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @PostMapping("/upload")
    public Map<String, Object> uploadVideo(@RequestParam String title,
                                           @RequestParam String description,
                                           @RequestParam(required = false) Long categoryId,
                                           @RequestParam MultipartFile video,
                                           @RequestParam(required = false) MultipartFile cover,
                                           @RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            if (video == null || video.isEmpty()) {
                response.put("success", false);
                response.put("message", "请选择视频文件");
                return response;
            }
            
            // 确保目录存在
            String uploadDir = "d:/A_Graduation_Project/project/p2_0/backend/video/" + user.getId();
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成视频文件名
            String originalFilename = video.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String videoFilename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
            String videoPath = uploadDir + "/" + videoFilename;
            
            // 保存视频文件
            video.transferTo(new File(videoPath));
            
            // 生成视频URL
            String videoUrl = "http://localhost:8080/backend/video/" + user.getId() + "/" + videoFilename;
            
            // 处理封面图片
            String coverUrl = "";
            if (cover != null && !cover.isEmpty()) {
                String coverDir = "d:/A_Graduation_Project/project/p2_0/backend/cover/" + user.getId();
                File coverDirFile = new File(coverDir);
                if (!coverDirFile.exists()) {
                    coverDirFile.mkdirs();
                }
                
                String coverOriginalFilename = cover.getOriginalFilename();
                String coverExtension = coverOriginalFilename.substring(coverOriginalFilename.lastIndexOf('.'));
                String coverFilename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + coverExtension;
                String coverPath = coverDir + "/" + coverFilename;
                
                cover.transferTo(new File(coverPath));
                coverUrl = "http://localhost:8080/backend/cover/" + user.getId() + "/" + coverFilename;
            }
            
            // 保存视频信息到数据库
            Video videoEntity = new Video(title, description, videoUrl, coverUrl, user.getId(), username);
            if (categoryId != null) {
                videoEntity.setCategoryId(categoryId);
            }
            videoMapper.insert(videoEntity);
            
            response.put("success", true);
            response.put("message", "视频上传成功");
            response.put("data", videoEntity);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "视频上传失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteVideo(@PathVariable Long id,
                                            @RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            Video video = videoMapper.selectById(id);
            if (video == null) {
                response.put("success", false);
                response.put("message", "视频不存在");
                return response;
            }
            
            if (!video.getUserId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "无权限删除此视频");
                return response;
            }
            
            videoMapper.deleteById(id);
            
            response.put("success", true);
            response.put("message", "视频删除成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "视频删除失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @PutMapping("/{id}/view")
    public Map<String, Object> incrementViewCount(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        Video video = videoMapper.selectById(id);
        if (video == null) {
            response.put("success", false);
            response.put("message", "视频不存在");
            return response;
        }
        
        UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("view_count = view_count + 1");
        updateWrapper.eq("id", id);
        videoMapper.update(null, updateWrapper);
        
        response.put("success", true);
        return response;
    }
    
    @GetMapping("/recommend")
    public Map<String, Object> getRecommendations(@RequestParam Long userId,
                                                   @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> response = new HashMap<>();
        try {
            String mlServiceUrl = "http://127.0.0.1:5001/api/recommend?userId=" + userId + "&limit=" + limit;
            
            java.net.URL url = new java.net.URL(mlServiceUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                java.io.BufferedReader in = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> mlResponse = mapper.readValue(content.toString(), Map.class);
                
                response.put("success", mlResponse.get("success"));
                response.put("data", mlResponse.get("data"));
                response.put("message", mlResponse.get("message"));
                response.put("userTags", mlResponse.get("userTags"));
            } else {
                response.put("success", false);
                response.put("message", "推荐服务暂时不可用");
            }
            conn.disconnect();
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取推荐失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/features/compute")
    public Map<String, Object> computeFeatures() {
        Map<String, Object> response = new HashMap<>();
        try {
            String mlServiceUrl = "http://127.0.0.1:5001/api/features/compute";
            
            java.net.URL url = new java.net.URL(mlServiceUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(120000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.getOutputStream().write("{}".getBytes());
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                java.io.BufferedReader in = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> mlResponse = mapper.readValue(content.toString(), Map.class);
                
                response.put("success", mlResponse.get("success"));
                response.put("message", mlResponse.get("message"));
                response.put("total", mlResponse.get("total"));
                response.put("saved", mlResponse.get("saved"));
            } else {
                response.put("success", false);
                response.put("message", "特征计算服务暂时不可用");
            }
            conn.disconnect();
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "特征计算失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/features/compute/{videoId}")
    public Map<String, Object> computeSingleFeature(@PathVariable Long videoId) {
        Map<String, Object> response = new HashMap<>();
        try {
            String mlServiceUrl = "http://127.0.0.1:5001/api/features/compute/" + videoId;
            
            java.net.URL url = new java.net.URL(mlServiceUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.getOutputStream().write("{}".getBytes());
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                java.io.BufferedReader in = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> mlResponse = mapper.readValue(content.toString(), Map.class);
                
                response.put("success", mlResponse.get("success"));
                response.put("message", mlResponse.get("message"));
                response.put("videoId", mlResponse.get("videoId"));
            } else {
                response.put("success", false);
                response.put("message", "特征计算服务暂时不可用");
            }
            conn.disconnect();
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "特征计算失败: " + e.getMessage());
        }
        return response;
    }
}
