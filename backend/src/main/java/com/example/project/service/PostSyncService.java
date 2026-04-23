package com.example.project.service;

import com.example.project.document.PostDocument;
import com.example.project.entity.Post;

import java.util.List;

/**
 * 帖子数据同步服务接口
 * <p>
 * 提供 MySQL 与 Elasticsearch 之间的数据同步功能
 * </p>
 *
 * @author system
 * @since 1.0
 */
public interface PostSyncService {

    /**
     * 同步单个帖子到 ES
     *
     * @param postId 帖子ID
     */
    void syncPost(Long postId);

    /**
     * 同步帖子到 ES
     *
     * @param post 帖子实体
     */
    void syncPost(Post post);

    /**
     * 批量同步帖子到 ES
     *
     * @param posts 帖子实体列表
     */
    void syncPosts(List<Post> posts);

    /**
     * 从 ES 删除帖子
     *
     * @param postId 帖子ID
     */
    void deletePostFromEs(Long postId);

    /**
     * 全量同步所有帖子到 ES
     */
    void syncAllPosts();

    /**
     * 将 Post 实体转换为 PostDocument
     *
     * @param post 帖子实体
     * @return ES 文档
     */
    PostDocument convertToDocument(Post post);
}
