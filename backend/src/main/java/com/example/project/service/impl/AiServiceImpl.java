package com.example.project.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.project.service.AiService;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class AiServiceImpl implements AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceImpl.class);

    @Value("${aliyun.ai.api-key}")
    private String apiKey;

    @Value("${aliyun.ai.api-url}")
    private String apiUrl;

    @Value("${aliyun.ai.model}")
    private String model;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    @Override
    public String chat(String userMessage) throws IOException {
        logger.info("Received AI chat request: {}", userMessage);
        
        // 1. 构建请求体
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("model", model);
        
        List<Map<String, String>> messages = new ArrayList<>();
        // 系统预设
        messages.add(Map.of("role", "system", "content", "你是一个大学生活动策划助手，可以帮助用户规划游戏组队、漫展、旅游、自习、运动等各种校园生活活动。你的回答应该热情、富有创意且贴近大学生生活。"));
        // 用户提问
        messages.add(Map.of("role", "user", "content", userMessage));
        
        bodyMap.put("messages", messages);

        String jsonBody = JSON.toJSONString(bodyMap);
        logger.debug("Sending request to AI API: {}", jsonBody);

        // 2. 发起请求
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        // 3. 解析响应
        try (Response response = client.newCall(request).execute()) {
            String responseStr = response.body().string();
            logger.debug("Received response from AI API: {}", responseStr);
            
            if (!response.isSuccessful()) {
                logger.error("AI API request failed with status {}: {}", response.code(), responseStr);
                throw new IOException("Unexpected code " + response);
            }
            
            JSONObject jsonResponse = JSON.parseObject(responseStr);
            
            // 提取回复内容
            if (jsonResponse.containsKey("choices") && !jsonResponse.getJSONArray("choices").isEmpty()) {
                String aiResponse = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
                logger.info("AI response generated successfully");
                return aiResponse;
            }
            logger.warn("AI API returned unexpected response format: {}", responseStr);
            return "抱歉，我暂时无法回答这个问题。";
        } catch (Exception e) {
            logger.error("Error occurred during AI chat processing", e);
            throw e;
        }
    }
}
