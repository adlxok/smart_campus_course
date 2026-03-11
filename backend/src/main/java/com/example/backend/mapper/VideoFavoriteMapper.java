package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.VideoFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VideoFavoriteMapper extends BaseMapper<VideoFavorite> {
    
    @Select("SELECT COUNT(*) FROM video_favorite WHERE video_id = #{videoId}")
    Long countByVideoId(Long videoId);
    
    @Select("SELECT COUNT(*) > 0 FROM video_favorite WHERE video_id = #{videoId} AND user_id = #{userId}")
    boolean existsByVideoIdAndUserId(Long videoId, Long userId);
}
