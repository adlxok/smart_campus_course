package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.BilibiliVideo;
import com.example.backend.mapper.BilibiliVideoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BilibiliVideoService {
    
    @Autowired
    private BilibiliVideoMapper bilibiliVideoMapper;
    
    public IPage<BilibiliVideo> getVideoList(int page, int pageSize, String keyword) {
        Page<BilibiliVideo> pageParam = new Page<>(page, pageSize);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return bilibiliVideoMapper.searchByKeyword(pageParam, keyword);
        }
        QueryWrapper<BilibiliVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return bilibiliVideoMapper.selectPage(pageParam, queryWrapper);
    }
    
    public BilibiliVideo getVideoByBvid(String bvid) {
        QueryWrapper<BilibiliVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bvid", bvid);
        return bilibiliVideoMapper.selectOne(queryWrapper);
    }
    
    public boolean deleteVideo(String bvid) {
        QueryWrapper<BilibiliVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bvid", bvid);
        return bilibiliVideoMapper.delete(queryWrapper) > 0;
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("videoCount", bilibiliVideoMapper.countAll());
        stats.put("totalViews", bilibiliVideoMapper.sumViewCount());
        stats.put("totalLikes", bilibiliVideoMapper.sumLikeCount());
        stats.put("categoryStats", bilibiliVideoMapper.countByCategory(10));
        return stats;
    }
}
