package com.example.project.controller;

import java.util.List;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.project.common.CategoryConstants;
import com.example.project.common.PageResult;
import com.example.project.common.Result;
import com.example.project.dto.PostQueryRequest;
import com.example.project.entity.Post;
import com.example.project.service.PostService;
import com.example.project.service.PostSyncService;

/**
 * 帖子控制器
 * <p>
 * 提供帖子的增删改查API接口，包括帖子列表查询、详情查看、创建发布和删除等功能
 * </p>
 *
 * @author system
 * @since 1.0
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostService postService;

    @Autowired
    private PostSyncService postSyncService;

    /**
     * 获取所有帖子分类
     *
     * @return 分类数组
     */
    @GetMapping("/categories")
    public Result<String[]> getCategories() {
        logger.info("Fetching all post categories");
        String[] categories = CategoryConstants.ALL_CATEGORIES;
        logger.info("Successfully fetched {} categories", categories.length);
        return Result.success(categories);
    }

    /**
     * 获取帖子列表（支持分页和筛选）
     *
     * @param request 查询请求DTO，包含分类、关键词、地点、学校、认证状态、性别等筛选条件
     * @return 分页帖子列表
     */
    @GetMapping
    public Result<PageResult<Post>> list(PostQueryRequest request) {
        logger.info("Fetching post list - category: {}, keyword: {}, location: {}, school: {}, verified: {}, gender: {}, pageNum: {}, pageSize: {}",
                request.getCategory(), request.getKeyword(), request.getLocation(), request.getSchool(), request.getVerified(), request.getGender(), request.getPageNum(), request.getPageSize());
        PageResult<Post> result;
        if (request.hasUserFilters()) {
            result = postService.listPostsWithFilter(
                    request.getCategory(),
                    request.getKeyword(),
                    request.getLocation(),
                    request.getSchool(),
                    request.getVerified(),
                    request.getGender(),
                    request.getPageNum(),
                    request.getPageSize());
        } else {
            result = postService.listPostsWithPage(
                    request.getCategory(),
                    request.getKeyword(),
                    request.getPageNum(),
                    request.getPageSize());
        }
        logger.info("Successfully fetched {} posts (total: {})", result.getRecords().size(), result.getTotal());
        return Result.success(result);
    }

    /**
     * 获取帖子详情
     *
     * @param id 帖子ID
     * @return 帖子详情信息
     */
    @GetMapping("/{id}")
    public Result<Post> getDetail(@PathVariable Long id) {
        logger.info("Fetching post detail - id: {}", id);
        Post post = postService.getPostById(id);
        if (post == null) {
            return Result.error(404, "帖子不存在");
        }
        return Result.success(post);
    }

    /**
     * 获取指定用户的帖子列表
     *
     * @param userId 用户ID
     * @return 该用户发布的帖子列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Post>> getPostsByUser(@PathVariable Long userId) {
        logger.info("Fetching posts by user - userId: {}", userId);
        List<Post> posts = postService.getPostsByUserId(userId);
        return Result.success(posts);
    }

    /**
     * 创建新帖子（支持图片上传）
     *
     * @param title       帖子标题
     * @param content     帖子内容
     * @param category    帖子分类
     * @param destination 目的地，可选
     * @param images      图片文件列表，可选
     * @return 操作结果
     */
    @PostMapping
    public Result<String> create(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("category") String category,
            @RequestParam(value = "destination", required = false) String destination,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        logger.info("Creating new post - title: {}, category: {}", title, category);
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setCategory(category);
        post.setDestination(destination);
        
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = postService.uploadImages(images);
            if (!imageUrls.isEmpty()) {
                post.setImages(toJsonArray(imageUrls));
            }
        }
        
        boolean success = postService.createPost(post);
        if (success) {
            logger.info("Successfully created post with ID: {}", post.getId());
            return Result.success("发布成功");
        } else {
            logger.warn("Failed to create post - title: {}", title);
            return Result.error(500, "发布失败");
        }
    }

    /**
     * 删除帖子
     *
     * @param id 帖子ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deletePost(@PathVariable Long id) {
        logger.info("Deleting post - id: {}", id);
        postService.deletePost(id);
        return Result.success("删除成功");
    }

    /**
     * 全量同步所有帖子到 Elasticsearch
     *
     * @return 操作结果
     */
    @PostMapping("/sync/all")
    public Result<String> syncAllPostsToEs() {
        logger.info("Starting full sync of all posts to Elasticsearch");
        try {
            postSyncService.syncAllPosts();
            long count = postSyncService.getEsPostCount();
            logger.info("Successfully synced all posts to ES, current count: {}", count);
            return Result.success("同步完成，ES中共有 " + count + " 条帖子");
        } catch (Exception e) {
            logger.error("Failed to sync posts to ES", e);
            return Result.error(500, "同步失败: " + e.getMessage());
        }
    }

    /**
     * 获取ES中的帖子数量
     *
     * @return 帖子数量
     */
    @GetMapping("/sync/count")
    public Result<Long> getEsPostCount() {
        logger.info("Getting ES post count");
        try {
            long count = postSyncService.getEsPostCount();
            return Result.success(count);
        } catch (Exception e) {
            logger.error("Failed to get ES post count", e);
            return Result.error(500, "获取失败: " + e.getMessage());
        }
    }

    /**
     * 将字符串列表转换为JSON数组字符串
     *
     * @param list 字符串列表
     * @return JSON数组字符串
     */
    private String toJsonArray(List<String> list) {
        return JSON.toJSONString(list);
    }
}
