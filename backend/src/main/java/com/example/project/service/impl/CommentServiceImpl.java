package com.example.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.project.entity.Comment;
import com.example.project.entity.User;
import com.example.project.mapper.CommentMapper;
import com.example.project.mapper.UserMapper;
import com.example.project.service.CommentService;
import com.example.project.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<Comment> listCommentsByPost(Long postId, Integer pageNum, Integer pageSize) {
        logger.info("Listing comments for postId: {}, page: {}, size: {}", postId, pageNum, pageSize);

        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId);
        queryWrapper.isNull("parent_id"); // Only get top-level comments
        queryWrapper.orderByAsc("create_time");

        Page<Comment> page = new Page<>(pageNum, pageSize);
        Page<Comment> result = page(page, queryWrapper);

        // Populate user information (following PostService pattern)
        populateUserInfo(result.getRecords());
        
        // Populate reply count for each comment
        populateReplyCount(result.getRecords());

        logger.info("Retrieved {} comments for postId: {}", result.getRecords().size(), postId);
        return result;
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

            boolean result = save(comment);
            if (result) {
                logger.info("Successfully created comment with ID: {}", comment.getId());
            }
            return result;
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
            Comment comment = getById(commentId);
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
            QueryWrapper<Comment> replyWrapper = new QueryWrapper<>();
            replyWrapper.eq("parent_id", commentId);
            remove(replyWrapper);

            // Delete the comment
            boolean result = removeById(commentId);
            if (result) {
                logger.info("Successfully deleted comment: {}", commentId);
            }
            return result;
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
            Comment comment = getById(commentId);
            if (comment == null) {
                logger.warn("Comment not found: {}", commentId);
                return 0;
            }

            // Simple increment/decrement logic
            // In a real system, you'd have a separate like_table to track who liked what
            int newCount = comment.getLikeCount() + 1;
            comment.setLikeCount(newCount);

            updateById(comment);
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

        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", parentCommentId);
        queryWrapper.orderByAsc("create_time");

        List<Comment> replies = list(queryWrapper);
        populateUserInfo(replies);

        logger.info("Retrieved {} replies for comment: {}", replies.size(), parentCommentId);
        return replies;
    }

    /**
     * Populate user information (username, avatar) for comments
     * Follows the same pattern as PostServiceImpl
     */
    private void populateUserInfo(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        // Extract unique user IDs
        Set<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (!userIds.isEmpty()) {
            // Batch query user information
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, user -> user));

            // Populate comment user info
            for (Comment comment : comments) {
                if (comment.getUserId() != null) {
                    User user = userMap.get(comment.getUserId());
                    if (user != null) {
                        // Priority: nickname > username
                        comment.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
                        comment.setAvatar(user.getAvatar());
                    }
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
            Integer replyCount = baseMapper.countReplies(comment.getId());
            comment.setReplyCount(replyCount != null ? replyCount : 0);
        }
    }
}