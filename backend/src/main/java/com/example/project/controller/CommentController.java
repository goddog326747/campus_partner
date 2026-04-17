package com.example.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.project.common.Result;
import com.example.project.entity.Comment;
import com.example.project.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 * <p>
 * 提供评论相关的API接口，包括评论的增删查、点赞和回复等功能
 * </p>
 *
 * @author system
 * @since 1.0
 */
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    private CommentService commentService;

    /**
     * 获取帖子的评论列表（分页）
     *
     * @param postId   帖子ID
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页大小
     * @return 评论分页列表
     */
    @GetMapping("/list")
    public Result<Page<Comment>> listComments(
            @RequestParam Long postId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        logger.info("Fetching comments for postId: {}, page: {}, size: {}", postId, pageNum, pageSize);
        try {
            Page<Comment> page = commentService.listCommentsByPost(postId, pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            logger.error("Error fetching comments for postId: {}", postId, e);
            return Result.error(500, "获取评论列表失败");
        }
    }

    /**
     * 创建新评论
     *
     * @param comment 评论对象，包含评论内容、帖子ID等信息
     * @return 操作结果
     */
    @PostMapping("/create")
    public Result<String> createComment(@RequestBody Comment comment) {
        logger.info("Creating comment for postId: {}", comment.getPostId());
        try {
            boolean success = commentService.createComment(comment);
            if (success) {
                logger.info("Successfully created comment with ID: {}", comment.getId());
                return Result.success("评论成功");
            } else {
                return Result.error(500, "评论失败");
            }
        } catch (Exception e) {
            logger.error("Error creating comment", e);
            return Result.error(500, "评论失败: " + e.getMessage());
        }
    }

    /**
     * 删除评论
     * <p>
     * 仅评论所有者或管理员可以删除评论
     * </p>
     *
     * @param commentId 评论ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{commentId}")
    public Result<String> deleteComment(@PathVariable Long commentId) {
        logger.info("Deleting comment: {}", commentId);
        try {
            boolean success = commentService.deleteComment(commentId);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error(500, "删除失败");
            }
        } catch (Exception e) {
            logger.error("Error deleting comment: {}", commentId, e);
            return Result.error(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 点赞/取消点赞评论
     * <p>
     * 切换评论的点赞状态，已点赞则取消，未点赞则点赞
     * </p>
     *
     * @param commentId 评论ID
     * @return 更新后的点赞数量
     */
    @PostMapping("/like/{commentId}")
    public Result<Integer> toggleLike(@PathVariable Long commentId) {
        logger.info("Toggling like for comment: {}", commentId);
        try {
            Integer likeCount = commentService.toggleLike(commentId);
            return Result.success(likeCount);
        } catch (Exception e) {
            logger.error("Error toggling like for comment: {}", commentId, e);
            return Result.error(500, "操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取评论的回复列表
     *
     * @param parentCommentId 父评论ID
     * @return 回复列表
     */
    @GetMapping("/replies/{parentCommentId}")
    public Result<List<Comment>> listReplies(@PathVariable Long parentCommentId) {
        logger.info("Fetching replies for comment: {}", parentCommentId);
        try {
            List<Comment> replies = commentService.listReplies(parentCommentId);
            return Result.success(replies);
        } catch (Exception e) {
            logger.error("Error fetching replies for comment: {}", parentCommentId, e);
            return Result.error(500, "获取回复失败");
        }
    }
}