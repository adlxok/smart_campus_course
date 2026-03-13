package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
    
    @Select("SELECT * FROM conversation WHERE (user1_id = #{userId} OR user2_id = #{userId}) ORDER BY last_message_time DESC")
    List<Conversation> findByUserId(Long userId);
    
    @Select("SELECT * FROM conversation WHERE (user1_id = #{user1Id} AND user2_id = #{user2Id}) OR (user1_id = #{user2Id} AND user2_id = #{user1Id})")
    Conversation findByUsers(Long user1Id, Long user2Id);
}
