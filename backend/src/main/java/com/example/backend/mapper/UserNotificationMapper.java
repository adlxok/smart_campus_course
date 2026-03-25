package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.UserNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserNotificationMapper extends BaseMapper<UserNotification> {
    
    @Insert("INSERT INTO user_system_notifications (user_id, notification_id, is_read, create_time) " +
            "VALUES (#{userId}, #{notificationId}, 0, NOW()) " +
            "ON DUPLICATE KEY UPDATE is_read = is_read")
    int insertOrUpdate(@Param("userId") Long userId, @Param("notificationId") Long notificationId);
    
    @Update("UPDATE user_system_notifications SET is_read = 1, read_time = NOW() " +
            "WHERE user_id = #{userId} AND notification_id = #{notificationId}")
    int markAsRead(@Param("userId") Long userId, @Param("notificationId") Long notificationId);
    
    @Update("UPDATE user_system_notifications SET is_read = 1, read_time = NOW() " +
            "WHERE user_id = #{userId}")
    int markAllAsRead(@Param("userId") Long userId);
}
