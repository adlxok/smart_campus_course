package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

@TableName("user_system_notifications")
public class UserNotification {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("notification_id")
    private Long notificationId;
    
    @TableField("is_read")
    private Integer isRead;
    
    @TableField("read_time")
    private Date readTime;
    
    @TableField("create_time")
    private Date createTime;
    
    public UserNotification() {
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getNotificationId() {
        return notificationId;
    }
    
    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }
    
    public Integer getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }
    
    public Date getReadTime() {
        return readTime;
    }
    
    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
