package com.example.project.service;

import java.io.IOException;

/**
 * AI对话服务接口
 * <p>
 * 提供AI对话功能的服务接口，支持与AI进行实时对话交互
 * </p>
 *
 * @author system
 * @since 1.0
 */
public interface AiService {
    
    /**
     * 与AI进行对话
     *
     * @param userMessage 用户输入的消息内容
     * @return AI的回复内容
     * @throws IOException 当AI服务调用失败时抛出
     */
    String chat(String userMessage) throws IOException;
}
