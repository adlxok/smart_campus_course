package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BannerMapper extends BaseMapper<Banner> {
    
    @Select("SELECT * FROM banners WHERE status = 1 ORDER BY sort_order ASC, create_time DESC")
    List<Banner> selectActiveBanners();
    
    @Select("SELECT COUNT(*) FROM banners WHERE status = 1")
    Long countActive();
}
