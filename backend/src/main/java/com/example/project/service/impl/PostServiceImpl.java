package com.example.project.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.project.entity.Post;
import com.example.project.entity.User;
import com.example.project.mapper.PostMapper;
import com.example.project.mapper.UserMapper;
import com.example.project.service.PostService;
import com.example.project.util.UserContext;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

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
            
            // 批量查询用户信息并填充
            if (!posts.isEmpty()) {
                Set<Long> userIds = posts.stream()
                        .map(Post::getUserId)
                        .filter(id -> id != null)
                        .collect(Collectors.toSet());
                
                if (!userIds.isEmpty()) {
                    List<User> users = userMapper.selectBatchIds(userIds);
                    Map<Long, String> userMap = users.stream()
                            .collect(Collectors.toMap(User::getId, user -> {
                                // 优先显示昵称，没有则显示用户名
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
    public boolean createPost(Post post) {
        logger.info("Creating post - title: {}", post.getTitle());
        try {
            post.setCreateTime(LocalDateTime.now());
            post.setUpdateTime(LocalDateTime.now());
            // 从 UserContext 获取当前用户ID (如果已登录)
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
}
