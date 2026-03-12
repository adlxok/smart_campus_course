package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.Tag;
import com.example.backend.entity.VideoTag;
import com.example.backend.mapper.TagMapper;
import com.example.backend.mapper.VideoTagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tag")
public class TagController {
    
    @Autowired
    private TagMapper tagMapper;
    
    @Autowired
    private VideoTagMapper videoTagMapper;
    
    @GetMapping("/list")
    public Map<String, Object> getAllTags() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("create_time");
            List<Tag> tags = tagMapper.selectList(queryWrapper);
            
            response.put("success", true);
            response.put("data", tags);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取标签失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/video/{videoId}")
    public Map<String, Object> getVideoTags(@PathVariable Long videoId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Tag> tags = videoTagMapper.selectTagsByVideoId(videoId);
            
            List<Map<String, Object>> tagList = new ArrayList<>();
            for (Tag tag : tags) {
                Map<String, Object> tagMap = new HashMap<>();
                tagMap.put("id", tag.getId());
                tagMap.put("name", tag.getName());
                tagList.add(tagMap);
            }
            
            response.put("success", true);
            response.put("data", tagList);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取视频标签失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @PostMapping("/create")
    public Map<String, Object> createTag(@RequestParam String name) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name", name);
            Tag existingTag = tagMapper.selectOne(queryWrapper);
            
            if (existingTag != null) {
                response.put("success", true);
                response.put("data", existingTag);
                return response;
            }
            
            Tag tag = new Tag(name);
            tagMapper.insert(tag);
            
            response.put("success", true);
            response.put("data", tag);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "创建标签失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @PostMapping("/video/{videoId}/tags")
    public Map<String, Object> setVideoTags(@PathVariable Long videoId, @RequestBody List<String> tagNames) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            QueryWrapper<VideoTag> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("video_id", videoId);
            videoTagMapper.delete(deleteWrapper);
            
            for (String tagName : tagNames) {
                if (tagName == null || tagName.trim().isEmpty()) {
                    continue;
                }
                
                QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
                tagQueryWrapper.eq("name", tagName.trim());
                Tag tag = tagMapper.selectOne(tagQueryWrapper);
                
                if (tag == null) {
                    tag = new Tag(tagName.trim());
                    tagMapper.insert(tag);
                }
                
                VideoTag videoTag = new VideoTag(videoId, tag.getId());
                videoTagMapper.insert(videoTag);
            }
            
            response.put("success", true);
            response.put("message", "标签设置成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "设置标签失败: " + e.getMessage());
        }
        
        return response;
    }
}
