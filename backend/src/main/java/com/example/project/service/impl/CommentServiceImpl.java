package com.example.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.project.entity.Comment;
import com.example.project.entity.User;
import com.example.project.mapper.CommentMapper;
import com.example.project.mapper.UserMapper;
import com.example.project.service.CommentService;
import com.example.project.shiro.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Comment> listCommentsByPost(Long postId, Integer pageNum, Integer pageSize) {
        logger.info("Listing comments for postId: {}, page: {}, size: {}", postId, pageNum, pageSize);

        List<Comment> comments = commentMapper.selectByPostId(postId);
        
        // Populate user information
        populateUserInfo(comments);
        
        // Populate reply count for each comment
        populateReplyCount(comments);

        // Manual pagination
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, comments.size());
        if (start > comments.size()) {
            return new ArrayList<>();
        }

        logger.info("Retrieved {} comments for postId: {}", end - start, postId);
        return comments.subList(start, end);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createComment(Comment comment) {
        logger.info("Creating comment for postId: {}", comment.getPostId());

        try {
            // Set user ID from context
            if (UserContext.get() != null) {
                comment.setUserId(UserContext.get().getId());
                logger.debug("Setting comment userId from UserContext: {}", UserContext.get().getId());
            } else {
                logger.warn("No user in context, comment creation may fail");
                return false;
            }

            // Set default values
            if (comment.getLikeCount() == null) {
                comment.setLikeCount(0);
            }
            comment.setCreateTime(LocalDateTime.now());
            comment.setUpdateTime(LocalDateTime.now());

            int result = commentMapper.insert(comment);
            if (result > 0) {
                logger.info("Successfully created comment with ID: {}", comment.getId());
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error creating comment for postId: {}", comment.getPostId(), e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long commentId) {
        logger.info("Deleting comment: {}", commentId);

        try {
            // Verify ownership
            Comment comment = commentMapper.selectById(commentId);
            if (comment == null) {
                logger.warn("Comment not found: {}", commentId);
                return false;
            }

            // Only allow deletion by comment owner or admin
            Long currentUserId = UserContext.get() != null ? UserContext.get().getId() : null;
            if (!comment.getUserId().equals(currentUserId)) {
                logger.warn("User {} attempted to delete comment owned by user {}", currentUserId, comment.getUserId());
                return false;
            }

            // Delete replies first (cascade delete)
            List<Comment> replies = commentMapper.selectByParentId(commentId);
            for (Comment reply : replies) {
                commentMapper.deleteById(reply.getId());
            }

            // Delete the comment
            int result = commentMapper.deleteById(commentId);
            if (result > 0) {
                logger.info("Successfully deleted comment: {}", commentId);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting comment: {}", commentId, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer toggleLike(Long commentId) {
        logger.info("Toggling like for comment: {}", commentId);

        try {
            Comment comment = commentMapper.selectById(commentId);
            if (comment == null) {
                logger.warn("Comment not found: {}", commentId);
                return 0;
            }

            // Simple increment logic
            int newCount = comment.getLikeCount() + 1;
            
            LambdaUpdateWrapper<Comment> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(Comment::getId, commentId)
                   .set(Comment::getLikeCount, newCount)
                   .set(Comment::getUpdateTime, LocalDateTime.now());
            commentMapper.update(null, wrapper);
            logger.info("Updated like count for comment {}: {}", commentId, newCount);
            return newCount;
        } catch (Exception e) {
            logger.error("Error toggling like for comment: {}", commentId, e);
            throw e;
        }
    }

    @Override
    public List<Comment> listReplies(Long parentCommentId) {
        logger.info("Listing replies for comment: {}", parentCommentId);

        List<Comment> replies = commentMapper.selectByParentId(parentCommentId);
        populateUserInfo(replies);

        logger.info("Retrieved {} replies for comment: {}", replies.size(), parentCommentId);
        return replies;
    }

    /**
     * Populate user information (username, avatar) for comments
     */
    private void populateUserInfo(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        for (Comment comment : comments) {
            if (comment.getUserId() != null) {
                User user = userMapper.selectById(comment.getUserId());
                if (user != null) {
                    comment.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    comment.setAvatar(user.getAvatar());
                }
            }
        }
    }

    /**
     * Populate reply count for each comment
     */
    private void populateReplyCount(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        for (Comment comment : comments) {
            Integer replyCount = commentMapper.countReplies(comment.getId());
            comment.setReplyCount(replyCount != null ? replyCount : 0);
        }
    }
}
