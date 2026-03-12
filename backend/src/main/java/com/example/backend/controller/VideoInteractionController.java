package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.User;
import com.example.backend.entity.Video;
import com.example.backend.entity.VideoLike;
import com.example.backend.entity.VideoFavorite;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.VideoMapper;
import com.example.backend.mapper.VideoLikeMapper;
import com.example.backend.mapper.VideoFavoriteMapper;
import com.example.backend.service.NotificationService;
import com.example.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/video")
public class VideoInteractionController {
    
    @Autowired
    private VideoLikeMapper videoLikeMapper;
    
    @Autowired
    private VideoFavoriteMapper videoFavoriteMapper;
    
    @Autowired
    private VideoMapper videoMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @PostMapping("/{videoId}/like")
    public Map<String, Object> toggleLike(@PathVariable Long videoId,
                                          @RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            Video video = videoMapper.selectById(videoId);
            if (video == null) {
                response.put("success", false);
                response.put("message", "视频不存在");
                return response;
            }
            
            QueryWrapper<VideoLike> likeQueryWrapper = new QueryWrapper<>();
            likeQueryWrapper.eq("video_id", videoId).eq("user_id", user.getId());
            VideoLike existingLike = videoLikeMapper.selectOne(likeQueryWrapper);
            
            boolean isLiked;
            if (existingLike != null) {
                videoLikeMapper.deleteById(existingLike.getId());
                isLiked = false;
            } else {
                VideoLike like = new VideoLike(videoId, user.getId());
                videoLikeMapper.insert(like);
                isLiked = true;
                notificationService.sendLikeNotification(videoId, user.getId());
            }
            
            Long likeCount = videoLikeMapper.countByVideoId(videoId);
            
            response.put("success", true);
            response.put("isLiked", isLiked);
            response.put("likeCount", likeCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @PostMapping("/{videoId}/favorite")
    public Map<String, Object> toggleFavorite(@PathVariable Long videoId,
                                              @RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            Video video = videoMapper.selectById(videoId);
            if (video == null) {
                response.put("success", false);
                response.put("message", "视频不存在");
                return response;
            }
            
            QueryWrapper<VideoFavorite> favoriteQueryWrapper = new QueryWrapper<>();
            favoriteQueryWrapper.eq("video_id", videoId).eq("user_id", user.getId());
            VideoFavorite existingFavorite = videoFavoriteMapper.selectOne(favoriteQueryWrapper);
            
            boolean isFavorited;
            if (existingFavorite != null) {
                videoFavoriteMapper.deleteById(existingFavorite.getId());
                isFavorited = false;
            } else {
                VideoFavorite favorite = new VideoFavorite(videoId, user.getId());
                videoFavoriteMapper.insert(favorite);
                isFavorited = true;
                notificationService.sendFavoriteNotification(videoId, user.getId());
            }
            
            Long favoriteCount = videoFavoriteMapper.countByVideoId(videoId);
            
            response.put("success", true);
            response.put("isFavorited", isFavorited);
            response.put("favoriteCount", favoriteCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/{videoId}/interaction")
    public Map<String, Object> getInteractionInfo(@PathVariable Long videoId,
                                                   @RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long likeCount = videoLikeMapper.countByVideoId(videoId);
            Long favoriteCount = videoFavoriteMapper.countByVideoId(videoId);
            
            boolean isLiked = false;
            boolean isFavorited = false;
            
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                String username = JwtUtil.getUsernameFromToken(token);
                
                QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                userQueryWrapper.eq("username", username);
                User user = userMapper.selectOne(userQueryWrapper);
                
                if (user != null) {
                    isLiked = videoLikeMapper.existsByVideoIdAndUserId(videoId, user.getId());
                    isFavorited = videoFavoriteMapper.existsByVideoIdAndUserId(videoId, user.getId());
                }
            }
            
            response.put("success", true);
            response.put("likeCount", likeCount);
            response.put("favoriteCount", favoriteCount);
            response.put("isLiked", isLiked);
            response.put("isFavorited", isFavorited);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/favorites")
    public Map<String, Object> getUserFavorites(@RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            QueryWrapper<VideoFavorite> favoriteQueryWrapper = new QueryWrapper<>();
            favoriteQueryWrapper.eq("user_id", user.getId()).orderByDesc("create_time");
            List<VideoFavorite> favorites = videoFavoriteMapper.selectList(favoriteQueryWrapper);
            
            List<Map<String, Object>> videoList = new ArrayList<>();
            for (VideoFavorite favorite : favorites) {
                Video video = videoMapper.selectById(favorite.getVideoId());
                if (video != null) {
                    Map<String, Object> videoInfo = new HashMap<>();
                    videoInfo.put("id", video.getId());
                    videoInfo.put("title", video.getTitle());
                    videoInfo.put("description", video.getDescription());
                    videoInfo.put("videoUrl", video.getVideoUrl());
                    videoInfo.put("coverUrl", video.getCoverUrl());
                    videoInfo.put("viewCount", video.getViewCount());
                    videoInfo.put("userId", video.getUserId());
                    videoInfo.put("username", video.getUsername());
                    videoInfo.put("createTime", video.getCreateTime());
                    videoInfo.put("favoriteTime", favorite.getCreateTime());
                    videoList.add(videoInfo);
                }
            }
            
            response.put("success", true);
            response.put("favorites", videoList);
            response.put("total", videoList.size());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/likes")
    public Map<String, Object> getUserLikes(@RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            QueryWrapper<VideoLike> likeQueryWrapper = new QueryWrapper<>();
            likeQueryWrapper.eq("user_id", user.getId()).orderByDesc("create_time");
            List<VideoLike> likes = videoLikeMapper.selectList(likeQueryWrapper);
            
            List<Map<String, Object>> videoList = new ArrayList<>();
            for (VideoLike like : likes) {
                Video video = videoMapper.selectById(like.getVideoId());
                if (video != null) {
                    Map<String, Object> videoInfo = new HashMap<>();
                    videoInfo.put("id", video.getId());
                    videoInfo.put("title", video.getTitle());
                    videoInfo.put("description", video.getDescription());
                    videoInfo.put("videoUrl", video.getVideoUrl());
                    videoInfo.put("coverUrl", video.getCoverUrl());
                    videoInfo.put("viewCount", video.getViewCount());
                    videoInfo.put("userId", video.getUserId());
                    videoInfo.put("username", video.getUsername());
                    videoInfo.put("createTime", video.getCreateTime());
                    videoInfo.put("likeTime", like.getCreateTime());
                    videoList.add(videoInfo);
                }
            }
            
            response.put("success", true);
            response.put("likes", videoList);
            response.put("total", videoList.size());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
        }
        
        return response;
    }
}
