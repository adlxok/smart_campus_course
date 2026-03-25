package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

@TableName("bilibili_video")
public class BilibiliVideo {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("bvid")
    private String bvid;
    
    @TableField("title")
    private String title;
    
    @TableField("tags")
    private String tags;
    
    @TableField("category")
    private String category;
    
    @TableField("cover")
    private String cover;
    
    @TableField("video_url")
    private String videoUrl;
    
    @TableField("audio_url")
    private String audioUrl;
    
    @TableField("view_count")
    private Integer viewCount;
    
    @TableField("danmaku_count")
    private Integer danmakuCount;
    
    @TableField("like_count")
    private Integer likeCount;
    
    @TableField("coin_count")
    private Integer coinCount;
    
    @TableField("favorite_count")
    private Integer favoriteCount;
    
    @TableField("share_count")
    private Integer shareCount;
    
    @TableField("reply_count")
    private Integer replyCount;
    
    @TableField("create_time")
    private Date createTime;
    
    @TableField("update_time")
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBvid() {
        return bvid;
    }

    public void setBvid(String bvid) {
        this.bvid = bvid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getDanmakuCount() {
        return danmakuCount;
    }

    public void setDanmakuCount(Integer danmakuCount) {
        this.danmakuCount = danmakuCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getCoinCount() {
        return coinCount;
    }

    public void setCoinCount(Integer coinCount) {
        this.coinCount = coinCount;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Integer getShareCount() {
        return shareCount;
    }

    public void setShareCount(Integer shareCount) {
        this.shareCount = shareCount;
    }

    public Integer getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
