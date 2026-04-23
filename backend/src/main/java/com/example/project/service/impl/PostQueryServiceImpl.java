package com.example.project.service.impl;

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

import java.util.ArrayList;
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
    public List<Post> queryPosts(PostQueryRequest request) {
        logger.info("Querying posts with filters: category={}, keyword={}, location={}, school={}, verified={}, gender={}",
                request.getCategory(), request.getKeyword(), request.getLocation(),
                request.getSchool(), request.getVerified(), request.getGender());

        // 先根据条件查询帖子
        List<Post> posts = postMapper.selectByCondition(request.getCategory(), request.getKeyword());

        // 如果有用户筛选条件，进一步过滤
        if (request.hasUserFilters()) {
            List<Long> matchingUserIds = findMatchingUserIds(request);
            if (matchingUserIds.isEmpty()) {
                return new ArrayList<>();
            }
            posts = posts.stream()
                    .filter(post -> matchingUserIds.contains(post.getUserId()))
                    .collect(Collectors.toList());
        }

        // 填充用户信息
        if (!posts.isEmpty()) {
            populateUserInfo(posts);
        }

        // 手动分页
        int start = (request.getPageNum() - 1) * request.getPageSize();
        int end = Math.min(start + request.getPageSize(), posts.size());
        if (start > posts.size()) {
            return new ArrayList<>();
        }

        logger.info("Found {} posts", posts.size());
        return posts.subList(start, end);
    }

    @Override
    public List<Post> listPosts(String category, String keyword) {
        logger.info("Listing posts - category: {}, keyword: {}", category, keyword);

        List<Post> posts = postMapper.selectByCondition(category, keyword);

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
        List<Post> posts = postMapper.selectByUserId(userId);

        User user = userMapper.selectById(userId);
        if (user != null) {
            for (Post post : posts) {
                populatePostUserInfo(post, user);
            }
        }

        return posts;
    }

    private List<Long> findMatchingUserIds(PostQueryRequest request) {
        // 获取所有用户，然后手动筛选
        List<User> allUsers = userMapper.selectAll();

        return allUsers.stream()
                .filter(user -> {
                    if (StringUtils.hasText(request.getLocation()) && !request.getLocation().equals(user.getLocation())) {
                        return false;
                    }
                    if (StringUtils.hasText(request.getSchool()) && !request.getSchool().equals(user.getSchool())) {
                        return false;
                    }
                    if (request.getVerified() != null && !request.getVerified().equals(user.getVerified())) {
                        return false;
                    }
                    if (request.getGender() != null && !request.getGender().equals(user.getGender())) {
                        return false;
                    }
                    return true;
                })
                .map(User::getId)
                .collect(Collectors.toList());
    }

    private void populateUserInfo(List<Post> posts) {
        Set<Long> userIds = posts.stream()
                .map(Post::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (!userIds.isEmpty()) {
            // 查询所有用户，然后过滤
            List<User> allUsers = userMapper.selectAll();
            Map<Long, User> userMap = allUsers.stream()
                    .filter(u -> userIds.contains(u.getId()))
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
