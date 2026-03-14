package com.example.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.project.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {

    /**
     * Get paginated comments for a post with user information populated
     * @param postId Post ID
     * @param pageNum Page number (1-based)
     * @param pageSize Page size
     * @return Page of comments with user details
     */
    Page<Comment> listCommentsByPost(Long postId, Integer pageNum, Integer pageSize);

    /**
     * Create a new comment
     * @param comment Comment object
     * @return true if successful
     */
    boolean createComment(Comment comment);

    /**
     * Delete a comment (only by owner or admin)
     * @param commentId Comment ID
     * @return true if successful
     */
    boolean deleteComment(Long commentId);

    /**
     * Like/unlike a comment (toggle)
     * @param commentId Comment ID
     * @return Updated like count
     */
    Integer toggleLike(Long commentId);

    /**
     * Get replies for a specific comment
     * @param parentCommentId Parent comment ID
     * @return List of replies
     */
    List<Comment> listReplies(Long parentCommentId);
}