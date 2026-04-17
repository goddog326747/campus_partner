package com.example.project.dto;

import lombok.Data;

import java.util.List;

/**
 * 帖子创建请求DTO
 * <p>
 * 封装帖子创建接口的请求参数
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
public class PostCreateRequest {
    
    /** 帖子标题 */
    private String title;
    /** 帖子内容 */
    private String content;
    /** 帖子分类 */
    private String category;
    /** 目的地 */
    private String destination;
    /** 图片URL列表 */
    private List<String> images;
}
