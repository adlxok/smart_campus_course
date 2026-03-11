package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User loginUser) {
        Map<String, Object> response = new HashMap<>();
        
        // 验证用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginUser.getUsername());
        User user = userMapper.selectOne(queryWrapper);
        
        if (user != null && passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            // 生成JWT token
            String token = JwtUtil.generateToken(user.getUsername());
            
            response.put("success", true);
            response.put("token", token);
            response.put("user", user);
        } else {
            response.put("success", false);
            response.put("message", "用户名或密码错误");
        }
        
        return response;
    }
    
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User registerUser) {
        Map<String, Object> response = new HashMap<>();
        
        // 检查用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", registerUser.getUsername());
        User existingUser = userMapper.selectOne(queryWrapper);
        if (existingUser != null) {
            response.put("success", false);
            response.put("message", "用户名已存在");
            return response;
        }
        
        // 创建新用户，加密密码
        User user = new User(registerUser.getUsername(), passwordEncoder.encode(registerUser.getPassword()));
        // 设置默认头像（完整URL）
        user.setAvatar("http://localhost:8080/backend/image/default_image/defaultImage.png");
        userMapper.insert(user);
        
        // 生成JWT token
        String token = JwtUtil.generateToken(user.getUsername());
        
        response.put("success", true);
        response.put("token", token);
        response.put("user", user);
        
        return response;
    }
    
    @PostMapping("/change-password")
    public Map<String, Object> changePassword(@RequestParam String username, @RequestParam String currentPassword, @RequestParam String newPassword) {
        Map<String, Object> response = new HashMap<>();
        
        // 查找用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }
        
        // 验证当前密码
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            response.put("success", false);
            response.put("message", "当前密码错误");
            return response;
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        
        response.put("success", true);
        response.put("message", "密码修改成功");
        
        return response;
    }
    
    @PostMapping("/change-avatar")
    public Map<String, Object> changeAvatar(@RequestParam String username, @RequestParam MultipartFile avatar) {
        Map<String, Object> response = new HashMap<>();
        
        // 查找用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }
        
        // 处理头像文件
        try {
            // 确保目录存在
            String uploadDir = "d:/A_Graduation_Project/project/p2_0/backend/image/" + user.getId();
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成文件名
            String originalFilename = avatar.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String filename = System.currentTimeMillis() + extension;
            String filePath = uploadDir + "/" + filename;
            
            // 保存文件
            avatar.transferTo(new File(filePath));
            
            // 更新用户头像路径（完整URL）
            String avatarUrl = "http://localhost:8080/backend/image/" + user.getId() + "/" + filename;
            user.setAvatar(avatarUrl);
            userMapper.updateById(user);
            
            response.put("success", true);
            response.put("message", "头像修改成功");
            response.put("user", user);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "头像上传失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @PostMapping("/update-profile")
    public Map<String, Object> updateProfile(@RequestBody Map<String, String> profileData) {
        Map<String, Object> response = new HashMap<>();
        
        String username = profileData.get("username");
        String oldUsername = profileData.get("oldUsername");
        
        if (username == null || username.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "用户名不能为空");
            return response;
        }
        
        // 查找用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", oldUsername);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }
        
        // 如果修改了用户名，检查新用户名是否已存在
        if (!username.equals(oldUsername)) {
            QueryWrapper<User> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("username", username);
            User existingUser = userMapper.selectOne(checkWrapper);
            if (existingUser != null) {
                response.put("success", false);
                response.put("message", "用户名已存在");
                return response;
            }
        }
        
        // 更新用户信息
        user.setUsername(username);
        user.setGender(profileData.get("gender"));
        user.setBirthday(profileData.get("birthday"));
        user.setSignature(profileData.get("signature"));
        userMapper.updateById(user);
        
        response.put("success", true);
        response.put("message", "个人信息更新成功");
        response.put("user", user);
        
        return response;
    }
}