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
}
