package com.example.project.service.impl;

import com.example.project.common.PageResult;
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
    public PageResult<Post> listPostsWithPage(String category, String keyword, int pageNum, int pageSize) {
        logger.info("Listing posts with page - category: {}, keyword: {}, pageNum: {}, pageSize: {}",
                category, keyword, pageNum, pageSize);
        try {
            List<Post> allPosts = postMapper.selectByCondition(category, keyword);
            long total = allPosts.size();

            for (Post post : allPosts) {
                if (post.getUserId() != null) {
                    User user = userMapper.selectById(post.getUserId());
                    if (user != null) {
                        post.setUsername(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
                        post.setAvatar(user.getAvatar());
                    }
                }
            }

            int start = (pageNum - 1) * pageSize;
            if (start >= allPosts.size()) {
                return PageResult.of(new ArrayList<>(), total, pageNum, pageSize);
            }
            int end = Math.min(start + pageSize, allPosts.size());
            List<Post> pagePosts = allPosts.subList(start, end);

            logger.info("Successfully retrieved {} posts (total: {})", pagePosts.size(), total);
            return PageResult.of(pagePosts, total, pageNum, pageSize);
        } catch (Exception e) {
            logger.error("Error listing posts with page", e);
            throw e;
        }
    }

    @Override
    public PageResult<Post> listPostsWithFilter(String category, String keyword, String location, String school, Boolean verified, Integer gender, int pageNum, int pageSize) {
        logger.info("Listing posts with filters - category: {}, keyword: {}, location: {}, school: {}, verified: {}, gender: {}, pageNum: {}, pageSize: {}",
                category, keyword, location, school, verified, gender, pageNum, pageSize);

        Integer verifiedInt = null;
        if (verified != null) {
            verifiedInt = verified ? 2 : 0;
        }

        int offset = (pageNum - 1) * pageSize;

        List<Post> posts = postMapper.selectByConditionWithFilters(
                category, keyword, location, school, verifiedInt, gender, offset, pageSize);

        long total = postMapper.countByConditionWithFilters(
                category, keyword, location, school, verifiedInt, gender);

        for (Post post : posts) {
            if (post.getUserId() != null) {
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
        }

        logger.info("Successfully retrieved {} posts (total: {})", posts.size(), total);
        return PageResult.of(posts, total, pageNum, pageSize);
    }

    @Override
    public boolean createPost(Post post) {
        logger.info("Creating post - title: {}", post.getTitle());
        try {
            post.setCreateTime(LocalDateTime.now());
            post.setUpdateTime(LocalDateTime.now());
            Long userId = UserContext.getUserId();
            if (userId != null) {
                post.setUserId(userId);
                logger.debug("Setting post userId from UserContext: {}", userId);
            }
            int result = postMapper.insert(post);
            if (result > 0) {
                logger.info("Successfully created post with ID: {}", post.getId());
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
        postSyncService.deletePostFromEs(postId);
        logger.info("Successfully deleted post - id: {}", postId);
    }
}
