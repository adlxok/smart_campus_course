package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.Conversation;
import com.example.backend.entity.PrivateMessage;
import com.example.backend.entity.User;
import com.example.backend.entity.UserFollow;
import com.example.backend.mapper.ConversationMapper;
import com.example.backend.mapper.PrivateMessageMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.UserFollowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrivateMessageService {
    
    @Autowired
    private ConversationMapper conversationMapper;
    
    @Autowired
    private PrivateMessageMapper privateMessageMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserFollowMapper userFollowMapper;
    
    public Map<String, Object> getConversations(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        List<Conversation> conversations = conversationMapper.findByUserId(userId);
        List<Map<String, Object>> conversationList = new ArrayList<>();
        
        for (Conversation conversation : conversations) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", conversation.getId());
            item.put("lastMessage", conversation.getLastMessage());
            item.put("lastMessageTime", conversation.getLastMessageTime());
            
            Long otherUserId = conversation.getUser1Id().equals(userId) 
                ? conversation.getUser2Id() 
                : conversation.getUser1Id();
            
            User otherUser = userMapper.selectById(otherUserId);
            if (otherUser != null) {
                item.put("otherUserId", otherUser.getId());
                item.put("otherUsername", otherUser.getUsername());
                item.put("otherUserAvatar", otherUser.getAvatar());
            }
            
            Long unreadCount = privateMessageMapper.countUnreadByConversationAndUser(conversation.getId(), userId);
            item.put("unreadCount", unreadCount);
            
            conversationList.add(item);
        }
        
        result.put("success", true);
        result.put("conversations", conversationList);
        return result;
    }
    
    public Map<String, Object> getMessages(Long conversationId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            result.put("success", false);
            result.put("message", "会话不存在");
            return result;
        }
        
        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
            result.put("success", false);
            result.put("message", "无权访问此会话");
            return result;
        }
        
        privateMessageMapper.markAsRead(conversationId, userId);
        
        List<PrivateMessage> messages = privateMessageMapper.findByConversationId(conversationId);
        
        Long otherUserId = conversation.getUser1Id().equals(userId) 
            ? conversation.getUser2Id() 
            : conversation.getUser1Id();
        User otherUser = userMapper.selectById(otherUserId);
        
        List<Map<String, Object>> messageList = new ArrayList<>();
        for (PrivateMessage message : messages) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", message.getId());
            item.put("senderId", message.getSenderId());
            item.put("receiverId", message.getReceiverId());
            item.put("content", message.getContent());
            item.put("createTime", message.getCreateTime());
            item.put("isMine", message.getSenderId().equals(userId));
            messageList.add(item);
        }
        
        result.put("success", true);
        result.put("messages", messageList);
        result.put("otherUser", otherUser);
        return result;
    }
    
    @Transactional
    public Map<String, Object> sendMessage(Long senderId, Long receiverId, String content) {
        Map<String, Object> result = new HashMap<>();
        
        if (senderId.equals(receiverId)) {
            result.put("success", false);
            result.put("message", "不能给自己发消息");
            return result;
        }
        
        QueryWrapper<UserFollow> followQuery1 = new QueryWrapper<>();
        followQuery1.eq("follower_id", senderId).eq("following_id", receiverId);
        UserFollow follow1 = userFollowMapper.selectOne(followQuery1);
        
        QueryWrapper<UserFollow> followQuery2 = new QueryWrapper<>();
        followQuery2.eq("follower_id", receiverId).eq("following_id", senderId);
        UserFollow follow2 = userFollowMapper.selectOne(followQuery2);
        
        if (follow1 == null && follow2 == null) {
            result.put("success", false);
            result.put("message", "关注对方或对方关注你后才能私聊");
            return result;
        }
        
        Conversation conversation = conversationMapper.findByUsers(senderId, receiverId);
        
        if (conversation == null) {
            conversation = new Conversation();
            Long user1Id = Math.min(senderId, receiverId);
            Long user2Id = Math.max(senderId, receiverId);
            conversation.setUser1Id(user1Id);
            conversation.setUser2Id(user2Id);
            conversation.setLastMessage(content);
            conversation.setLastMessageTime(LocalDateTime.now());
            conversationMapper.insert(conversation);
        } else {
            conversation.setLastMessage(content);
            conversation.setLastMessageTime(LocalDateTime.now());
            conversationMapper.updateById(conversation);
        }
        
        PrivateMessage message = new PrivateMessage();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        privateMessageMapper.insert(message);
        
        result.put("success", true);
        result.put("messageId", message.getId());
        result.put("conversationId", conversation.getId());
        return result;
    }
    
    public Map<String, Object> getOrCreateConversation(Long userId, Long otherUserId) {
        Map<String, Object> result = new HashMap<>();
        
        if (userId.equals(otherUserId)) {
            result.put("success", false);
            result.put("message", "不能和自己创建会话");
            return result;
        }
        
        QueryWrapper<UserFollow> followQuery1 = new QueryWrapper<>();
        followQuery1.eq("follower_id", userId).eq("following_id", otherUserId);
        UserFollow follow1 = userFollowMapper.selectOne(followQuery1);
        
        QueryWrapper<UserFollow> followQuery2 = new QueryWrapper<>();
        followQuery2.eq("follower_id", otherUserId).eq("following_id", userId);
        UserFollow follow2 = userFollowMapper.selectOne(followQuery2);
        
        if (follow1 == null && follow2 == null) {
            result.put("success", false);
            result.put("message", "关注对方或对方关注你后才能私聊");
            return result;
        }
        
        Conversation conversation = conversationMapper.findByUsers(userId, otherUserId);
        
        if (conversation == null) {
            conversation = new Conversation();
            Long user1Id = Math.min(userId, otherUserId);
            Long user2Id = Math.max(userId, otherUserId);
            conversation.setUser1Id(user1Id);
            conversation.setUser2Id(user2Id);
            conversationMapper.insert(conversation);
        }
        
        result.put("success", true);
        result.put("conversationId", conversation.getId());
        return result;
    }
    
    public Map<String, Object> getUnreadCount(Long userId) {
        Map<String, Object> result = new HashMap<>();
        Long unreadCount = privateMessageMapper.countUnreadByUserId(userId);
        result.put("success", true);
        result.put("unreadCount", unreadCount);
        return result;
    }
}
