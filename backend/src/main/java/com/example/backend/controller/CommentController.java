package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.Comment;
import com.example.backend.entity.User;
import com.example.backend.mapper.CommentMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.TextClassificationService;
import com.example.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
public class CommentController {
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TextClassificationService textClassificationService;
    
    @GetMapping("/list/{videoId}")
    public Map<String, Object> getComments(@PathVariable Long videoId) {
        Map<String, Object> response = new HashMap<>();
        
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("video_id", videoId);
        queryWrapper.orderByDesc("create_time");
        
        List<Comment> comments = commentMapper.selectList(queryWrapper);
        
        for (Comment comment : comments) {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                comment.setAvatar(user.getAvatar());
            }
        }
        
        response.put("success", true);
        response.put("data", comments);
        response.put("total", comments.size());
        
        return response;
    }
    
    @GetMapping("/replies/{commentId}")
    public Map<String, Object> getReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize) {
        Map<String, Object> response = new HashMap<>();
        
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", commentId);
        queryWrapper.orderByAsc("create_time");
        
        Long total = commentMapper.selectCount(queryWrapper);
        
        int offset = (pageNum - 1) * pageSize;
        queryWrapper.last("LIMIT " + offset + ", " + pageSize);
        
        List<Comment> replies = commentMapper.selectList(queryWrapper);
        
        for (Comment reply : replies) {
            User user = userMapper.selectById(reply.getUserId());
            if (user != null) {
                reply.setAvatar(user.getAvatar());
            }
        }
        
        response.put("success", true);
        response.put("data", replies);
        response.put("total", total);
        response.put("pageNum", pageNum);
        response.put("pageSize", pageSize);
        
        return response;
    }
    
    @PostMapping("/add")
    public Map<String, Object> addComment(@RequestBody Map<String, Object> params,
                                           @RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            Long videoId = Long.valueOf(params.get("videoId").toString());
            String content = params.get("content").toString();
            Long parentId = params.get("parentId") != null ? Long.valueOf(params.get("parentId").toString()) : null;
            String replyToUsername = params.get("replyToUsername") != null ? params.get("replyToUsername").toString() : null;
            
            if (content == null || content.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "评论内容不能为空");
                return response;
            }
            
            TextClassificationService.TextClassificationResult classificationResult = 
                textClassificationService.classifyText(content);
            
            if (!classificationResult.isSuccess()) {
                response.put("success", false);
                response.put("message", "内容审核服务暂时不可用，请稍后重试");
                return response;
            }
            
            if (classificationResult.isViolation()) {
                response.put("success", false);
                response.put("message", "评论内容涉嫌违规，无法发布");
                response.put("violationProbability", classificationResult.getViolationProbability());
                return response;
            }
            
            Comment comment;
            if (parentId != null) {
                comment = new Comment(videoId, user.getId(), username, content, parentId, replyToUsername);
            } else {
                comment = new Comment(videoId, user.getId(), username, content);
            }
            comment.setAvatar(user.getAvatar());
            commentMapper.insert(comment);
            
            response.put("success", true);
            response.put("message", "评论成功");
            response.put("data", comment);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "评论失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteComment(@PathVariable Long id,
                                              @RequestHeader("Authorization") String authorization) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authorization.substring(7);
            String username = JwtUtil.getUsernameFromToken(token);
            
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            Comment comment = commentMapper.selectById(id);
            if (comment == null) {
                response.put("success", false);
                response.put("message", "评论不存在");
                return response;
            }
            
            if (!comment.getUserId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "无权限删除此评论");
                return response;
            }
            
            deleteCommentAndReplies(id);
            
            response.put("success", true);
            response.put("message", "删除成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
        }
        
        return response;
    }
    
    private void deleteCommentAndReplies(Long commentId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", commentId);
        List<Comment> replies = commentMapper.selectList(queryWrapper);
        
        for (Comment reply : replies) {
            deleteCommentAndReplies(reply.getId());
        }
        
        commentMapper.deleteById(commentId);
    }
}
