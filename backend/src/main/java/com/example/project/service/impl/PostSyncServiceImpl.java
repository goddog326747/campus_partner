package com.example.project.service.impl;

import com.alibaba.fastjson.JSON;
import com.example.project.document.PostDocument;
import com.example.project.entity.Post;
import com.example.project.entity.User;
import com.example.project.mapper.CommentMapper;
import com.example.project.mapper.PostMapper;
import com.example.project.mapper.UserMapper;
import com.example.project.repository.PostSearchRepository;
import com.example.project.service.PostSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 帖子数据同步服务实现类
 * <p>
 * 实现 MySQL 与 Elasticsearch 之间的数据同步功能
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Service
public class PostSyncServiceImpl implements PostSyncService {

    private static final Logger logger = LoggerFactory.getLogger(PostSyncServiceImpl.class);

    @Autowired
    private PostSearchRepository postSearchRepository;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Override
    @Async
    public void syncPost(Long postId) {
        logger.info("Syncing post to ES - postId: {}", postId);
        try {
            Post post = postMapper.selectById(postId);
            if (post != null) {
                syncPost(post);
            } else {
                logger.warn("Post not found - postId: {}", postId);
            }
        } catch (Exception e) {
            logger.error("Error syncing post to ES - postId: {}", postId, e);
        }
    }

    @Override
    public void syncPost(Post post) {
        logger.info("Syncing post to ES - postId: {}, title: {}", post.getId(), post.getTitle());
        try {
            PostDocument document = convertToDocument(post);
            postSearchRepository.save(document);
            logger.info("Successfully synced post to ES - postId: {}", post.getId());
        } catch (Exception e) {
            logger.error("Error syncing post to ES - postId: {}", post.getId(), e);
        }
    }

    @Override
    public void syncPosts(List<Post> posts) {
        if (CollectionUtils.isEmpty(posts)) {
            return;
        }
        logger.info("Batch syncing {} posts to ES", posts.size());
        try {
            List<PostDocument> documents = posts.stream()
                    .map(this::convertToDocument)
                    .collect(Collectors.toList());
            postSearchRepository.saveAll(documents);
            logger.info("Successfully batch synced {} posts to ES", posts.size());
        } catch (Exception e) {
            logger.error("Error batch syncing posts to ES", e);
        }
    }

    @Override
    public void deletePostFromEs(Long postId) {
        logger.info("Deleting post from ES - postId: {}", postId);
        try {
            postSearchRepository.deleteById(postId);
            logger.info("Successfully deleted post from ES - postId: {}", postId);
        } catch (Exception e) {
            logger.error("Error deleting post from ES - postId: {}", postId, e);
        }
    }

    @Override
    public void syncAllPosts() {
        logger.info("Starting full sync of all posts to ES");
        try {
            List<Post> allPosts = postMapper.selectList(null);
            logger.info("Found {} posts to sync", allPosts.size());

            int batchSize = 100;
            for (int i = 0; i < allPosts.size(); i += batchSize) {
                List<Post> batch = allPosts.subList(i, Math.min(i + batchSize, allPosts.size()));
                syncPosts(batch);
                logger.info("Synced batch {}/{} to ES", (i / batchSize) + 1, (allPosts.size() + batchSize - 1) / batchSize);
            }
            logger.info("Full sync completed - total posts: {}", allPosts.size());
        } catch (Exception e) {
            logger.error("Error during full sync", e);
        }
    }

    @Override
    public PostDocument convertToDocument(Post post) {
        PostDocument document = new PostDocument();
        document.setId(post.getId());
        document.setTitle(post.getTitle());
        document.setContent(post.getContent());
        document.setCategory(post.getCategory());
        document.setUserId(post.getUserId());
        document.setDestination(post.getDestination());
        document.setCreateTime(post.getCreateTime());
        document.setUpdateTime(post.getUpdateTime());

        // Parse images JSON
        if (StringUtils.hasText(post.getImages())) {
            try {
                List<String> imageList = JSON.parseArray(post.getImages(), String.class);
                document.setImages(imageList);
            } catch (Exception e) {
                logger.warn("Failed to parse images JSON for post: {}", post.getId());
                document.setImages(new ArrayList<>());
            }
        } else {
            document.setImages(new ArrayList<>());
        }

        // Populate user information
        if (post.getUserId() != null) {
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                document.setUsername(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
                document.setAvatar(user.getAvatar());
                document.setUserLocation(user.getLocation());
                document.setUserSchool(user.getSchool());
                document.setUserVerified(user.getVerified());
                document.setUserGender(user.getGender());
            }
        }

        // Get comment count
        try {
            Integer commentCount = commentMapper.selectCountByPostId(post.getId());
            document.setCommentCount(commentCount != null ? commentCount : 0);
        } catch (Exception e) {
            logger.warn("Failed to get comment count for post: {}", post.getId());
            document.setCommentCount(0);
        }

        return document;
    }
}
