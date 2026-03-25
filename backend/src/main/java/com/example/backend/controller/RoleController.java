package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.Permission;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.entity.UserRole;
import com.example.backend.mapper.PermissionMapper;
import com.example.backend.mapper.RoleMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.UserRoleMapper;
import com.example.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/role")
public class RoleController {
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private PermissionMapper permissionMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @GetMapping("/list")
    public Map<String, Object> getRoleList() {
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
    
    @GetMapping("/permissions/{roleId}")
    public Map<String, Object> getRolePermissions(@PathVariable Long roleId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Permission> permissions = permissionMapper.selectPermissionsByRoleId(roleId);
            response.put("success", true);
            response.put("data", permissions);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取角色权限失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/user/info")
    public Map<String, Object> getUserRoleAndPermissions(@RequestHeader("Authorization") String authorization) {
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
            
            List<Role> roles = userMapper.selectRolesByUserId(user.getId());
            
            Set<String> permissionCodes = new HashSet<>();
            Role primaryRole = null;
            
            for (Role role : roles) {
                if (primaryRole == null || role.getId() < primaryRole.getId()) {
                    primaryRole = role;
                }
                
                List<Permission> permissions = permissionMapper.selectPermissionsByRoleId(role.getId());
                for (Permission permission : permissions) {
                    permissionCodes.add(permission.getCode());
                }
            }
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("gender", user.getGender());
            userInfo.put("birthday", user.getBirthday());
            userInfo.put("signature", user.getSignature());
            
            if (primaryRole != null) {
                userInfo.put("role", primaryRole.getName());
                userInfo.put("roleCode", primaryRole.getCode());
                userInfo.put("roleId", primaryRole.getId());
            } else {
                userInfo.put("role", "普通用户");
                userInfo.put("roleCode", "USER");
                userInfo.put("roleId", 4);
            }
            
            userInfo.put("permissions", new ArrayList<>(permissionCodes));
            
            response.put("success", true);
            response.put("data", userInfo);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取用户信息失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/permissions/all")
    public Map<String, Object> getAllPermissions() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Permission> permissions = permissionMapper.selectList(null);
            response.put("success", true);
            response.put("data", permissions);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取权限列表失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/assign")
    public Map<String, Object> assignRoleToUser(@RequestBody Map<String, Long> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = params.get("userId");
            Long roleId = params.get("roleId");
            
            User user = userMapper.selectById(userId);
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            QueryWrapper<UserRole> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            userRoleMapper.delete(queryWrapper);
            
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
            
            response.put("success", true);
            response.put("message", "角色分配成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "角色分配失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/assign-multiple")
    public Map<String, Object> assignMultipleRolesToUser(@RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.parseLong(params.get("userId").toString());
            List<Integer> roleIds = (List<Integer>) params.get("roleIds");
            
            User user = userMapper.selectById(userId);
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            QueryWrapper<UserRole> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            userRoleMapper.delete(queryWrapper);
            
            for (Integer roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
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
    
    @GetMapping("/user/{userId}/roles")
    public Map<String, Object> getUserRoles(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Role> roles = userMapper.selectRolesByUserId(userId);
            response.put("success", true);
            response.put("data", roles);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取用户角色失败: " + e.getMessage());
        }
        return response;
    }
}
