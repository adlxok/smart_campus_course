package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.SystemNotification;
import com.example.backend.entity.User;
import com.example.backend.entity.UserNotification;
import com.example.backend.mapper.SystemNotificationMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.UserNotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/notification")
public class AdminNotificationController {
    
    @Autowired
    private SystemNotificationMapper notificationMapper;
    
    @Autowired
    private UserNotificationMapper userNotificationMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @GetMapping("/list")
    public Map<String, Object> getNotificationList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<SystemNotification> pageParam = new Page<>(page, pageSize);
            QueryWrapper<SystemNotification> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("create_time");
            
            IPage<SystemNotification> pageResult = notificationMapper.selectPage(pageParam, queryWrapper);
            
            response.put("success", true);
            response.put("data", pageResult.getRecords());
            response.put("total", pageResult.getTotal());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取通知列表失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/add")
    public Map<String, Object> addNotification(
            @RequestBody SystemNotification notification,
            @RequestParam(required = false) List<Long> userIds) {
        Map<String, Object> response = new HashMap<>();
        try {
            notification.setCreateTime(new Date());
            notification.setUpdateTime(new Date());
            if (notification.getStatus() == null) {
                notification.setStatus(1);
            }
            if (notification.getType() == null) {
                notification.setType("normal");
            }
            
            notificationMapper.insert(notification);
            
            if (userIds != null && !userIds.isEmpty()) {
                for (Long userId : userIds) {
                    userNotificationMapper.insertOrUpdate(userId, notification.getId());
                }
            } else if (notification.getStatus() == 1) {
                List<User> users = userMapper.selectList(null);
                for (User user : users) {
                    userNotificationMapper.insertOrUpdate(user.getId(), notification.getId());
                }
            }
            
            response.put("success", true);
            response.put("message", "添加通知成功");
            response.put("data", notification);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加通知失败: " + e.getMessage());
        }
        return response;
    }
    
    @PutMapping("/update")
    public Map<String, Object> updateNotification(@RequestBody SystemNotification notification) {
        Map<String, Object> response = new HashMap<>();
        try {
            notification.setUpdateTime(new Date());
            notificationMapper.updateById(notification);
            
            response.put("success", true);
            response.put("message", "更新通知成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新通知失败: " + e.getMessage());
        }
        return response;
    }
    
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteNotification(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            notificationMapper.deleteById(id);
            
            QueryWrapper<UserNotification> wrapper = new QueryWrapper<>();
            wrapper.eq("notification_id", id);
            userNotificationMapper.delete(wrapper);
            
            response.put("success", true);
            response.put("message", "删除通知成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除通知失败: " + e.getMessage());
        }
        return response;
    }
    
    @PutMapping("/toggle/{id}")
    public Map<String, Object> toggleNotificationStatus(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            SystemNotification notification = notificationMapper.selectById(id);
            if (notification == null) {
                response.put("success", false);
                response.put("message", "通知不存在");
                return response;
            }
            
            notification.setStatus(notification.getStatus() == 1 ? 0 : 1);
            notification.setUpdateTime(new Date());
            notificationMapper.updateById(notification);
            
            response.put("success", true);
            response.put("message", notification.getStatus() == 1 ? "已启用" : "已禁用");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/send-to-users/{notificationId}")
    public Map<String, Object> sendToUsers(
            @PathVariable Long notificationId,
            @RequestBody List<Long> userIds) {
        Map<String, Object> response = new HashMap<>();
        try {
            SystemNotification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                response.put("success", false);
                response.put("message", "通知不存在");
                return response;
            }
            
            int count = 0;
            for (Long userId : userIds) {
                int result = userNotificationMapper.insertOrUpdate(userId, notificationId);
                if (result > 0) count++;
            }
            
            response.put("success", true);
            response.put("message", "发送成功，已发送给 " + count + " 个用户");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "发送失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/send-to-all/{notificationId}")
    public Map<String, Object> sendToAll(@PathVariable Long notificationId) {
        Map<String, Object> response = new HashMap<>();
        try {
            SystemNotification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                response.put("success", false);
                response.put("message", "通知不存在");
                return response;
            }
            
            List<User> users = userMapper.selectList(null);
            int count = 0;
            for (User user : users) {
                int result = userNotificationMapper.insertOrUpdate(user.getId(), notificationId);
                if (result > 0) count++;
            }
            
            response.put("success", true);
            response.put("message", "发送成功，已发送给 " + count + " 个用户");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "发送失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/users")
    public Map<String, Object> getUserList() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<User> users = userMapper.selectList(null);
            response.put("success", true);
            response.put("data", users);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取用户列表失败: " + e.getMessage());
        }
        return response;
    }
}
