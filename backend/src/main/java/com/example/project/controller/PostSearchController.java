package com.example.project.controller;

import com.example.project.common.Result;
import com.example.project.dto.PostSearchRequest;
import com.example.project.elasticsearch.document.PostDocument;
import com.example.project.service.PostSearchService;
import com.example.project.service.PostSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 帖子搜索控制器
 * <p>
 * 提供基于 Elasticsearch 的帖子搜索 API 接口
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class PostSearchController {

    private final PostSearchService postSearchService;
    private final PostSyncService postSyncService;

    /**
     * 关键词搜索帖子
     *
     * @param keyword  关键词
     * @param pageNum  页码（默认1）
     * @param pageSize 每页大小（默认10）
     * @return 分页帖子列表
     */
    @GetMapping("/keyword")
    public Result<Page<PostDocument>> searchByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Search by keyword: {}, page: {}, size: {}", keyword, pageNum, pageSize);
        try {
            Page<PostDocument> result = postSearchService.searchByKeyword(keyword, pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Error searching by keyword: {}", keyword, e);
            return Result.error(500, "搜索失败: " + e.getMessage());
        }
    }

    /**
     * 高级搜索帖子
     *
     * @param request 搜索请求参数
     * @return 分页帖子列表
     */
    @PostMapping("/advanced")
    public Result<Page<PostDocument>> advancedSearch(@RequestBody PostSearchRequest request) {
        log.info("Advanced search with request: {}", request);
        try {
            Page<PostDocument> result = postSearchService.advancedSearch(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return Result.error(500, "搜索失败: " + e.getMessage());
        }
    }

    /**
     * 根据分类搜索帖子
     *
     * @param category 分类
     * @param pageNum  页码（默认1）
     * @param pageSize 每页大小（默认10）
     * @return 分页帖子列表
     */
    @GetMapping("/category/{category}")
    public Result<Page<PostDocument>> searchByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Search by category: {}, page: {}, size: {}", category, pageNum, pageSize);
        try {
            Page<PostDocument> result = postSearchService.searchByCategory(category, pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Error searching by category: {}", category, e);
            return Result.error(500, "搜索失败: " + e.getMessage());
        }
    }

    /**
     * 根据目的地搜索帖子
     *
     * @param destination 目的地
     * @param pageNum     页码（默认1）
     * @param pageSize    每页大小（默认10）
     * @return 分页帖子列表
     */
    @GetMapping("/destination")
    public Result<Page<PostDocument>> searchByDestination(
            @RequestParam String destination,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Search by destination: {}, page: {}, size: {}", destination, pageNum, pageSize);
        try {
            Page<PostDocument> result = postSearchService.searchByDestination(destination, pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Error searching by destination: {}", destination, e);
            return Result.error(500, "搜索失败: " + e.getMessage());
        }
    }

    /**
     * 获取搜索建议
     *
     * @param keyword 关键词前缀
     * @param size    建议数量（默认10）
     * @return 建议列表
     */
    @GetMapping("/suggestions")
    public Result<List<String>> getSearchSuggestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get search suggestions for keyword: {}, size: {}", keyword, size);
        try {
            List<String> suggestions = postSearchService.getSearchSuggestions(keyword, size);
            return Result.success(suggestions);
        } catch (Exception e) {
            log.error("Error getting search suggestions", e);
            return Result.error(500, "获取搜索建议失败: " + e.getMessage());
        }
    }

    /**
     * 获取热门搜索词
     *
     * @param size 数量（默认10）
     * @return 热门搜索词列表
     */
    @GetMapping("/hot-keywords")
    public Result<List<String>> getHotSearchKeywords(
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get hot search keywords, size: {}", size);
        try {
            List<String> keywords = postSearchService.getHotSearchKeywords(size);
            return Result.success(keywords);
        } catch (Exception e) {
            log.error("Error getting hot search keywords", e);
            return Result.error(500, "获取热门搜索词失败: " + e.getMessage());
        }
    }

    /**
     * 全量同步帖子到 ES
     *
     * @return 操作结果
     */
    @PostMapping("/sync/all")
    public Result<String> syncAllPosts() {
        log.info("Sync all posts to ES");
        try {
            postSyncService.syncAllPosts();
            return Result.success("同步任务已启动");
        } catch (Exception e) {
            log.error("Error syncing all posts", e);
            return Result.error(500, "同步失败: " + e.getMessage());
        }
    }

    /**
     * 同步单个帖子到 ES
     *
     * @param postId 帖子ID
     * @return 操作结果
     */
    @PostMapping("/sync/{postId}")
    public Result<String> syncPost(@PathVariable Long postId) {
        log.info("Sync post to ES - postId: {}", postId);
        try {
            postSyncService.syncPost(postId);
            return Result.success("同步成功");
        } catch (Exception e) {
            log.error("Error syncing post: {}", postId, e);
            return Result.error(500, "同步失败: " + e.getMessage());
        }
    }
}
