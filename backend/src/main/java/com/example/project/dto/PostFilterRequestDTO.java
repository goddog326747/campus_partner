package com.example.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 帖子筛选请求DTO
 * <p>
 * 封装帖子列表高级筛选接口的请求参数
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostFilterRequestDTO {

    /** 分类筛选条件 */
    private String category;

    /** 关键词搜索条件 */
    private String keyword;

    /** 地点筛选条件 */
    private String location;

    /** 学校筛选条件 */
    private String school;

    /** 是否已认证筛选 */
    private Boolean verified;

    /** 性别筛选 */
    private Integer gender;

    /** 页码，默认为1 */
    @Builder.Default
    private int pageNum = 1;

    /** 每页大小，默认为10 */
    @Builder.Default
    private int pageSize = 10;
}
