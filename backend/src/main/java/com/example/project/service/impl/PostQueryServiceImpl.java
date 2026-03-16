package com.example.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.project.dto.PostQueryRequest;
import com.example.project.entity.Post;
import com.example.project.entity.User;
import com.example.project.mapper.PostMapper;
import com.example.project.mapper.UserMapper;
import com.example.project.service.PostQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostQueryServiceImpl implements PostQueryService {

    private static final Logger logger = LoggerFactory.getLogger(PostQueryServiceImpl.class);

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<Post> queryPosts(PostQueryRequest request) {
        logger.info("Querying posts with filters: category={}, keyword={}, location={}, school={}, verified={}, gender={}",
                request.getCategory(), request.getKeyword(), request.getLocation(),
                request.getSchool(), request.getVerified(), request.getGender());

        Page<Post> page = new Page<>(request.getPageNum(), request.getPageSize());
        QueryWrapper<Post> postWrapper = buildPostQueryWrapper(request);
        Page<Post> result = postMapper.selectPage(page, postWrapper);
        
        if (!result.getRecords().isEmpty()) {
            populateUserInfo(result.getRecords());
        }

        logger.info("Found {} posts", result.getTotal());
        return result;
    }

    @Override
    public List<Post> listPosts(String category, String keyword) {
        logger.info("Listing posts - category: {}, keyword: {}", category, keyword);
        
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(category)) {
            queryWrapper.eq("category", category);
        }
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like("title", keyword)
                    .or()
                    .like("content", keyword));
        }
        queryWrapper.orderByDesc("create_time");

        List<Post> posts = postMapper.selectList(queryWrapper);
        
        if (!posts.isEmpty()) {
            populateUserInfo(posts);
        }

        logger.info("Successfully retrieved {} posts", posts.size());
        return posts;
    }

    @Override
    public Post getPostById(Long id) {
        Post post = postMapper.selectById(id);
        if (post != null && post.getUserId() != null) {
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                populatePostUserInfo(post, user);
            }
        }
        return post;
    }

    @Override
    public List<Post> getPostsByUserId(Long userId) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        
        List<Post> posts = postMapper.selectList(queryWrapper);
        
        User user = userMapper.selectById(userId);
        if (user != null) {
            for (Post post : posts) {
                populatePostUserInfo(post, user);
            }
        }
        
        return posts;
    }

    private QueryWrapper<Post> buildPostQueryWrapper(PostQueryRequest request) {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(request.getCategory())) {
            wrapper.eq("category", request.getCategory());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like("title", request.getKeyword())
                    .or()
                    .like("content", request.getKeyword()));
        }
        
        if (request.hasUserFilters()) {
            List<Long> userIds = findMatchingUserIds(request);
            if (userIds.isEmpty()) {
                wrapper.apply("1 = 0");
                return wrapper;
            }
            wrapper.in("user_id", userIds);
        }
        
        wrapper.orderByDesc("create_time");
        return wrapper;
    }

    private List<Long> findMatchingUserIds(PostQueryRequest request) {
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(request.getLocation())) {
            userWrapper.eq("location", request.getLocation());
        }
        if (StringUtils.hasText(request.getSchool())) {
            userWrapper.eq("school", request.getSchool());
        }
        if (request.getVerified() != null && request.getVerified()) {
            userWrapper.eq("verified", 1);
        }
        if (request.getGender() != null) {
            userWrapper.eq("gender", request.getGender());
        }

        List<User> users = userMapper.selectList(userWrapper);
        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }

    private void populateUserInfo(List<Post> posts) {
        Set<Long> userIds = posts.stream()
                .map(Post::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));

            for (Post post : posts) {
                if (post.getUserId() != null) {
                    User user = userMap.get(post.getUserId());
                    if (user != null) {
                        populatePostUserInfo(post, user);
                    }
                }
            }
        }
    }

    private void populatePostUserInfo(Post post, User user) {
        post.setUsername(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
        post.setAvatar(user.getAvatar());
        post.setUserLocation(user.getLocation());
        post.setUserSchool(user.getSchool());
        post.setUserVerified(user.getVerified());
        post.setUserGender(user.getGender());
    }
}
