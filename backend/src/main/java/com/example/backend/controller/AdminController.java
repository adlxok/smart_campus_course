package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.entity.BilibiliVideo;
import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.BilibiliVideoService;
import com.example.backend.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private BilibiliVideoService bilibiliVideoService;
    
    @Autowired
    private VideoService videoService;
    
    @Autowired
    private UserMapper userMapper;
    
    @GetMapping("/videos")
    public Map<String, Object> getVideoList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> response = new HashMap<>();
        try {
            IPage<BilibiliVideo> pageResult = bilibiliVideoService.getVideoList(page, pageSize, keyword);
            response.put("success", true);
            response.put("data", pageResult.getRecords());
            response.put("total", pageResult.getTotal());
            response.put("page", page);
            response.put("pageSize", pageSize);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取视频列表失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/video/{bvid}")
    public Map<String, Object> getVideoDetail(@PathVariable String bvid) {
        Map<String, Object> response = new HashMap<>();
        try {
            BilibiliVideo video = bilibiliVideoService.getVideoByBvid(bvid);
            if (video != null) {
                response.put("success", true);
                response.put("data", video);
            } else {
                response.put("success", false);
                response.put("message", "视频不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取视频详情失败: " + e.getMessage());
        }
        return response;
    }
    
    @DeleteMapping("/video/{bvid}")
    public Map<String, Object> deleteVideo(@PathVariable String bvid) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean deleted = bilibiliVideoService.deleteVideo(bvid);
            if (deleted) {
                response.put("success", true);
                response.put("message", "删除成功");
            } else {
                response.put("success", false);
                response.put("message", "视频不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> stats = videoService.getStatistics();
            response.put("success", true);
            response.put("data", stats);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取统计数据失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/users")
    public Map<String, Object> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Map<String, Object> response = new HashMap<>();
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("created_at");
            
            List<User> users = userMapper.selectList(queryWrapper);
            int total = users.size();
            int fromIndex = (page - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, total);
            
            response.put("success", true);
            response.put("data", users.subList(fromIndex, toIndex));
            response.put("total", total);
            response.put("page", page);
            response.put("pageSize", pageSize);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取用户列表失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/check-admin")
    public Map<String, Object> checkAdmin(@RequestBody Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = data.get("username");
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            User user = userMapper.selectOne(queryWrapper);
            
            if (user != null && "ADMIN".equals(user.getRole())) {
                response.put("success", true);
                response.put("isAdmin", true);
            } else {
                response.put("success", true);
                response.put("isAdmin", false);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "检查管理员权限失败: " + e.getMessage());
        }
        return response;
    }
}
