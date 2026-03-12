CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '接收通知的用户ID',
    type VARCHAR(50) NOT NULL COMMENT '通知类型: FAVORITE, LIKE, COMMENT, SYSTEM等',
    title VARCHAR(100) NOT NULL COMMENT '通知标题',
    content VARCHAR(500) NOT NULL COMMENT '通知内容',
    related_id BIGINT COMMENT '关联的视频ID',
    from_user_id BIGINT COMMENT '触发通知的用户ID',
    is_read TINYINT(1) DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';
