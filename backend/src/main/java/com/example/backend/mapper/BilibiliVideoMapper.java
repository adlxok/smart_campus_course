package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.BilibiliVideo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface BilibiliVideoMapper extends BaseMapper<BilibiliVideo> {
    
    @Select("SELECT COUNT(*) as count FROM bilibili_video")
    Long countAll();
    
    @Select("SELECT COALESCE(SUM(view_count), 0) as total FROM bilibili_video")
    Long sumViewCount();
    
    @Select("SELECT COALESCE(SUM(like_count), 0) as total FROM bilibili_video")
    Long sumLikeCount();
    
    @Select("SELECT category, COUNT(*) as count FROM bilibili_video GROUP BY category ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> countByCategory(@Param("limit") int limit);
    
    @Select("SELECT * FROM bilibili_video WHERE title LIKE CONCAT('%', #{keyword}, '%') OR bvid LIKE CONCAT('%', #{keyword}, '%') ORDER BY create_time DESC")
    IPage<BilibiliVideo> searchByKeyword(Page<BilibiliVideo> page, @Param("keyword") String keyword);
}
