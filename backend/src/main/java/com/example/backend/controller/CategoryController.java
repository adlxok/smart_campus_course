package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.annotation.RequirePermission;
import com.example.backend.entity.Category;
import com.example.backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/category")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    @GetMapping("/list")
    @RequirePermission("system:category:manage")
    public Map<String, Object> getCategoryList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> response = new HashMap<>();
        try {
            IPage<Category> pageResult = categoryService.getCategoryList(page, pageSize);
            response.put("success", true);
            response.put("data", pageResult.getRecords());
            response.put("total", pageResult.getTotal());
            response.put("page", page);
            response.put("pageSize", pageSize);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取分类列表失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/all")
    public Map<String, Object> getAllCategories() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Category> categories = categoryService.getAllCategories();
            response.put("success", true);
            response.put("data", categories);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取分类列表失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/{id}")
    public Map<String, Object> getCategoryById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Category category = categoryService.getCategoryById(id);
            if (category != null) {
                response.put("success", true);
                response.put("data", category);
            } else {
                response.put("success", false);
                response.put("message", "分类不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取分类详情失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/add")
    @RequirePermission("system:category:manage")
    public Map<String, Object> addCategory(@RequestBody Category category) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (category.getName() == null || category.getName().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "分类名称不能为空");
                return response;
            }
            
            if (category.getCode() != null && !category.getCode().trim().isEmpty()) {
                if (categoryService.checkCodeExists(category.getCode(), null)) {
                    response.put("success", false);
                    response.put("message", "分类编码已存在");
                    return response;
                }
            }
            
            boolean result = categoryService.addCategory(category);
            if (result) {
                response.put("success", true);
                response.put("message", "添加成功");
            } else {
                response.put("success", false);
                response.put("message", "添加失败");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加分类失败: " + e.getMessage());
        }
        return response;
    }
    
    @PutMapping("/update")
    @RequirePermission("system:category:manage")
    public Map<String, Object> updateCategory(@RequestBody Category category) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (category.getId() == null) {
                response.put("success", false);
                response.put("message", "分类ID不能为空");
                return response;
            }
            
            if (category.getName() == null || category.getName().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "分类名称不能为空");
                return response;
            }
            
            if (category.getCode() != null && !category.getCode().trim().isEmpty()) {
                if (categoryService.checkCodeExists(category.getCode(), category.getId())) {
                    response.put("success", false);
                    response.put("message", "分类编码已存在");
                    return response;
                }
            }
            
            boolean result = categoryService.updateCategory(category);
            if (result) {
                response.put("success", true);
                response.put("message", "更新成功");
            } else {
                response.put("success", false);
                response.put("message", "更新失败，分类不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新分类失败: " + e.getMessage());
        }
        return response;
    }
    
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteCategory(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = categoryService.deleteCategory(id);
            if (result) {
                response.put("success", true);
                response.put("message", "删除成功");
            } else {
                response.put("success", false);
                response.put("message", "删除失败，分类不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除分类失败: " + e.getMessage());
        }
        return response;
    }
}
