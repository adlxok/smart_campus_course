package com.example.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TextClassificationService {
    
    @Value("${ml.service.url:http://127.0.0.1:5001}")
    private String mlServiceUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public TextClassificationResult classifyText(String text) {
        try {
            String url = mlServiceUrl + "/api/predict";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("text", text);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data");
            
            boolean isViolation = data.get("isViolation").asBoolean();
            String label = data.get("label").asText();
            double violationProbability = data.get("violationProbability").asDouble();
            double confidence = data.get("confidence").asDouble();
            
            return new TextClassificationResult(true, isViolation, label, violationProbability, confidence, null);
            
        } catch (Exception e) {
            return new TextClassificationResult(false, false, "检测失败", 0.0, 0.0, "文本分类服务调用失败: " + e.getMessage());
        }
    }
    
    public static class TextClassificationResult {
        private final boolean success;
        private final boolean isViolation;
        private final String label;
        private final double violationProbability;
        private final double confidence;
        private final String errorMessage;
        
        public TextClassificationResult(boolean success, boolean isViolation, String label, 
                                        double violationProbability, double confidence, String errorMessage) {
            this.success = success;
            this.isViolation = isViolation;
            this.label = label;
            this.violationProbability = violationProbability;
            this.confidence = confidence;
            this.errorMessage = errorMessage;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public boolean isViolation() {
            return isViolation;
        }
        
        public String getLabel() {
            return label;
        }
        
        public double getViolationProbability() {
            return violationProbability;
        }
        
        public double getConfidence() {
            return confidence;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
