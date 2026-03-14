package com.example.project.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.project.entity.Post;
import com.example.project.entity.User;
import com.example.project.mapper.PostMapper;
import com.example.project.mapper.UserMapper;
import com.example.project.service.PostService;
import com.example.project.service.helper.PostFilterHelper;
import com.example.project.storage.StorageService;
import com.example.project.storage.StorageServiceFactory;
import com.example.project.util.UserContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);
    private static final String IMAGE_DIRECTORY = "post";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostFilterHelper postFilterHelper;

    @Autowired
    private StorageServiceFactory storageServiceFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<Post> listPosts(String category, String keyword) {
        logger.info("Listing posts - category: {}, keyword: {}", category, keyword);
        try {
            QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
            if (StringUtils.hasText(category)) {
                queryWrapper.eq("category", category);
            }
            if (StringUtils.hasText(keyword)) {
                queryWrapper.and(wrapper -> wrapper.like("title", keyword).or().like("content", keyword));
            }
            queryWrapper.orderByDesc("create_time");
            
            List<Post> posts = baseMapper.selectList(queryWrapper);
            
            if (!posts.isEmpty()) {
                Set<Long> userIds = posts.stream()
                        .map(Post::getUserId)
                        .filter(id -> id != null)
                        .collect(Collectors.toSet());
                
                if (!userIds.isEmpty()) {
                    List<User> users = userMapper.selectBatchIds(userIds);
                    Map<Long, String> userMap = users.stream()
                            .collect(Collectors.toMap(User::getId, user -> {
                                return StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
                            }));
                    
                    for (Post post : posts) {
                        if (post.getUserId() != null) {
                            post.setUsername(userMap.get(post.getUserId()));
                        }
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
    public Page<Post> listPosts(String category, String keyword, String location, String school, Boolean verified, Integer gender, int pageNum, int pageSize) {
        logger.info("Listing posts with filters - category: {}, keyword: {}, location: {}, school: {}, verified: {}, gender: {}", 
                category, keyword, location, school, verified, gender);
        
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(category)) {
            queryWrapper.eq("category", category);
        }
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like("title", keyword).or().like("content", keyword));
        }
        
        postFilterHelper.applyUserFilters(queryWrapper, location, school, verified, gender);
        
        queryWrapper.orderByDesc("create_time");
        
        Page<Post> page = new Page<>(pageNum, pageSize);
        Page<Post> resultPage = baseMapper.selectPage(page, queryWrapper);
        
        postFilterHelper.populateUserInfo(resultPage.getRecords());
        
        logger.info("Successfully retrieved {} posts (total: {})", resultPage.getRecords().size(), resultPage.getTotal());
        return resultPage;
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
            boolean result = save(post);
            if (result) {
                logger.info("Successfully created post with ID: {}", post.getId());
            } else {
                logger.error("Failed to create post - title: {}", post.getTitle());
            }
            return result;
        } catch (Exception e) {
            logger.error("Error creating post - title: {}", post.getTitle(), e);
            throw e;
        }
    }

    @Override
    public Post getPostById(Long id) {
        Post post = baseMapper.selectById(id);
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
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        List<Post> posts = baseMapper.selectList(queryWrapper);
        
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
        Post post = baseMapper.selectById(postId);
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
        baseMapper.deleteById(postId);
        logger.info("Successfully deleted post - id: {}", postId);
    }
}
