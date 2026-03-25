package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.User;
import com.example.backend.entity.UserNotification;
import com.example.backend.mapper.SystemNotificationMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.UserNotificationMapper;
import com.example.backend.service.NotificationService;
import com.example.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    
    @Autowired
    private SystemNotificationMapper systemNotificationMapper;
    
    @Autowired
    private UserNotificationMapper userNotificationMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @GetMapping("/list")
    public Map<String, Object> getUserNotifications(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(token);
            
            User user = userMapper.selectByUsername(username);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            Map<String, Object> result = notificationService.getUserNotifications(user.getId());
            return result;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取通知列表失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/system/list")
    public Map<String, Object> getSystemNotifications(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(token);
            
            User user = userMapper.selectByUsername(username);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            Long userId = user.getId();
            int offset = (page - 1) * pageSize;
            List<Map<String, Object>> notifications = systemNotificationMapper.selectNotificationsForUser(userId, offset, pageSize);
            Long total = systemNotificationMapper.countNotificationsForUser(userId);
            
            response.put("success", true);
            response.put("data", notifications);
            response.put("total", total);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取系统通知列表失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(@RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(token);
            
            User user = userMapper.selectByUsername(username);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            Long systemUnreadCount = systemNotificationMapper.countUnreadForUser(user.getId());
            Map<String, Object> userNotificationResult = notificationService.getUnreadCount(user.getId());
            Long userUnreadCount = (Long) userNotificationResult.get("unreadCount");
            
            response.put("success", true);
            response.put("count", systemUnreadCount + userUnreadCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取未读数量失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/read/{notificationId}")
    public Map<String, Object> markAsRead(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long notificationId) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(token);
            
            User user = userMapper.selectByUsername(username);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            int result = userNotificationMapper.markAsRead(user.getId(), notificationId);
            
            response.put("success", result > 0);
            response.put("message", result > 0 ? "已标记为已读" : "操作失败");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/read-all")
    public Map<String, Object> markAllAsRead(@RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(token);
            
            User user = userMapper.selectByUsername(username);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            int result = userNotificationMapper.markAllAsRead(user.getId());
            notificationService.markAllAsRead(user.getId());
            
            response.put("success", true);
            response.put("message", "已全部标记为已读");
            response.put("count", result);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/{notificationId}/read")
    public Map<String, Object> markUserNotificationAsRead(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long notificationId) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(token);
            
            User user = userMapper.selectByUsername(username);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            Map<String, Object> result = notificationService.markAsRead(notificationId, user.getId());
            return result;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());
        }
        return response;
    }
}
