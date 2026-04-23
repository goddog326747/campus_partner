package com.example.project.controller;

import java.util.List;

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
import com.example.project.common.Result;
import com.example.project.dto.PostFilterRequestDTO;
import com.example.project.entity.Post;
import com.example.project.service.PostService;

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
@RequestMapping("/api/post")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostService postService;

    /**
     * 获取所有帖子分类
     *
     * @return 分类数组
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
     * 获取帖子列表（简单查询）
     *
     * @param category 分类筛选条件，可选
     * @param keyword  关键词搜索条件，可选
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
     * 获取帖子列表（高级筛选，分页）
     *
     * @param request 筛选请求DTO，包含分类、关键词、地点、学校、认证状态、性别等筛选条件
     * @return 分页帖子列表
     */
    @GetMapping("/list/filter")
    public Result<List<Post>> listWithFilter(PostFilterRequestDTO request) {
        logger.info("Fetching post list with filters - category: {}, keyword: {}, location: {}, school: {}, verified: {}, gender: {}",
                request.getCategory(), request.getKeyword(), request.getLocation(), request.getSchool(), request.getVerified(), request.getGender());
        try {
            List<Post> posts = postService.listPosts(
                    request.getCategory(),
                    request.getKeyword(),
                    request.getLocation(),
                    request.getSchool(),
                    request.getVerified(),
                    request.getGender(),
                    request.getPageNum(),
                    request.getPageSize());
            return Result.success(posts);
        } catch (Exception e) {
            logger.error("Error fetching post list with filters", e);
            return Result.error(500, "获取帖子列表失败");
        }
    }

    /**
     * 获取帖子详情
     *
     * @param id 帖子ID
     * @return 帖子详情信息
     */
    @GetMapping("/detail/{id}")
    public Result<Post> getDetail(@PathVariable Long id) {
        logger.info("Fetching post detail - id: {}", id);
        try {
            Post post = postService.getPostById(id);
            if (post == null) {
                return Result.error(404, "帖子不存在");
            }
            return Result.success(post);
        } catch (Exception e) {
            logger.error("Error fetching post detail", e);
            return Result.error(500, "获取帖子详情失败");
        }
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
        try {
            List<Post> posts = postService.getPostsByUserId(userId);
            return Result.success(posts);
        } catch (Exception e) {
            logger.error("Error fetching user posts", e);
            return Result.error(500, "获取用户帖子失败");
        }
    }

    /**
     * 创建新帖子
     *
     * @param title       帖子标题
     * @param content     帖子内容
     * @param category    帖子分类
     * @param destination 目的地，可选
     * @param images      图片文件列表，可选
     * @return 操作结果
     */
    @PostMapping("/create")
    public Result<String> create(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("category") String category,
            @RequestParam(value = "destination", required = false) String destination,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        logger.info("Creating new post - title: {}, category: {}", title, category);
        try {
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
        } catch (Exception e) {
            logger.error("Error creating post", e);
            return Result.error(500, "发布失败: " + e.getMessage());
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
        try {
            postService.deletePost(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            logger.error("Error deleting post", e);
            return Result.error(500, "删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 将字符串列表转换为JSON数组字符串
     *
     * @param list 字符串列表
     * @return JSON数组字符串
     */
    private String toJsonArray(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(list.get(i)).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }
}
