package com.example.project.service;

import com.example.project.dto.PostQueryRequest;
import com.example.project.entity.Post;

import java.util.List;

/**
 * 帖子查询服务接口
 * <p>
 * 提供帖子查询相关的服务，支持分页查询和条件筛选
 * </p>
 *
 * @author system
 * @since 1.0
 */
public interface PostQueryService {
    
    /**
     * 根据查询条件获取帖子列表（分页）
     *
     * @param request 查询请求对象，包含各种筛选条件
     * @return 分页帖子列表
     */
    List<Post> queryPosts(PostQueryRequest request);
    
    /**
     * 获取帖子列表（简单查询）
     *
     * @param category 分类筛选条件，可为null
     * @param keyword  关键词搜索条件，可为null
     * @return 帖子列表
     */
    List<Post> listPosts(String category, String keyword);
    
    /**
     * 根据ID获取帖子详情
     *
     * @param id 帖子ID
     * @return 帖子对象，不存在则返回null
     */
    Post getPostById(Long id);
    
    /**
     * 获取指定用户发布的帖子列表
     *
     * @param userId 用户ID
     * @return 该用户发布的帖子列表
     */
    List<Post> getPostsByUserId(Long userId);
}
