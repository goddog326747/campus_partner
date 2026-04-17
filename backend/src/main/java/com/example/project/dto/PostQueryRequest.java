package com.example.project.dto;

import lombok.Data;

/**
 * 帖子查询请求DTO
 * <p>
 * 封装帖子查询接口的请求参数，支持多条件筛选和分页
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
public class PostQueryRequest {
    
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
    private Integer pageNum = 1;
    /** 每页大小，默认为10 */
    private Integer pageSize = 10;
    
    /**
     * 判断是否包含用户相关筛选条件
     *
     * @return 包含用户筛选条件返回true，否则返回false
     */
    public boolean hasUserFilters() {
        return location != null || school != null || verified != null || gender != null;
    }
}
