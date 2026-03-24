package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Banner;
import com.example.backend.mapper.BannerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/banner")
public class BannerController {
    
    @Autowired
    private BannerMapper bannerMapper;
    
    private static final String UPLOAD_BASE_PATH = "D:/A_Graduation_Project/project/p2_0/uploads";
    
    @GetMapping("/list")
    public Map<String, Object> getActiveBanners() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Banner> banners = bannerMapper.selectActiveBanners();
            response.put("success", true);
            response.put("data", banners);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取轮播图失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/admin/list")
    public Map<String, Object> getBannerList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<Banner> pageParam = new Page<>(page, pageSize);
            QueryWrapper<Banner> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByAsc("sort_order").orderByDesc("create_time");
            
            IPage<Banner> pageResult = bannerMapper.selectPage(pageParam, queryWrapper);
            
            response.put("success", true);
            response.put("data", pageResult.getRecords());
            response.put("total", pageResult.getTotal());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取轮播图列表失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/admin/add")
    public Map<String, Object> addBanner(@RequestBody Banner banner) {
        Map<String, Object> response = new HashMap<>();
        try {
            banner.setCreateTime(new Date());
            banner.setUpdateTime(new Date());
            if (banner.getStatus() == null) {
                banner.setStatus(1);
            }
            if (banner.getSortOrder() == null) {
                banner.setSortOrder(0);
            }
            
            bannerMapper.insert(banner);
            
            response.put("success", true);
            response.put("message", "添加轮播图成功");
            response.put("data", banner);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加轮播图失败: " + e.getMessage());
        }
        return response;
    }
    
    @PutMapping("/admin/update")
    public Map<String, Object> updateBanner(@RequestBody Banner banner) {
        Map<String, Object> response = new HashMap<>();
        try {
            banner.setUpdateTime(new Date());
            bannerMapper.updateById(banner);
            
            response.put("success", true);
            response.put("message", "更新轮播图成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新轮播图失败: " + e.getMessage());
        }
        return response;
    }
    
    @DeleteMapping("/admin/delete/{id}")
    public Map<String, Object> deleteBanner(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            bannerMapper.deleteById(id);
            
            response.put("success", true);
            response.put("message", "删除轮播图成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除轮播图失败: " + e.getMessage());
        }
        return response;
    }
    
    @PutMapping("/admin/toggle/{id}")
    public Map<String, Object> toggleBannerStatus(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Banner banner = bannerMapper.selectById(id);
            if (banner == null) {
                response.put("success", false);
                response.put("message", "轮播图不存在");
                return response;
            }
            
            banner.setStatus(banner.getStatus() == 1 ? 0 : 1);
            banner.setUpdateTime(new Date());
            bannerMapper.updateById(banner);
            
            response.put("success", true);
            response.put("message", banner.getStatus() == 1 ? "已启用" : "已禁用");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/admin/upload")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "请选择文件");
                return response;
            }
            
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
            String filename = System.currentTimeMillis() + extension;
            
            File bannerDir = new File(UPLOAD_BASE_PATH + "/banners");
            if (!bannerDir.exists()) {
                bannerDir.mkdirs();
            }
            
            File destFile = new File(bannerDir.getAbsolutePath() + "/" + filename);
            file.transferTo(destFile);
            
            String imageUrl = "/uploads/banners/" + filename;
            
            response.put("success", true);
            response.put("url", imageUrl);
            response.put("message", "上传成功");
        } catch (IOException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "上传失败: " + e.getMessage());
        }
        return response;
    }
}
