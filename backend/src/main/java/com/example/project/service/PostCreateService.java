package com.example.project.service;

import com.example.project.dto.PostCreateRequest;
import com.example.project.entity.Post;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostCreateService {
    
    Post createPost(PostCreateRequest request);
    
    boolean createPost(Post post);
    
    List<String> uploadPostImages(List<MultipartFile> files);
    
    void deletePost(Long postId);
}
