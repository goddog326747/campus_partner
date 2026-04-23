package com.example.project.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分类列表响应VO
 * <p>
 * 封装帖子分类列表接口的响应数据
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryListVO {

    /** 分类列表 */
    private List<String> categories;

    /** 分类列表描述 */
    private String description;
}
