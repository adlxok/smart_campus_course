package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.mapper.*;
import com.example.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/creator")
public class CreatorAnalyticsController {
    
    @Autowired
    private VideoMapper videoMapper;
    
    @Autowired
    private VideoLikeMapper videoLikeMapper;
    
    @Autowired
    private VideoFavoriteMapper videoFavoriteMapper;
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private UserFollowMapper userFollowMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @GetMapping("/analytics/overview")
    public Map<String, Object> getOverview(@RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            Long userId = user.getId();
            
            Map<String, Object> overview = new HashMap<>();
            overview.put("videoCount", videoMapper.countByUserId(userId));
            overview.put("totalViews", videoMapper.sumViewCountByUserId(userId));
            overview.put("totalLikes", videoLikeMapper.countLikesByUserId(userId));
            overview.put("totalFavorites", videoFavoriteMapper.countFavoritesByUserId(userId));
            overview.put("totalComments", commentMapper.countCommentsByUserId(userId));
            overview.put("fansCount", userFollowMapper.countFollowers(userId));
            
            response.put("success", true);
            response.put("data", overview);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取数据概览失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/analytics/trend")
    public Map<String, Object> getTrend(@RequestHeader("Authorization") String authorization,
                                        @RequestParam(defaultValue = "7") Integer days) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            Long userId = user.getId();
            String startDate = LocalDate.now().minusDays(days - 1).format(DateTimeFormatter.ISO_DATE);
            
            List<Map<String, Object>> videoTrend = videoMapper.getVideoTrendByUserId(userId, startDate);
            List<Map<String, Object>> likeTrend = videoLikeMapper.getLikeTrendByUserId(userId, startDate);
            List<Map<String, Object>> favoriteTrend = videoFavoriteMapper.getFavoriteTrendByUserId(userId, startDate);
            List<Map<String, Object>> commentTrend = commentMapper.getCommentTrendByUserId(userId, startDate);
            
            Map<String, Map<String, Integer>> trendData = new LinkedHashMap<>();
            
            for (int i = 0; i < days; i++) {
                String date = LocalDate.now().minusDays(days - 1 - i).format(DateTimeFormatter.ISO_DATE);
                trendData.put(date, new HashMap<>());
                trendData.get(date).put("videos", 0);
                trendData.get(date).put("views", 0);
                trendData.get(date).put("likes", 0);
                trendData.get(date).put("favorites", 0);
                trendData.get(date).put("comments", 0);
            }
            
            for (Map<String, Object> item : videoTrend) {
                String date = item.get("date").toString();
                if (trendData.containsKey(date)) {
                    trendData.get(date).put("videos", ((Number) item.get("count")).intValue());
                    trendData.get(date).put("views", ((Number) item.get("views")).intValue());
                }
            }
            
            for (Map<String, Object> item : likeTrend) {
                String date = item.get("date").toString();
                if (trendData.containsKey(date)) {
                    trendData.get(date).put("likes", ((Number) item.get("count")).intValue());
                }
            }
            
            for (Map<String, Object> item : favoriteTrend) {
                String date = item.get("date").toString();
                if (trendData.containsKey(date)) {
                    trendData.get(date).put("favorites", ((Number) item.get("count")).intValue());
                }
            }
            
            for (Map<String, Object> item : commentTrend) {
                String date = item.get("date").toString();
                if (trendData.containsKey(date)) {
                    trendData.get(date).put("comments", ((Number) item.get("count")).intValue());
                }
            }
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map.Entry<String, Map<String, Integer>> entry : trendData.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("date", entry.getKey());
                item.putAll(entry.getValue());
                result.add(item);
            }
            
            response.put("success", true);
            response.put("data", result);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取趋势数据失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/analytics/top-videos")
    public Map<String, Object> getTopVideos(@RequestHeader("Authorization") String authorization,
                                            @RequestParam(defaultValue = "10") Integer limit) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            Long userId = user.getId();
            
            List<Map<String, Object>> topVideos = videoMapper.getTopVideosByViews(userId, limit);
            
            for (Map<String, Object> video : topVideos) {
                Long videoId = ((Number) video.get("id")).longValue();
                video.put("likes", videoLikeMapper.countByVideoId(videoId));
                video.put("favorites", videoFavoriteMapper.countByVideoId(videoId));
                video.put("comments", commentMapper.countByVideoId(videoId));
            }
            
            response.put("success", true);
            response.put("data", topVideos);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取热门视频失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/analytics/category-distribution")
    public Map<String, Object> getCategoryDistribution(@RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            Long userId = user.getId();
            
            List<Map<String, Object>> distribution = videoMapper.getCategoryDistributionByUserId(userId);
            
            response.put("success", true);
            response.put("data", distribution);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取分类分布失败: " + e.getMessage());
        }
        
        return response;
    }
}
