package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Proxy;
import com.example.backend.mapper.ProxyMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProxyService extends ServiceImpl<ProxyMapper, Proxy> {
    
    public List<Proxy> getActiveProxies() {
        QueryWrapper<Proxy> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        return list(queryWrapper);
    }
    
    public List<Proxy> getAllProxies() {
        return list();
    }
    
    public boolean addProxy(Proxy proxy) {
        QueryWrapper<Proxy> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("host", proxy.getHost());
        queryWrapper.eq("port", proxy.getPort());
        if (count(queryWrapper) > 0) {
            return false;
        }
        proxy.setStatus(1);
        proxy.setSuccessCount(0);
        proxy.setFailCount(0);
        return save(proxy);
    }
    
    public boolean updateProxy(Proxy proxy) {
        return updateById(proxy);
    }
    
    public boolean deleteProxy(Integer id) {
        return removeById(id);
    }
    
    public boolean toggleStatus(Integer id) {
        Proxy proxy = getById(id);
        if (proxy == null) {
            return false;
        }
        proxy.setStatus(proxy.getStatus() == 1 ? 0 : 1);
        return updateById(proxy);
    }
    
    public void recordSuccess(Integer id) {
        Proxy proxy = getById(id);
        if (proxy != null) {
            proxy.setSuccessCount(proxy.getSuccessCount() + 1);
            proxy.setLastUsedAt(LocalDateTime.now());
            updateById(proxy);
        }
    }
    
    public void recordFail(Integer id) {
        Proxy proxy = getById(id);
        if (proxy != null) {
            proxy.setFailCount(proxy.getFailCount() + 1);
            proxy.setLastUsedAt(LocalDateTime.now());
            updateById(proxy);
        }
    }
}
