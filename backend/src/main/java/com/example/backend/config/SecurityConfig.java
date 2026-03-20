package com.example.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Autowired
    private JwtFilter jwtFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/api/auth/**").permitAll()
            .antMatchers("/api/admin/**").permitAll()
            .antMatchers("/api/avatar/**").permitAll()
            .antMatchers("/api/video/**").permitAll()
            .antMatchers("/api/comment/**").permitAll()
            .antMatchers("/api/user/**").permitAll()
            .antMatchers("/api/tag/**").permitAll()
            .antMatchers("/api/notification/**").permitAll()
            .antMatchers("/api/chat/**").permitAll()
            .antMatchers("/api/admin/**").permitAll()
            .antMatchers("/backend/image/**").permitAll()
            .antMatchers("/api/image/**").permitAll()
            .antMatchers("/backend/video/**").permitAll()
            .antMatchers("/backend/cover/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .formLogin().disable();
        
        // 添加JWT过滤器
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }
}