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
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User loginUser) {
        Map<String, Object> response = new HashMap<>();
        
        User user = userMapper.selectByUsername(loginUser.getUsername());
        
        if (user != null && passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            String token = jwtUtil.generateToken(user.getUsername());
            
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
        
        User existingUser = userMapper.selectByUsername(registerUser.getUsername());
        if (existingUser != null) {
            response.put("success", false);
            response.put("message", "用户名已存在");
            return response;
        }
        
        User user = new User(registerUser.getUsername(), passwordEncoder.encode(registerUser.getPassword()));
        user.setAvatar("http://localhost:8080/backend/image/default_image/defaultImage.png");
        userMapper.insert(user);
        
        String token = jwtUtil.generateToken(user.getUsername());
        
        response.put("success", true);
        response.put("token", token);
        response.put("user", user);
        
        return response;
    }
    
    @PostMapping("/change-password")
    public Map<String, Object> changePassword(@RequestParam String username, @RequestParam String currentPassword, @RequestParam String newPassword) {
        Map<String, Object> response = new HashMap<>();
        
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            response.put("success", false);
            response.put("message", "当前密码错误");
            return response;
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        
        response.put("success", true);
        response.put("message", "密码修改成功");
        
        return response;
    }
    
    @PostMapping("/change-avatar")
    public Map<String, Object> changeAvatar(@RequestParam String username, @RequestParam MultipartFile avatar) {
        Map<String, Object> response = new HashMap<>();
        
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }
        
        try {
            String uploadDir = "d:/A_Graduation_Project/project/p2_0/backend/image/" + user.getId();
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String originalFilename = avatar.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String filename = System.currentTimeMillis() + extension;
            String filePath = uploadDir + "/" + filename;
            
            avatar.transferTo(new File(filePath));
            
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
        
        User user = userMapper.selectByUsername(oldUsername);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }
        
        if (!username.equals(oldUsername)) {
            User existingUser = userMapper.selectByUsername(username);
            if (existingUser != null) {
                response.put("success", false);
                response.put("message", "用户名已存在");
                return response;
            }
        }
        
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
