package com.example.project.controller;

import com.example.project.common.Result;
import com.example.project.dto.CommentQueryRequestDTO;
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
     * @param request 评论查询请求DTO，包含帖子ID、页码、每页大小
     * @return 评论分页列表
     */
    @GetMapping("/list")
    public Result<List<Comment>> listComments(CommentQueryRequestDTO request) {
        logger.info("Fetching comments for postId: {}, page: {}, size: {}", request.getPostId(), request.getPageNum(), request.getPageSize());
        List<Comment> comments = commentService.listCommentsByPost(request.getPostId(), request.getPageNum(), request.getPageSize());
        return Result.success(comments);
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
        boolean success = commentService.createComment(comment);
        if (success) {
            logger.info("Successfully created comment with ID: {}", comment.getId());
            return Result.success("评论成功");
        } else {
            return Result.error(500, "评论失败");
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
        boolean success = commentService.deleteComment(commentId);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error(500, "删除失败");
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
        Integer likeCount = commentService.toggleLike(commentId);
        return Result.success(likeCount);
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
        List<Comment> replies = commentService.listReplies(parentCommentId);
        return Result.success(replies);
    }
}
