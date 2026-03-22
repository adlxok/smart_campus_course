package com.example.backend.controller;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FSDataInputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/video")
public class VideoProxyController {

    private FileSystem fileSystem;
    private static final String HDFS_NAMENODE = "hdfs://localhost:9000";

    private synchronized FileSystem getFileSystem() throws IOException {
        if (fileSystem == null) {
            Configuration conf = new Configuration();
            conf.set("fs.defaultFS", HDFS_NAMENODE);
            conf.set("dfs.client.use.datanode.hostname", "true");
            fileSystem = FileSystem.get(URI.create(HDFS_NAMENODE), conf);
        }
        return fileSystem;
    }

    @GetMapping("/proxy")
    public ResponseEntity<byte[]> proxyVideo(@RequestParam("url") String videoUrl) {
        try {
            String hdfsPath = videoUrl;
            
            if (videoUrl.startsWith("hdfs://localhost:9000")) {
                hdfsPath = videoUrl.replace("hdfs://localhost:9000", "");
            } else if (videoUrl.startsWith("hdfs://")) {
                hdfsPath = videoUrl.replaceFirst("hdfs://[^/]+", "");
            }
            
            if (!hdfsPath.startsWith("/")) {
                hdfsPath = "/" + hdfsPath;
            }
            
            FileSystem fs = getFileSystem();
            Path path = new Path(hdfsPath);
            
            if (!fs.exists(path)) {
                return ResponseEntity.notFound().build();
            }
            
            FSDataInputStream inputStream = fs.open(path);
            byte[] bytes = new byte[(int) fs.getFileStatus(path).getLen()];
            inputStream.readFully(bytes);
            inputStream.close();
            
            String contentType = getContentType(hdfsPath);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(bytes.length);
            headers.setCacheControl("max-age=86400");
            headers.set("Accept-Ranges", "bytes");
            
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private String getContentType(String path) {
        if (path.endsWith(".mp4")) {
            return "video/mp4";
        } else if (path.endsWith(".webm")) {
            return "video/webm";
        } else if (path.endsWith(".mkv")) {
            return "video/x-matroska";
        } else if (path.endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (path.endsWith(".mov")) {
            return "video/quicktime";
        } else if (path.endsWith(".flv")) {
            return "video/x-flv";
        }
        return "video/mp4";
    }
}
