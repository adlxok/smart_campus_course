package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.VideoLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoLikeMapper extends BaseMapper<VideoLike> {
    
    @Select("SELECT COUNT(*) FROM video_like WHERE video_id = #{videoId}")
    Long countByVideoId(Long videoId);
    
    @Select("SELECT COUNT(*) > 0 FROM video_like WHERE video_id = #{videoId} AND user_id = #{userId}")
    boolean existsByVideoIdAndUserId(Long videoId, Long userId);
    
    @Select("SELECT COUNT(*) FROM video_like vl JOIN video v ON vl.video_id = v.id WHERE v.user_id = #{userId}")
    Long countLikesByUserId(@Param("userId") Long userId);
    
    @Select("SELECT DATE(vl.create_time) as date, COUNT(*) as count FROM video_like vl JOIN video v ON vl.video_id = v.id WHERE v.user_id = #{userId} AND vl.create_time >= #{startDate} GROUP BY DATE(vl.create_time) ORDER BY date")
    List<Map<String, Object>> getLikeTrendByUserId(@Param("userId") Long userId, @Param("startDate") String startDate);
}
