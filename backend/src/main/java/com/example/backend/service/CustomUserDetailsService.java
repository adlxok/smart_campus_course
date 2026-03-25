package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.Permission;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.mapper.PermissionMapper;
import com.example.backend.mapper.RoleMapper;
import com.example.backend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private PermissionMapper permissionMapper;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);
        
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        List<Role> roles = userMapper.selectRolesByUserId(user.getId());
        Set<String> permissionCodes = new HashSet<>();
        
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
            
            List<Permission> permissions = permissionMapper.selectPermissionsByRoleId(role.getId());
            for (Permission permission : permissions) {
                permissionCodes.add(permission.getCode());
            }
        }
        
        for (String code : permissionCodes) {
            authorities.add(new SimpleGrantedAuthority(code));
        }
        
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            authorities
        );
    }
    
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }
    
    public List<Role> getUserRoles(Long userId) {
        return userMapper.selectRolesByUserId(userId);
    }
    
    public List<String> getUserPermissionCodes(Long userId) {
        List<Role> roles = userMapper.selectRolesByUserId(userId);
        Set<String> permissionCodes = new HashSet<>();
        
        for (Role role : roles) {
            List<Permission> permissions = permissionMapper.selectPermissionsByRoleId(role.getId());
            for (Permission permission : permissions) {
                permissionCodes.add(permission.getCode());
            }
        }
        
        return new ArrayList<>(permissionCodes);
    }
}
