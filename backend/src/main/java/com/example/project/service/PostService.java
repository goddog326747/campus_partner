package com.example.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.project.entity.Post;
import java.util.List;

public interface PostService extends IService<Post> {
    List<Post> listPosts(String category, String keyword);
    boolean createPost(Post post);
}
