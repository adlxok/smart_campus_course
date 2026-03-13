package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.backend.dto.NotificationMessage;
import com.example.backend.entity.Notification;
import com.example.backend.entity.User;
import com.example.backend.entity.Video;
import com.example.backend.kafka.NotificationProducer;
import com.example.backend.mapper.NotificationMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.VideoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationMapper notificationMapper;
    
    @Autowired
    private NotificationProducer notificationProducer;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private VideoMapper videoMapper;
    
    public void sendFavoriteNotification(Long videoId, Long fromUserId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            return;
        }
        
        Long toUserId = video.getUserId();
        if (toUserId.equals(fromUserId)) {
            return;
        }
        
        User fromUser = userMapper.selectById(fromUserId);
        if (fromUser == null) {
            return;
        }
        
        NotificationMessage message = new NotificationMessage();
        message.setUserId(toUserId);
        message.setType("FAVORITE");
        message.setTitle("作品被收藏");
        message.setContent(fromUser.getUsername() + " 收藏了你的作品「" + video.getTitle() + "」");
        message.setRelatedId(videoId);
        message.setFromUserId(fromUserId);
        
        notificationProducer.sendNotification(message);
    }
    
    public void sendLikeNotification(Long videoId, Long fromUserId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            return;
        }
        
        Long toUserId = video.getUserId();
        if (toUserId.equals(fromUserId)) {
            return;
        }
        
        User fromUser = userMapper.selectById(fromUserId);
        if (fromUser == null) {
            return;
        }
        
        NotificationMessage message = new NotificationMessage();
        message.setUserId(toUserId);
        message.setType("LIKE");
        message.setTitle("作品被点赞");
        message.setContent(fromUser.getUsername() + " 点赞了你的作品「" + video.getTitle() + "」");
        message.setRelatedId(videoId);
        message.setFromUserId(fromUserId);
        
        notificationProducer.sendNotification(message);
    }
    
    public void sendFollowNotification(Long toUserId, Long fromUserId) {
        if (toUserId.equals(fromUserId)) {
            return;
        }
        
        User fromUser = userMapper.selectById(fromUserId);
        if (fromUser == null) {
            return;
        }
        
        NotificationMessage message = new NotificationMessage();
        message.setUserId(toUserId);
        message.setType("FOLLOW");
        message.setTitle("新增粉丝");
        message.setContent(fromUser.getUsername() + " 关注了你");
        message.setRelatedId(null);
        message.setFromUserId(fromUserId);
        
        notificationProducer.sendNotification(message);
    }
    
    public Map<String, Object> getUserNotifications(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        List<Notification> notifications = notificationMapper.findByUserIdOrderByCreateTimeDesc(userId);
        Long unreadCount = notificationMapper.countUnreadByUserId(userId);
        
        List<Map<String, Object>> notificationList = new ArrayList<>();
        for (Notification notification : notifications) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", notification.getId());
            item.put("userId", notification.getUserId());
            item.put("type", notification.getType());
            item.put("title", notification.getTitle());
            item.put("content", notification.getContent());
            item.put("relatedId", notification.getRelatedId());
            item.put("fromUserId", notification.getFromUserId());
            item.put("isRead", notification.getIsRead());
            item.put("createTime", notification.getCreateTime());
            
            if (notification.getFromUserId() != null) {
                User fromUser = userMapper.selectById(notification.getFromUserId());
                if (fromUser != null) {
                    item.put("fromUsername", fromUser.getUsername());
                    item.put("fromUserAvatar", fromUser.getAvatar());
                }
            }
            
            notificationList.add(item);
        }
        
        result.put("success", true);
        result.put("notifications", notificationList);
        result.put("unreadCount", unreadCount);
        return result;
    }
    
    public Map<String, Object> markAsRead(Long notificationId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "通知不存在或无权操作");
            return result;
        }
        
        notification.setIsRead(true);
        notificationMapper.updateById(notification);
        
        result.put("success", true);
        result.put("message", "已标记为已读");
        return result;
    }
    
    public Map<String, Object> markAllAsRead(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        UpdateWrapper<Notification> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId).eq("is_read", false);
        updateWrapper.set("is_read", true);
        
        notificationMapper.update(null, updateWrapper);
        
        result.put("success", true);
        result.put("message", "全部已标记为已读");
        return result;
    }
    
    public Map<String, Object> getUnreadCount(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        Long unreadCount = notificationMapper.countUnreadByUserId(userId);
        
        result.put("success", true);
        result.put("unreadCount", unreadCount);
        return result;
    }
    
    public void deleteFavoriteNotification(Long videoId, Long fromUserId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            return;
        }
        
        Long toUserId = video.getUserId();
        
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", toUserId)
                   .eq("type", "FAVORITE")
                   .eq("related_id", videoId)
                   .eq("from_user_id", fromUserId);
        
        notificationMapper.delete(queryWrapper);
    }
    
    public void deleteLikeNotification(Long videoId, Long fromUserId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            return;
        }
        
        Long toUserId = video.getUserId();
        
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", toUserId)
                   .eq("type", "LIKE")
                   .eq("related_id", videoId)
                   .eq("from_user_id", fromUserId);
        
        notificationMapper.delete(queryWrapper);
    }
    
    public void deleteFollowNotification(Long toUserId, Long fromUserId) {
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", toUserId)
                   .eq("type", "FOLLOW")
                   .eq("from_user_id", fromUserId);
        
        notificationMapper.delete(queryWrapper);
    }
}
