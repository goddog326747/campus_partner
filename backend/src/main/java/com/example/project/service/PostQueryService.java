package com.example.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.project.dto.PostQueryRequest;
import com.example.project.entity.Post;

import java.util.List;

public interface PostQueryService {
    
    Page<Post> queryPosts(PostQueryRequest request);
    
    List<Post> listPosts(String category, String keyword);
    
    Post getPostById(Long id);
    
    List<Post> getPostsByUserId(Long userId);
}
