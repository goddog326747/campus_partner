package com.example.project.service.impl;

import com.example.project.entity.Post;
import com.example.project.entity.User;
import com.example.project.mapper.PostMapper;
import com.example.project.mapper.UserMapper;
import com.example.project.service.PostService;
import com.example.project.service.PostSyncService;
import com.example.project.service.storage.StorageService;
import com.example.project.service.storage.StorageServiceFactory;
import com.example.project.shiro.util.UserContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);
    private static final String IMAGE_DIRECTORY = "post";

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StorageServiceFactory storageServiceFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostSyncService postSyncService;

    @Override
    public List<Post> listPosts(String category, String keyword) {
        logger.info("Listing posts - category: {}, keyword: {}", category, keyword);
        try {
            List<Post> posts = postMapper.selectByCondition(category, keyword);
            
            // Populate user information
            for (Post post : posts) {
                if (post.getUserId() != null) {
                    User user = userMapper.selectById(post.getUserId());
                    if (user != null) {
                        post.setUsername(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
                    }
                }
            }
            
            logger.info("Successfully retrieved {} posts", posts.size());
            return posts;
        } catch (Exception e) {
            logger.error("Error listing posts", e);
            throw e;
        }
    }

    @Override
    public List<Post> listPosts(String category, String keyword, String location, String school, Boolean verified, Integer gender, int pageNum, int pageSize) {
        logger.info("Listing posts with filters - category: {}, keyword: {}, location: {}, school: {}, verified: {}, gender: {}", 
                category, keyword, location, school, verified, gender);
        
        // For now, use basic condition query. Advanced filtering can be added in XML
        List<Post> posts = postMapper.selectByCondition(category, keyword);
        
        // Populate user information with filters
        List<Post> filteredPosts = new ArrayList<>();
        for (Post post : posts) {
            if (post.getUserId() != null) {
                User user = userMapper.selectById(post.getUserId());
                if (user != null) {
                    // Apply user filters
                    if (location != null && !location.isEmpty() && !location.equals(user.getLocation())) {
                        continue;
                    }
                    if (school != null && !school.isEmpty() && !school.equals(user.getSchool())) {
                        continue;
                    }
                    if (verified != null && !verified.equals(user.getVerified())) {
                        continue;
                    }
                    if (gender != null && !gender.equals(user.getGender())) {
                        continue;
                    }
                    
                    post.setUsername(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
                    post.setAvatar(user.getAvatar());
                    post.setUserLocation(user.getLocation());
                    post.setUserSchool(user.getSchool());
                    post.setUserVerified(user.getVerified());
                    post.setUserGender(user.getGender());
                }
            }
            filteredPosts.add(post);
        }
        
        // Manual pagination
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredPosts.size());
        if (start > filteredPosts.size()) {
            return new ArrayList<>();
        }
        
        logger.info("Successfully retrieved {} posts (total: {})", end - start, filteredPosts.size());
        return filteredPosts.subList(start, end);
    }

    @Override
    public boolean createPost(Post post) {
        logger.info("Creating post - title: {}", post.getTitle());
        try {
            post.setCreateTime(LocalDateTime.now());
            post.setUpdateTime(LocalDateTime.now());
            if (UserContext.get() != null) {
                post.setUserId(UserContext.get().getId());
                logger.debug("Setting post userId from UserContext: {}", UserContext.get().getId());
            }
            int result = postMapper.insert(post);
            if (result > 0) {
                logger.info("Successfully created post with ID: {}", post.getId());
                // 同步到 ES
                postSyncService.syncPost(post);
                return true;
            } else {
                logger.error("Failed to create post - title: {}", post.getTitle());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error creating post - title: {}", post.getTitle(), e);
            throw e;
        }
    }

    @Override
    public Post getPostById(Long id) {
        Post post = postMapper.selectById(id);
        if (post != null && post.getUserId() != null) {
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                post.setUsername(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
                post.setAvatar(user.getAvatar());
                post.setUserLocation(user.getLocation());
                post.setUserSchool(user.getSchool());
                post.setUserVerified(user.getVerified());
                post.setUserGender(user.getGender());
            }
        }
        return post;
    }

    @Override
    public List<Post> getPostsByUserId(Long userId) {
        List<Post> posts = postMapper.selectByUserId(userId);
        
        User user = userMapper.selectById(userId);
        if (user != null) {
            String username = StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
            for (Post post : posts) {
                post.setUsername(username);
            }
        }
        
        return posts;
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> files) {
        logger.info("Uploading {} images for post", files.size());
        List<String> urls = new ArrayList<>();
        StorageService storageService = storageServiceFactory.getStorageService();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    String url = storageService.upload(file, IMAGE_DIRECTORY);
                    urls.add(url);
                    logger.debug("Uploaded image: {}", url);
                } catch (Exception e) {
                    logger.error("Failed to upload image: {}", file.getOriginalFilename(), e);
                }
            }
        }
        
        logger.info("Successfully uploaded {} images", urls.size());
        return urls;
    }

    @Override
    public void deletePost(Long postId) {
        logger.info("Deleting post - id: {}", postId);
        Post post = postMapper.selectById(postId);
        if (post != null && StringUtils.hasText(post.getImages())) {
            try {
                List<String> imageUrls = objectMapper.readValue(post.getImages(), List.class);
                StorageService storageService = storageServiceFactory.getStorageService();
                for (String url : imageUrls) {
                    try {
                        storageService.delete(url);
                    } catch (Exception e) {
                        logger.warn("Failed to delete image: {}", url, e);
                    }
                }
            } catch (JsonProcessingException e) {
                logger.warn("Failed to parse images JSON for post: {}", postId, e);
            }
        }
        postMapper.deleteById(postId);
        // 从 ES 删除
        postSyncService.deletePostFromEs(postId);
        logger.info("Successfully deleted post - id: {}", postId);
    }
}
