package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.User;
import com.example.backend.entity.Video;
import com.example.backend.entity.UserFollow;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.VideoMapper;
import com.example.backend.mapper.UserFollowMapper;
import com.example.backend.service.NotificationService;
import com.example.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/user")
public class UserFollowController {
    
    @Autowired
    private UserFollowMapper userFollowMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private VideoMapper videoMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @PostMapping("/{userId}/follow")
    public Map<String, Object> toggleFollow(@PathVariable Long userId,
                                            @RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User currentUser = userMapper.selectOne(userQueryWrapper);
            
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            if (currentUser.getId().equals(userId)) {
                response.put("success", false);
                response.put("message", "不能关注自己");
                return response;
            }
            
            User targetUser = userMapper.selectById(userId);
            if (targetUser == null) {
                response.put("success", false);
                response.put("message", "目标用户不存在");
                return response;
            }
            
            QueryWrapper<UserFollow> followQueryWrapper = new QueryWrapper<>();
            followQueryWrapper.eq("follower_id", currentUser.getId()).eq("following_id", userId);
            UserFollow existingFollow = userFollowMapper.selectOne(followQueryWrapper);
            
            boolean isFollowing;
            if (existingFollow != null) {
                userFollowMapper.deleteById(existingFollow.getId());
                isFollowing = false;
                notificationService.deleteFollowNotification(userId, currentUser.getId());
            } else {
                UserFollow follow = new UserFollow(currentUser.getId(), userId);
                userFollowMapper.insert(follow);
                isFollowing = true;
                notificationService.sendFollowNotification(userId, currentUser.getId());
            }
            
            Long followerCount = userFollowMapper.countFollowers(userId);
            
            response.put("success", true);
            response.put("isFollowing", isFollowing);
            response.put("followerCount", followerCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/{userId}/follow-status")
    public Map<String, Object> getFollowStatus(@PathVariable Long userId,
                                               @RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long followerCount = userFollowMapper.countFollowers(userId);
            Long followingCount = userFollowMapper.countFollowings(userId);
            
            boolean isFollowing = false;
            
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                String username = JwtUtil.getUsernameFromToken(token);
                
                QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                userQueryWrapper.eq("username", username);
                User currentUser = userMapper.selectOne(userQueryWrapper);
                
                if (currentUser != null) {
                    isFollowing = userFollowMapper.existsByFollowerIdAndFollowingId(currentUser.getId(), userId);
                }
            }
            
            response.put("success", true);
            response.put("followerCount", followerCount);
            response.put("followingCount", followingCount);
            response.put("isFollowing", isFollowing);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/{userId}/videos")
    public Map<String, Object> getUserVideos(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            QueryWrapper<Video> videoQueryWrapper = new QueryWrapper<>();
            videoQueryWrapper.eq("user_id", userId).orderByDesc("create_time");
            List<Video> videos = videoMapper.selectList(videoQueryWrapper);
            
            response.put("success", true);
            response.put("videos", videos);
            response.put("total", videos.size());
            response.put("user", getUserInfo(user));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/followings")
    public Map<String, Object> getMyFollowings(@RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User currentUser = userMapper.selectOne(userQueryWrapper);
            
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            QueryWrapper<UserFollow> followQueryWrapper = new QueryWrapper<>();
            followQueryWrapper.eq("follower_id", currentUser.getId()).orderByDesc("create_time");
            List<UserFollow> followings = userFollowMapper.selectList(followQueryWrapper);
            
            List<Map<String, Object>> followingList = new ArrayList<>();
            for (UserFollow follow : followings) {
                User user = userMapper.selectById(follow.getFollowingId());
                if (user != null) {
                    Map<String, Object> userInfo = getUserInfo(user);
                    userInfo.put("followTime", follow.getCreateTime());
                    followingList.add(userInfo);
                }
            }
            
            response.put("success", true);
            response.put("followings", followingList);
            response.put("total", followingList.size());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/followers")
    public Map<String, Object> getMyFollowers(@RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User currentUser = userMapper.selectOne(userQueryWrapper);
            
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            QueryWrapper<UserFollow> followQueryWrapper = new QueryWrapper<>();
            followQueryWrapper.eq("following_id", currentUser.getId()).orderByDesc("create_time");
            List<UserFollow> followers = userFollowMapper.selectList(followQueryWrapper);
            
            List<Map<String, Object>> followerList = new ArrayList<>();
            for (UserFollow follow : followers) {
                User user = userMapper.selectById(follow.getFollowerId());
                if (user != null) {
                    Map<String, Object> userInfo = getUserInfo(user);
                    userInfo.put("followTime", follow.getCreateTime());
                    boolean isFollowing = userFollowMapper.existsByFollowerIdAndFollowingId(currentUser.getId(), user.getId());
                    userInfo.put("isFollowing", isFollowing);
                    followerList.add(userInfo);
                }
            }
            
            response.put("success", true);
            response.put("followers", followerList);
            response.put("total", followerList.size());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
        }
        
        return response;
    }
    
    private Map<String, Object> getUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("signature", user.getSignature());
        userInfo.put("followerCount", userFollowMapper.countFollowers(user.getId()));
        userInfo.put("followingCount", userFollowMapper.countFollowings(user.getId()));
        
        QueryWrapper<Video> videoQueryWrapper = new QueryWrapper<>();
        videoQueryWrapper.eq("user_id", user.getId());
        Long videoCount = videoMapper.selectCount(videoQueryWrapper);
        userInfo.put("videoCount", videoCount);
        
        return userInfo;
    }
}
