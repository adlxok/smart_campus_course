package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.NotificationService;
import com.example.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserMapper userMapper;
    
    @GetMapping("/list")
    public Map<String, Object> getNotifications(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            return notificationService.getUserNotifications(user.getId());
        } catch (Exception e) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", "获取通知失败: " + e.getMessage());
            return response;
        }
    }
    
    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            return notificationService.getUnreadCount(user.getId());
        } catch (Exception e) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", "获取未读数失败: " + e.getMessage());
            return response;
        }
    }
    
    @PostMapping("/{id}/read")
    public Map<String, Object> markAsRead(@PathVariable Long id,
                                          @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            return notificationService.markAsRead(id, user.getId());
        } catch (Exception e) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());
            return response;
        }
    }
    
    @PostMapping("/read-all")
    public Map<String, Object> markAllAsRead(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            return notificationService.markAllAsRead(user.getId());
        } catch (Exception e) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());
            return response;
        }
    }
}
