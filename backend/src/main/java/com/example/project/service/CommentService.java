package com.example.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.project.entity.Comment;

import java.util.List;

/**
 * 评论服务接口
 * <p>
 * 提供评论的增删查、点赞和回复等服务，继承MyBatis-Plus的IService接口
 * </p>
 *
 * @author system
 * @since 1.0
 */
public interface CommentService extends IService<Comment> {

    /**
     * 获取帖子的评论列表（分页，包含用户信息）
     *
     * @param postId   帖子ID
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页大小
     * @return 评论分页列表，包含用户详情
     */
    Page<Comment> listCommentsByPost(Long postId, Integer pageNum, Integer pageSize);

    /**
     * 创建新评论
     *
     * @param comment 评论对象
     * @return 创建成功返回true，否则返回false
     */
    boolean createComment(Comment comment);

    /**
     * 删除评论
     * <p>
     * 仅评论所有者或管理员可以删除
     * </p>
     *
     * @param commentId 评论ID
     * @return 删除成功返回true，否则返回false
     */
    boolean deleteComment(Long commentId);

    /**
     * 点赞/取消点赞评论（切换状态）
     *
     * @param commentId 评论ID
     * @return 更新后的点赞数量
     */
    Integer toggleLike(Long commentId);

    /**
     * 获取指定评论的回复列表
     *
     * @param parentCommentId 父评论ID
     * @return 回复列表
     */
    List<Comment> listReplies(Long parentCommentId);
}