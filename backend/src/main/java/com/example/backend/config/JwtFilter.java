package com.example.backend.config;

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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Component
public class JwtFilter extends OncePerRequestFilter {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            String authorizationHeader = request.getHeader("Authorization");
            
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                String username = JwtUtil.getUsernameFromToken(token);
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("username", username);
                    User user = userMapper.selectOne(queryWrapper);
                    if (user != null && JwtUtil.validateToken(token)) {
                        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                                .password(user.getPassword())
                                .roles(user.getRole())
                                .build();
                        
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception e) {
            // token解析失败，继续执行filter chain，不影响正常请求
        }
        
        chain.doFilter(request, response);
    }
}