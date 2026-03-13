package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("conversation")
public class Conversation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long user1Id;
    
    private Long user2Id;
    
    private String lastMessage;
    
    private LocalDateTime lastMessageTime;
    
    private LocalDateTime createTime;
    
    public Conversation() {
        this.createTime = LocalDateTime.now();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUser1Id() {
        return user1Id;
    }
    
    public void setUser1Id(Long user1Id) {
        this.user1Id = user1Id;
    }
    
    public Long getUser2Id() {
        return user2Id;
    }
    
    public void setUser2Id(Long user2Id) {
        this.user2Id = user2Id;
    }
    
    public String getLastMessage() {
        return lastMessage;
    }
    
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    
    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }
    
    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
