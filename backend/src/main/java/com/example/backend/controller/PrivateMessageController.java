package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.PrivateMessageService;
import com.example.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class PrivateMessageController {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PrivateMessageService privateMessageService;
    
    @Autowired
    private UserMapper userMapper;
    
    @GetMapping("/conversations")
    public Map<String, Object> getConversations(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            return privateMessageService.getConversations(user.getId());
        } catch (Exception e) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", "获取会话列表失败: " + e.getMessage());
            return response;
        }
    }
    
    @GetMapping("/messages/{conversationId}")
    public Map<String, Object> getMessages(@PathVariable Long conversationId,
                                           @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            return privateMessageService.getMessages(conversationId, user.getId());
        } catch (Exception e) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", "获取消息失败: " + e.getMessage());
            return response;
        }
    }
    
    @PostMapping("/send")
    public Map<String, Object> sendMessage(@RequestBody Map<String, Object> params,
                                           @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            Long receiverId = Long.valueOf(params.get("receiverId").toString());
            String content = params.get("content").toString();
            
            return privateMessageService.sendMessage(user.getId(), receiverId, content);
        } catch (Exception e) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", "发送消息失败: " + e.getMessage());
            return response;
        }
    }
    
    @PostMapping("/conversation/{otherUserId}")
    public Map<String, Object> getOrCreateConversation(@PathVariable Long otherUserId,
                                                       @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            return privateMessageService.getOrCreateConversation(user.getId(), otherUserId);
        } catch (Exception e) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", "创建会话失败: " + e.getMessage());
            return response;
        }
    }
    
    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            return privateMessageService.getUnreadCount(user.getId());
        } catch (Exception e) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", "获取未读数失败: " + e.getMessage());
            return response;
        }
    }
}
