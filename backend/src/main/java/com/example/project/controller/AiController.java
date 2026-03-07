package com.example.project.controller;

import com.example.project.common.Result;
import com.example.project.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, String> params) {
        String userText = params.get("text");
        try {
            String reply = aiService.chat(userText);
            return Result.success(reply);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "AI服务暂时不可用: " + e.getMessage());
        }
    }
}
