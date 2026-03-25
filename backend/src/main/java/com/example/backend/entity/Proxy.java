package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("proxy")
public class Proxy {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String protocol;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private Integer status;
    private LocalDateTime lastUsedAt;
    private Integer successCount;
    private Integer failCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
