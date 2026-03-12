package com.example.backend.kafka;

import com.example.backend.dto.NotificationMessage;
import com.example.backend.entity.Notification;
import com.example.backend.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {
    
    @Autowired
    private NotificationMapper notificationMapper;
    
    @KafkaListener(topics = "notification-topic", groupId = "notification-group")
    public void consumeNotification(NotificationMessage message) {
        Notification notification = new Notification();
        notification.setUserId(message.getUserId());
        notification.setType(message.getType());
        notification.setTitle(message.getTitle());
        notification.setContent(message.getContent());
        notification.setRelatedId(message.getRelatedId());
        notification.setFromUserId(message.getFromUserId());
        notification.setIsRead(false);
        
        notificationMapper.insert(notification);
    }
}
