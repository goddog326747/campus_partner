package com.example.project.service.impl;

import com.alibaba.fastjson.JSON;
import com.example.project.elasticsearch.document.PostDocument;
import com.example.project.elasticsearch.repository.PostSearchRepository;
import com.example.project.entity.Post;
import com.example.project.entity.User;
import com.example.project.mapper.CommentMapper;
import com.example.project.mapper.PostMapper;
import com.example.project.mapper.UserMapper;
import com.example.project.service.PostSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class PostSyncServiceImpl implements PostSyncService {

    private final PostSearchRepository postSearchRepository;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;

    @Override
    @Async
    public void syncPost(Long postId) {
        log.info("Syncing post to ES - postId: {}", postId);
        try {
            Post post = postMapper.selectById(postId);
            if (post != null) {
                syncPost(post);
            } else {
                log.warn("Post not found - postId: {}", postId);
            }
        } catch (Exception e) {
            log.error("Error syncing post to ES - postId: {}", postId, e);
        }
    }

    @Override
    public void syncPost(Post post) {
        log.info("Syncing post to ES - postId: {}, title: {}", post.getId(), post.getTitle());
        try {
            PostDocument document = convertToDocument(post);
            postSearchRepository.save(document);
            log.info("Successfully synced post to ES - postId: {}", post.getId());
        } catch (Exception e) {
            log.error("Error syncing post to ES - postId: {}", post.getId(), e);
        }
    }

    @Override
    public void syncPosts(List<Post> posts) {
        if (CollectionUtils.isEmpty(posts)) {
            return;
        }
        log.info("Batch syncing {} posts to ES", posts.size());
        try {
            List<PostDocument> documents = posts.stream()
                    .map(this::convertToDocument)
                    .collect(Collectors.toList());
            postSearchRepository.saveAll(documents);
            log.info("Successfully batch synced {} posts to ES", posts.size());
        } catch (Exception e) {
            log.error("Error batch syncing posts to ES", e);
        }
    }

    @Override
    public void deletePostFromEs(Long postId) {
        log.info("Deleting post from ES - postId: {}", postId);
        try {
            postSearchRepository.deleteById(postId);
            log.info("Successfully deleted post from ES - postId: {}", postId);
        } catch (Exception e) {
            log.error("Error deleting post from ES - postId: {}", postId, e);
        }
    }

    @Override
    public void syncAllPosts() {
        log.info("Starting full sync of all posts to ES");
        try {
            List<Post> allPosts = postMapper.selectList(null);
            log.info("Found {} posts to sync", allPosts.size());

            int batchSize = 100;
            for (int i = 0; i < allPosts.size(); i += batchSize) {
                List<Post> batch = allPosts.subList(i, Math.min(i + batchSize, allPosts.size()));
                syncPosts(batch);
                log.info("Synced batch {}/{} to ES", (i / batchSize) + 1, (allPosts.size() + batchSize - 1) / batchSize);
            }
            log.info("Full sync completed - total posts: {}", allPosts.size());
        } catch (Exception e) {
            log.error("Error during full sync", e);
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
                log.warn("Failed to parse images JSON for post: {}", post.getId());
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
            log.warn("Failed to get comment count for post: {}", post.getId());
            document.setCommentCount(0);
        }

        return document;
    }
}
