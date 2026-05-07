package com.example.project.controller;

import com.example.project.common.PageResult;
import com.example.project.common.Result;
import com.example.project.dto.PostSearchRequest;
import com.example.project.entity.Post;
import com.example.project.service.PostSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/api/posts/search")
@RequiredArgsConstructor
public class PostSearchController {

    private final PostSearchService postSearchService;

    /**
     * 关键词搜索帖子
     *
     * @param keyword  关键词
     * @param pageNum  页码（默认1）
     * @param pageSize 每页大小（默认10）
     * @return 分页帖子列表
     */
    @GetMapping("/keyword")
    public Result<PageResult<Post>> searchByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Search by keyword: {}, page: {}, size: {}", keyword, pageNum, pageSize);
        PageResult<Post> result = postSearchService.searchByKeyword(keyword, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 高级搜索帖子
     *
     * @param request 搜索请求参数
     * @return 分页帖子列表
     */
    @PostMapping("/advanced")
    public Result<PageResult<Post>> advancedSearch(@RequestBody PostSearchRequest request) {
        log.info("Advanced search with request: {}", request);
        PageResult<Post> result = postSearchService.advancedSearch(request);
        return Result.success(result);
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
    public Result<PageResult<Post>> searchByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Search by category: {}, page: {}, size: {}", category, pageNum, pageSize);
        PageResult<Post> result = postSearchService.searchByCategory(category, pageNum, pageSize);
        return Result.success(result);
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
    public Result<PageResult<Post>> searchByDestination(
            @RequestParam String destination,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Search by destination: {}, page: {}, size: {}", destination, pageNum, pageSize);
        PageResult<Post> result = postSearchService.searchByDestination(destination, pageNum, pageSize);
        return Result.success(result);
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
        List<String> suggestions = postSearchService.getSearchSuggestions(keyword, size);
        return Result.success(suggestions);
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
        List<String> keywords = postSearchService.getHotSearchKeywords(size);
        return Result.success(keywords);
    }
}
