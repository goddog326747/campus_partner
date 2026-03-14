package com.example.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.project.entity.Post;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService extends IService<Post> {
    List<Post> listPosts(String category, String keyword);
    
    Page<Post> listPosts(String category, String keyword, String location, String school, Boolean verified, Integer gender, int pageNum, int pageSize);
    
    boolean createPost(Post post);
    
    Post getPostById(Long id);
    
    List<Post> getPostsByUserId(Long userId);
    
    List<String> uploadImages(List<MultipartFile> files);
    
    void deletePost(Long postId);
}
