package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.Proxy;
import com.example.backend.service.ProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/proxy")
public class ProxyController {

    @Autowired
    private ProxyService proxyService;

    @GetMapping("/list")
    public Map<String, Object> getProxyList() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Proxy> proxies = proxyService.list();
            response.put("success", true);
            response.put("data", proxies);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取代理列表失败: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/add")
    public Map<String, Object> addProxy(@RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            String protocol = (String) params.getOrDefault("protocol", "http");
            String host = (String) params.get("host");
            Integer port = params.get("port") != null ? ((Number) params.get("port")).intValue() : null;
            String username = (String) params.get("username");
            String password = (String) params.get("password");

            if (host == null || host.isEmpty() || port == null) {
                response.put("success", false);
                response.put("message", "主机和端口不能为空");
                return response;
            }

            QueryWrapper<Proxy> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("host", host).eq("port", port);
            if (proxyService.count(queryWrapper) > 0) {
                response.put("success", false);
                response.put("message", "该代理已存在");
                return response;
            }

            Proxy proxy = new Proxy();
            proxy.setProtocol(protocol);
            proxy.setHost(host);
            proxy.setPort(port);
            proxy.setUsername(username);
            proxy.setPassword(password);
            proxy.setStatus(1);
            proxy.setSuccessCount(0);
            proxy.setFailCount(0);

            proxyService.save(proxy);
            response.put("success", true);
            response.put("message", "代理添加成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加代理失败: " + e.getMessage());
        }
        return response;
    }

    @PutMapping("/update/{id}")
    public Map<String, Object> updateProxy(@PathVariable Integer id, @RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            Proxy proxy = proxyService.getById(id);
            if (proxy == null) {
                response.put("success", false);
                response.put("message", "代理不存在");
                return response;
            }

            if (params.containsKey("protocol")) {
                proxy.setProtocol((String) params.get("protocol"));
            }
            if (params.containsKey("host")) {
                proxy.setHost((String) params.get("host"));
            }
            if (params.containsKey("port")) {
                proxy.setPort(((Number) params.get("port")).intValue());
            }
            if (params.containsKey("username")) {
                proxy.setUsername((String) params.get("username"));
            }
            if (params.containsKey("password")) {
                proxy.setPassword((String) params.get("password"));
            }
            if (params.containsKey("status")) {
                proxy.setStatus(((Number) params.get("status")).intValue());
            }

            proxyService.updateById(proxy);
            response.put("success", true);
            response.put("message", "代理更新成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新代理失败: " + e.getMessage());
        }
        return response;
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteProxy(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = proxyService.removeById(id);
            if (result) {
                response.put("success", true);
                response.put("message", "代理删除成功");
            } else {
                response.put("success", false);
                response.put("message", "代理不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除代理失败: " + e.getMessage());
        }
        return response;
    }

    @PutMapping("/toggle/{id}")
    public Map<String, Object> toggleProxy(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Proxy proxy = proxyService.getById(id);
            if (proxy == null) {
                response.put("success", false);
                response.put("message", "代理不存在");
                return response;
            }

            int newStatus = proxy.getStatus() == 1 ? 0 : 1;
            proxy.setStatus(newStatus);
            proxyService.updateById(proxy);

            response.put("success", true);
            response.put("message", "状态切换成功");
            response.put("newStatus", newStatus);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "切换状态失败: " + e.getMessage());
        }
        return response;
    }
}
