# Java Spring Boot 集成通义千问 (Qwen) 指南

目前前端直接调用的是 **qwen-plus** 模型。

如果想将 AI 调用逻辑迁移到 Java 后端（更安全，Key 不会泄露），请按照以下步骤操作。

## 1. 引入依赖 (pom.xml)

推荐使用阿里云官方 SDK，或者直接用 OkHttp 调用 REST API。这里推荐 **Spring AI** (新趋势) 或 **OkHttp** (最简单)。

**方案一：使用 OkHttp (轻量级，推荐)**

```xml
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>2.0.32</version>
</dependency>
```

## 2. 编写 Service

创建一个 `AiService.java` 来封装调用逻辑。

```java
package com.example.project.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AiService {

    private static final String API_KEY = "sk-c0efcf97c9a041ab8f9d998012ad1bc9"; // 建议放入 application.yml
    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    public String chat(String userMessage) throws IOException {
        // 1. 构建请求体
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("model", "qwen-plus");
        
        List<Map<String, String>> messages = new ArrayList<>();
        // 系统预设
        messages.add(Map.of("role", "system", "content", "你是一个乐于助人的旅行搭子AI助手，回答热情简短。"));
        // 用户提问
        messages.add(Map.of("role", "user", "content", userMessage));
        
        bodyMap.put("messages", messages);

        String jsonBody = JSON.toJSONString(bodyMap);

        // 2. 发起请求
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        // 3. 解析响应
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            
            String responseStr = response.body().string();
            JSONObject jsonResponse = JSON.parseObject(responseStr);
            
            // 提取回复内容
            return jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
    }
}
```

## 3. 编写 Controller

创建一个接口供前端调用。

```java
package com.example.project.controller;

import com.example.project.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> params) {
        String userText = params.get("text");
        try {
            String reply = aiService.chat(userText);
            return Map.of("code", 200, "data", reply);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("code", 500, "msg", "AI服务暂时不可用");
        }
    }
}
```

## 4. 前端对接修改

当后端写好后，你需要修改前端的 `AIAssistant.vue`，将原来的直接 fetch 改为调用你的后端接口。

**修改前 (直接调阿里云):**
```javascript
const response = await fetch('https://dashscope.aliyuncs.com/...', { ... })
```

**修改后 (调你的 Java 后端):**
```javascript
import http from '../api/http' // 使用封装好的 http

const fetchQwenResponse = async (text) => {
  // ...
  try {
    // 调用自己的后端接口
    const res = await http.post('/ai/chat', { text: text })
    
    if (res.code === 200) {
       messages.value.push({ role: 'ai', content: res.data })
    }
  } catch (e) {
    // ...
  }
}
```
