package com.example.project.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.project.common.CategoryConstants;
import com.example.project.common.Result;
import com.example.project.entity.Post;
import com.example.project.service.PostService;

/**
 * 帖子控制器，提供帖子相关的API接口
 */
@RestController
@RequestMapping("/api/post")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostService postService;

    /**
     * 获取所有帖子分类
     * @return 包含所有分类的数组
     */
    @GetMapping("/categories")
    public Result<String[]> getCategories() {
        logger.info("Fetching all post categories");
        try {
            String[] categories = CategoryConstants.ALL_CATEGORIES;
            logger.info("Successfully fetched {} categories", categories.length);
            return Result.success(categories);
        } catch (Exception e) {
            logger.error("Error fetching categories", e);
            return Result.error(500, "获取分类失败");
        }
    }

    /**
     * 获取帖子列表，支持按分类和关键词筛选
     * @param category 分类名称，可选
     * @param keyword 搜索关键词，可选
     * @return 帖子列表
     */
    @GetMapping("/list")
    public Result<List<Post>> list(@RequestParam(required = false) String category,
                                   @RequestParam(required = false) String keyword) {
        logger.info("Fetching post list - category: {}, keyword: {}", category, keyword);
        try {
            List<Post> posts = postService.listPosts(category, keyword);
            logger.info("Successfully fetched {} posts", posts.size());
            return Result.success(posts);
        } catch (Exception e) {
            logger.error("Error fetching post list", e);
            return Result.error(500, "获取帖子列表失败");
        }
    }

    /**
     * 创建新帖子
     * @param post 帖子对象
     * @return 操作结果
     */
    @PostMapping("/create")
    public Result<String> create(@RequestBody Post post) {
        logger.info("Creating new post - title: {}", post.getTitle());
        try {
            boolean success = postService.createPost(post);
            if (success) {
                logger.info("Successfully created post with ID: {}", post.getId());
                return Result.success("发布成功");
            } else {
                logger.warn("Failed to create post - title: {}", post.getTitle());
                return Result.error(500, "发布失败");
            }
        } catch (Exception e) {
            logger.error("Error creating post", e);
            return Result.error(500, "发布失败: " + e.getMessage());
        }
    }
}
