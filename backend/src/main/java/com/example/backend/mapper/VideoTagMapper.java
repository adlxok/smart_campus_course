package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.VideoTag;
import com.example.backend.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VideoTagMapper extends BaseMapper<VideoTag> {
    
    @Select("SELECT t.id, t.name, t.create_time FROM tag t " +
            "INNER JOIN video_tag vt ON t.id = vt.tag_id " +
            "WHERE vt.video_id = #{videoId}")
    List<Tag> selectTagsByVideoId(@Param("videoId") Long videoId);
}
