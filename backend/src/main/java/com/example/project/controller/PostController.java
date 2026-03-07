package com.example.project.controller;

import com.example.project.common.CategoryConstants;
import com.example.project.common.Result;
import com.example.project.entity.Post;
import com.example.project.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/post")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping("/categories")
    public Result<String[]> getCategories() {
        return Result.success(CategoryConstants.ALL_CATEGORIES);
    }

    @GetMapping("/list")
    public Result<List<Post>> list(@RequestParam(required = false) String category,
                                   @RequestParam(required = false) String keyword) {
        return Result.success(postService.listPosts(category, keyword));
    }

    @PostMapping("/create")
    public Result<String> create(@RequestBody Post post) {
        if (postService.createPost(post)) {
            return Result.success("发布成功");
        } else {
            return Result.error(500, "发布失败");
        }
    }
}
