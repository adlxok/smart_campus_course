package com.example.backend.config;

import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            String authorizationHeader = request.getHeader("Authorization");
            
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                String username = jwtUtil.getUsernameFromToken(token);
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    User user = userMapper.selectByUsername(username);
                    if (user != null && jwtUtil.validateToken(token)) {
                        List<Role> roles = userMapper.selectRolesByUserId(user.getId());
                        List<String> roleCodes = new ArrayList<>();
                        for (Role role : roles) {
                            roleCodes.add(role.getCode());
                        }
                        
                        String[] roleArray = roleCodes.toArray(new String[0]);
                        if (roleArray.length == 0) {
                            roleArray = new String[]{"USER"};
                        }
                        
                        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                                .password(user.getPassword())
                                .roles(roleArray)
                                .build();
                        
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception e) {
        }
        
        chain.doFilter(request, response);
    }
}
