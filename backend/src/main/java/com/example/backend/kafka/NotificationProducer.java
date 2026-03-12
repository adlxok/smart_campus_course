package com.example.backend.kafka;

import com.example.backend.dto.NotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationProducer {
    
    private static final String TOPIC = "notification-topic";
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public void sendNotification(NotificationMessage message) {
        kafkaTemplate.send(TOPIC, message);
    }
}
