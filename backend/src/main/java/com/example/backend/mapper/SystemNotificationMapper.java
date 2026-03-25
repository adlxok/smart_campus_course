package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.SystemNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SystemNotificationMapper extends BaseMapper<SystemNotification> {
    
    @Select("SELECT COUNT(*) FROM system_notifications WHERE status = 1")
    Long countActive();
    
    @Select("SELECT sn.*, un.is_read, un.read_time " +
            "FROM system_notifications sn " +
            "LEFT JOIN user_system_notifications un ON sn.id = un.notification_id AND un.user_id = #{userId} " +
            "WHERE sn.status = 1 " +
            "ORDER BY sn.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> selectNotificationsForUser(
        @Param("userId") Long userId,
        @Param("offset") int offset,
        @Param("limit") int limit
    );
    
    @Select("SELECT COUNT(*) FROM system_notifications sn " +
            "LEFT JOIN user_system_notifications un ON sn.id = un.notification_id AND un.user_id = #{userId} " +
            "WHERE sn.status = 1")
    Long countNotificationsForUser(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM system_notifications sn " +
            "LEFT JOIN user_system_notifications un ON sn.id = un.notification_id AND un.user_id = #{userId} " +
            "WHERE sn.status = 1 AND (un.is_read IS NULL OR un.is_read = 0)")
    Long countUnreadForUser(@Param("userId") Long userId);
}
