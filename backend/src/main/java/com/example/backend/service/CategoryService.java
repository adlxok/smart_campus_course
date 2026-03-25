package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Category;
import com.example.backend.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    public List<Category> getAllCategories() {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort");
        return categoryMapper.selectList(queryWrapper);
    }
    
    public IPage<Category> getCategoryList(int page, int pageSize) {
        Page<Category> pageParam = new Page<>(page, pageSize);
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort");
        return categoryMapper.selectPage(pageParam, queryWrapper);
    }
    
    public Category getCategoryById(Long id) {
        return categoryMapper.selectById(id);
    }
    
    public boolean addCategory(Category category) {
        return categoryMapper.insert(category) > 0;
    }
    
    public boolean updateCategory(Category category) {
        return categoryMapper.updateById(category) > 0;
    }
    
    public boolean deleteCategory(Long id) {
        return categoryMapper.deleteById(id) > 0;
    }
    
    public boolean checkCodeExists(String code, Long excludeId) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }
        return categoryMapper.selectCount(queryWrapper) > 0;
    }
}
