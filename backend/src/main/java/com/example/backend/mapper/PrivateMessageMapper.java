package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.PrivateMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {
    
    @Select("SELECT * FROM private_message WHERE conversation_id = #{conversationId} ORDER BY create_time ASC")
    List<PrivateMessage> findByConversationId(Long conversationId);
    
    @Select("SELECT COUNT(*) FROM private_message WHERE receiver_id = #{userId} AND is_read = false")
    Long countUnreadByUserId(Long userId);
    
    @Select("SELECT COUNT(*) FROM private_message WHERE conversation_id = #{conversationId} AND receiver_id = #{userId} AND is_read = false")
    Long countUnreadByConversationAndUser(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    
    @Update("UPDATE private_message SET is_read = true WHERE conversation_id = #{conversationId} AND receiver_id = #{userId}")
    void markAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
