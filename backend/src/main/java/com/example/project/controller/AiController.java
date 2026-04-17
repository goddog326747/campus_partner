package com.example.project.controller;

import com.example.project.common.Result;
import com.example.project.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI对话控制器
 * <p>
 * 提供AI对话功能的API接口，支持与AI进行实时对话交互
 * </p>
 *
 * @author system
 * @since 1.0
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    /**
     * 与AI进行对话
     *
     * @param params 对话参数，包含text字段（用户输入的文本）
     * @return AI的回复内容
     */
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
