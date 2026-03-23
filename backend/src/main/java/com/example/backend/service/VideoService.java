package com.example.backend.service;

import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.VideoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class VideoService {
    
    @Autowired
    private VideoMapper videoMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("videoCount", videoMapper.countAll());
        stats.put("totalViews", videoMapper.sumViewCount());
        stats.put("totalCategories", videoMapper.countCategories());
        stats.put("userCount", userMapper.countAll());
        stats.put("categoryStats", videoMapper.countByCategory(10));
        return stats;
    }
}
