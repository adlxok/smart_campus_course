package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {
    
    @Select("SELECT COUNT(*) FROM video")
    Long countAll();
    
    @Select("SELECT COALESCE(SUM(view_count), 0) FROM video")
    Long sumViewCount();
    
    @Select("SELECT COUNT(DISTINCT category_id) FROM video WHERE category_id IS NOT NULL")
    Long countCategories();
    
    @Select("SELECT c.name as category, COUNT(v.id) as count FROM video v LEFT JOIN category c ON v.category_id = c.id GROUP BY v.category_id, c.name ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> countByCategory(@Param("limit") int limit);
    
    @Select("SELECT COUNT(*) FROM video WHERE user_id = #{userId}")
    Long countByUserId(@Param("userId") Long userId);
    
    @Select("SELECT COALESCE(SUM(view_count), 0) FROM video WHERE user_id = #{userId}")
    Long sumViewCountByUserId(@Param("userId") Long userId);
    
    @Select("SELECT DATE(create_time) as date, COUNT(*) as count, COALESCE(SUM(view_count), 0) as views FROM video WHERE user_id = #{userId} AND create_time >= #{startDate} GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> getVideoTrendByUserId(@Param("userId") Long userId, @Param("startDate") String startDate);
    
    @Select("SELECT v.id, v.title, v.view_count, v.create_time FROM video v WHERE v.user_id = #{userId} ORDER BY v.view_count DESC LIMIT #{limit}")
    List<Map<String, Object>> getTopVideosByViews(@Param("userId") Long userId, @Param("limit") int limit);
    
    @Select("SELECT c.name as category, COUNT(v.id) as count FROM video v LEFT JOIN category c ON v.category_id = c.id WHERE v.user_id = #{userId} GROUP BY v.category_id, c.name ORDER BY count DESC")
    List<Map<String, Object>> getCategoryDistributionByUserId(@Param("userId") Long userId);
}
