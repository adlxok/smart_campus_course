package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    @Select("SELECT COUNT(*) FROM comment WHERE video_id = #{videoId}")
    Long countByVideoId(@Param("videoId") Long videoId);
    
    @Select("SELECT COUNT(*) FROM comment c JOIN video v ON c.video_id = v.id WHERE v.user_id = #{userId}")
    Long countCommentsByUserId(@Param("userId") Long userId);
    
    @Select("SELECT DATE(c.create_time) as date, COUNT(*) as count FROM comment c JOIN video v ON c.video_id = v.id WHERE v.user_id = #{userId} AND c.create_time >= #{startDate} GROUP BY DATE(c.create_time) ORDER BY date")
    List<Map<String, Object>> getCommentTrendByUserId(@Param("userId") Long userId, @Param("startDate") String startDate);
}
