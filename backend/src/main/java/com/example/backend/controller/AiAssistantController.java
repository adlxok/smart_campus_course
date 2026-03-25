package com.example.backend.controller;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-assistant")
public class AiAssistantController {

    private static final String API_KEY = "6a73e3bf68564df08a43178e932fe65f.WAnwPkDF04sTeZ3q";
    private static final String MODEL = "glm-4-flash";

    private static final String SYSTEM_PROMPT = "你是\"智慧学堂\"视频学习平台的智能小助手，名叫小智。你的职责是为用户提供友好的帮助和服务。\n\n"
            + "你可以帮助用户：\n"
            + "1. 推荐和搜索视频内容\n"
            + "2. 解答关于平台使用的问题\n"
            + "3. 提供学习建议和指导\n"
            + "4. 聊天互动，提供愉快的对话体验\n\n"
            + "注意事项：\n"
            + "- 回答要简洁友好，适合学生用户群体\n"
            + "- 如果不了解具体视频内容，可以引导用户去首页搜索或查看推荐\n"
            + "- 保持专业但亲切的语气\n"
            + "- 用中文回答问题";

    @SuppressWarnings("unchecked")
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            String userMessage = (String) params.get("message");
            if (userMessage == null || userMessage.trim().isEmpty()) {
                result.put("success", false);
                result.put("error", "消息不能为空");
                return result;
            }

            List<Map<String, String>> history = (List<Map<String, String>>) params.get("history");
            if (history == null) {
                history = new ArrayList<>();
            }

            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.builder()
                    .role("system")
                    .content(SYSTEM_PROMPT)
                    .build());
            for (Map<String, String> msg : history) {
                String role = msg.get("role");
                String content = msg.get("content");
                if (role != null && content != null && (role.equals("user") || role.equals("assistant"))) {
                    messages.add(ChatMessage.builder().role(role).content(content).build());
                }
            }
            messages.add(ChatMessage.builder()
                    .role(ChatMessageRole.USER.value())
                    .content(userMessage)
                    .build());

            ZhipuAiClient client = ZhipuAiClient.builder()
                    .ofZHIPU()
                    .apiKey(API_KEY)
                    .build();

            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(MODEL)
                    .messages(messages)
                    .maxTokens(2048)
                    .temperature(0.7f)
                    .build();

            ChatCompletionResponse response = client.chat().createChatCompletion(request);

            if (response.isSuccess() && response.getData() != null
                    && response.getData().getChoices() != null
                    && !response.getData().getChoices().isEmpty()) {
                String content = String.valueOf(response.getData().getChoices().get(0).getMessage().getContent());
                result.put("success", true);
                result.put("content", content);
            } else {
                result.put("success", false);
                result.put("error", response.getMsg() != null ? response.getMsg() : "AI服务调用失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}
