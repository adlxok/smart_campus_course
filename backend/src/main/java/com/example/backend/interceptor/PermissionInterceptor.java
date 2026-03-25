package com.example.backend.interceptor;

import com.example.backend.annotation.RequirePermission;
import com.example.backend.entity.Permission;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.mapper.PermissionMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class PermissionInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PermissionMapper permissionMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequirePermission annotation = handlerMethod.getMethodAnnotation(RequirePermission.class);
        
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }
        
        if (annotation == null) {
            return true;
        }
        
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            sendErrorResponse(response, "未登录或token无效");
            return false;
        }
        
        token = token.substring(7);
        String username;
        try {
            username = jwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            sendErrorResponse(response, "token解析失败");
            return false;
        }
        
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            sendErrorResponse(response, "用户不存在");
            return false;
        }
        
        List<Role> roles = userMapper.selectRolesByUserId(user.getId());
        if (roles == null || roles.isEmpty()) {
            sendErrorResponse(response, "用户未分配角色");
            return false;
        }
        
        Set<String> permissionCodes = new HashSet<>();
        boolean isSuperAdmin = false;
        
        for (Role role : roles) {
            if ("SUPER_ADMIN".equals(role.getCode())) {
                isSuperAdmin = true;
                break;
            }
            
            List<Permission> permissions = permissionMapper.selectPermissionsByRoleId(role.getId());
            for (Permission permission : permissions) {
                permissionCodes.add(permission.getCode());
            }
        }
        
        if (isSuperAdmin) {
            return true;
        }
        
        String[] requiredPermissions = annotation.value();
        String logical = annotation.logical();
        
        boolean hasPermission = false;
        if ("OR".equalsIgnoreCase(logical)) {
            hasPermission = checkOrPermission(permissionCodes, requiredPermissions);
        } else {
            hasPermission = checkAndPermission(permissionCodes, requiredPermissions);
        }
        
        if (!hasPermission) {
            sendErrorResponse(response, "权限不足");
            return false;
        }
        
        return true;
    }
    
    private boolean checkAndPermission(Set<String> userPermissionCodes, String[] requiredPermissions) {
        for (String required : requiredPermissions) {
            if (!userPermissionCodes.contains(required)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean checkOrPermission(Set<String> userPermissionCodes, String[] requiredPermissions) {
        for (String required : requiredPermissions) {
            if (userPermissionCodes.contains(required)) {
                return true;
            }
        }
        return false;
    }
    
    private void sendErrorResponse(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        result.put("code", 403);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
