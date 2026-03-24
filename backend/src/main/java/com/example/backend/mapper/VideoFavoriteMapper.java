package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.VideoFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoFavoriteMapper extends BaseMapper<VideoFavorite> {
    
    @Select("SELECT COUNT(*) FROM video_favorite WHERE video_id = #{videoId}")
    Long countByVideoId(Long videoId);
    
    @Select("SELECT COUNT(*) > 0 FROM video_favorite WHERE video_id = #{videoId} AND user_id = #{userId}")
    boolean existsByVideoIdAndUserId(Long videoId, Long userId);
    
    @Select("SELECT COUNT(*) FROM video_favorite vf JOIN video v ON vf.video_id = v.id WHERE v.user_id = #{userId}")
    Long countFavoritesByUserId(@Param("userId") Long userId);
    
    @Select("SELECT DATE(vf.create_time) as date, COUNT(*) as count FROM video_favorite vf JOIN video v ON vf.video_id = v.id WHERE v.user_id = #{userId} AND vf.create_time >= #{startDate} GROUP BY DATE(vf.create_time) ORDER BY date")
    List<Map<String, Object>> getFavoriteTrendByUserId(@Param("userId") Long userId, @Param("startDate") String startDate);
}
