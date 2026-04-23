package com.example.project.dto;

import lombok.Data;

/**
 * 帖子搜索请求DTO
 * <p>
 * 封装帖子搜索接口的请求参数，支持多条件筛选和分页
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
public class PostSearchRequest {

    /** 关键词搜索条件 */
    private String keyword;

    /** 分类筛选条件 */
    private String category;

    /** 地点筛选条件 */
    private String location;

    /** 学校筛选条件 */
    private String school;

    /** 是否已认证筛选 */
    private Boolean verified;

    /** 性别筛选 */
    private Integer gender;

    /** 目的地筛选 */
    private String destination;

    /** 页码，默认为1 */
    private Integer pageNum = 1;

    /** 每页大小，默认为10 */
    private Integer pageSize = 10;

    /** 排序字段：createTime-创建时间, updateTime-更新时间, likeCount-点赞数, commentCount-评论数 */
    private String sortField = "createTime";

    /** 排序方式：asc-升序, desc-降序 */
    private String sortOrder = "desc";

    /**
     * 判断是否包含用户相关筛选条件
     *
     * @return 包含用户筛选条件返回true，否则返回false
     */
    public boolean hasUserFilters() {
        return location != null || school != null || verified != null || gender != null;
    }

    /**
     * 判断是否包含搜索关键词
     *
     * @return 包含关键词返回true，否则返回false
     */
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
}
