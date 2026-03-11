package com.example.backend.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequestMapping("/api/avatar")
public class AvatarController {

    @GetMapping("/default")
    public ResponseEntity<Resource> getDefaultAvatar() {
        // 读取默认头像文件
        String defaultAvatarPath = "d:/A_Graduation_Project/project/p2_0/backend/image/default_image/defaultImage.png";
        File file = new File(defaultAvatarPath);
        Resource resource = new FileSystemResource(file);

        // 返回图片文件
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }
}
