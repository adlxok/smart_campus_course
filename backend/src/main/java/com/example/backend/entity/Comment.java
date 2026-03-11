package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("comment")
public class Comment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long videoId;
    
    private Long userId;
    
    private String username;
    
    @TableField(exist = false)
    private String avatar;
    
    private String content;
    
    private Long parentId;
    
    private String replyToUsername;
    
    private LocalDateTime createTime;
    
    public Comment() {
    }
    
    public Comment(Long videoId, Long userId, String username, String content) {
        this.videoId = videoId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.createTime = LocalDateTime.now();
    }
    
    public Comment(Long videoId, Long userId, String username, String content, Long parentId, String replyToUsername) {
        this.videoId = videoId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.parentId = parentId;
        this.replyToUsername = replyToUsername;
        this.createTime = LocalDateTime.now();
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public String getReplyToUsername() {
        return replyToUsername;
    }
    
    public void setReplyToUsername(String replyToUsername) {
        this.replyToUsername = replyToUsername;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getVideoId() {
        return videoId;
    }
    
    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
