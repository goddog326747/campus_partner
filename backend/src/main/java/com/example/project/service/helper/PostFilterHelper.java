package com.example.project.service.helper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.project.entity.Post;
import com.example.project.entity.User;
import com.example.project.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PostFilterHelper {

    @Autowired
    private UserMapper userMapper;

    public void applyUserFilters(QueryWrapper<Post> queryWrapper, 
                                  String location, 
                                  String school, 
                                  Boolean verified, 
                                  Integer gender) {
        if (!hasUserFilters(location, school, verified, gender)) {
            return;
        }
        
        Set<Long> matchingUserIds = findMatchingUserIds(location, school, verified, gender);
        if (matchingUserIds.isEmpty()) {
            queryWrapper.eq("1", "0");
        } else {
            queryWrapper.in("user_id", matchingUserIds);
        }
    }

    public boolean hasUserFilters(String location, String school, Boolean verified, Integer gender) {
        return StringUtils.hasText(location) || 
               StringUtils.hasText(school) || 
               verified != null || 
               gender != null;
    }

    public Set<Long> findMatchingUserIds(String location, String school, Boolean verified, Integer gender) {
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        
        if (StringUtils.hasText(location)) {
            userQuery.eq("location", location);
        }
        if (StringUtils.hasText(school)) {
            userQuery.eq("school", school);
        }
        if (verified != null && verified) {
            userQuery.eq("verified", 1);
        }
        if (gender != null) {
            userQuery.eq("gender", gender);
        }
        
        List<User> users = userMapper.selectList(userQuery);
        return users.stream()
                .map(User::getId)
                .collect(Collectors.toSet());
    }

    public void populateUserInfo(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        
        Set<Long> userIds = posts.stream()
                .map(Post::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        
        if (userIds.isEmpty()) {
            return;
        }
        
        List<User> users = userMapper.selectBatchIds(userIds);
        
        for (Post post : posts) {
            users.stream()
                    .filter(u -> u.getId().equals(post.getUserId()))
                    .findFirst()
                    .ifPresent(user -> {
                        post.setUsername(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
                        post.setAvatar(user.getAvatar());
                        post.setUserLocation(user.getLocation());
                        post.setUserSchool(user.getSchool());
                        post.setUserVerified(user.getVerified());
                        post.setUserGender(user.getGender());
                    });
        }
    }
}
