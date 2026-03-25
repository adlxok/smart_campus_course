package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.annotation.RequirePermission;
import com.example.backend.entity.BilibiliVideo;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.entity.UserRole;
import com.example.backend.mapper.RoleMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.UserRoleMapper;
import com.example.backend.service.BilibiliVideoService;
import com.example.backend.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/videos")
    public Map<String, Object> getVideoList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> response = new HashMap<>();
        try {
            IPage<BilibiliVideo> videoPage = bilibiliVideoService.getVideoList(page, pageSize, keyword);
            response.put("success", true);
            response.put("data", videoPage.getRecords());
            response.put("total", videoPage.getTotal());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取视频列表失败: " + e.getMessage());
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
            bilibiliVideoService.deleteVideo(bvid);
            response.put("success", true);
            response.put("message", "删除成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除视频失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/users")
    @RequirePermission("system:user:manage")
    public Map<String, Object> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<User> userPage = new Page<>(page, pageSize);
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.like("username", "%" + keyword + "%");
            }
            queryWrapper.orderByDesc("create_time");
            IPage<User> result = userMapper.selectPage(userPage, queryWrapper);
            
            List<Map<String, Object>> userList = new ArrayList<>();
            for (User user : result.getRecords()) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("avatar", user.getAvatar());
                userMap.put("gender", user.getGender());
                userMap.put("birthday", user.getBirthday());
                userMap.put("signature", user.getSignature());
                userMap.put("createTime", user.getCreateTime());
                
                List<Role> roles = userMapper.selectRolesByUserId(user.getId());
                List<Map<String, Object>> roleList = new ArrayList<>();
                for (Role role : roles) {
                    Map<String, Object> roleMap = new HashMap<>();
                    roleMap.put("id", role.getId());
                    roleMap.put("name", role.getName());
                    roleMap.put("code", role.getCode());
                    roleList.add(roleMap);
                }
                userMap.put("roles", roleList);
                userList.add(userMap);
            }
            
            response.put("success", true);
            response.put("data", userList);
            response.put("total", result.getTotal());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取用户列表失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/user/{id}")
    @RequirePermission("system:user:manage")
    public Map<String, Object> getUserDetail(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userMapper.selectById(id);
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("avatar", user.getAvatar());
            userMap.put("gender", user.getGender());
            userMap.put("birthday", user.getBirthday());
            userMap.put("signature", user.getSignature());
            userMap.put("createTime", user.getCreateTime());
            
            List<Role> roles = userMapper.selectRolesByUserId(user.getId());
            List<Long> roleIds = new ArrayList<>();
            for (Role role : roles) {
                roleIds.add(role.getId());
            }
            userMap.put("roleIds", roleIds);
            
            response.put("success", true);
            response.put("data", userMap);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取用户详情失败: " + e.getMessage());
        }
        return response;
    }
    
    @PutMapping("/user/{id}")
    @RequirePermission("system:user:manage")
    public Map<String, Object> updateUser(@PathVariable Long id, @RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            User existing = userMapper.selectById(id);
            if (existing == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            if (user.getUsername() != null) {
                existing.setUsername(user.getUsername());
            }
            if (user.getAvatar() != null) {
                existing.setAvatar(user.getAvatar());
            }
            if (user.getGender() != null) {
                existing.setGender(user.getGender());
            }
            if (user.getBirthday() != null) {
                existing.setBirthday(user.getBirthday());
            }
            if (user.getSignature() != null) {
                existing.setSignature(user.getSignature());
            }
            
            userMapper.updateById(existing);
            response.put("success", true);
            response.put("message", "更新成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新用户失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/user/add")
    @RequirePermission("system:user:manage")
    public Map<String, Object> addUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", user.getUsername());
            if (userMapper.selectCount(queryWrapper) > 0) {
                response.put("success", false);
                response.put("message", "用户名已存在");
                return response;
            }
            
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                response.put("success", false);
                response.put("message", "密码不能为空");
                return response;
            }
            
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            if (user.getGender() == null || user.getGender().isEmpty()) {
                user.setGender("保密");
            }
            
            userMapper.insert(user);
            
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(4L);
            userRoleMapper.insert(userRole);
            
            response.put("success", true);
            response.put("message", "添加成功");
            response.put("data", user);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加用户失败: " + e.getMessage());
        }
        return response;
    }
    
    @DeleteMapping("/user/{id}")
    @RequirePermission("system:user:manage")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            QueryWrapper<UserRole> urQuery = new QueryWrapper<>();
            urQuery.eq("user_id", id);
            userRoleMapper.delete(urQuery);
            
            userMapper.deleteById(id);
            response.put("success", true);
            response.put("message", "删除成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除用户失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/user/{id}/roles")
    @RequirePermission("system:role:manage")
    public Map<String, Object> assignUserRoles(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userMapper.selectById(id);
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            List<Integer> roleIds = (List<Integer>) params.get("roleIds");
            
            QueryWrapper<UserRole> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", id);
            userRoleMapper.delete(queryWrapper);
            
            for (Integer roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(id);
                userRole.setRoleId(roleId.longValue());
                userRoleMapper.insert(userRole);
            }
            
            response.put("success", true);
            response.put("message", "角色分配成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "角色分配失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/roles")
    public Map<String, Object> getAllRoles() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Role> roles = roleMapper.selectList(null);
            response.put("success", true);
            response.put("data", roles);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取角色列表失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/check-admin")
    public Map<String, Object> checkAdmin(@RequestBody Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = data.get("username");
            User user = userMapper.selectByUsername(username);
            
            if (user != null) {
                List<Role> roles = userMapper.selectRolesByUserId(user.getId());
                boolean isAdmin = false;
                for (Role role : roles) {
                    if ("SUPER_ADMIN".equals(role.getCode()) || 
                        "SYSTEM_ADMIN".equals(role.getCode()) || 
                        "DATA_ADMIN".equals(role.getCode())) {
                        isAdmin = true;
                        break;
                    }
                }
                response.put("success", true);
                response.put("isAdmin", isAdmin);
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
