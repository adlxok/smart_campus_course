package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {
    
    @Select("SELECT COUNT(*) FROM user_follow WHERE following_id = #{userId}")
    Long countFollowers(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{userId}")
    Long countFollowings(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) > 0 FROM user_follow WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    boolean existsByFollowerIdAndFollowingId(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
}
