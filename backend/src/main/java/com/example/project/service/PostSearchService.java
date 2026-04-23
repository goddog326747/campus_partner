package com.example.project.service;

import com.example.project.document.PostDocument;
import com.example.project.dto.PostSearchRequest;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 帖子搜索服务接口
 * <p>
 * 提供基于 Elasticsearch 的帖子搜索功能
 * </p>
 *
 * @author system
 * @since 1.0
 */
public interface PostSearchService {

    /**
     * 关键词搜索帖子
     *
     * @param keyword  关键词
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页帖子文档
     */
    Page<PostDocument> searchByKeyword(String keyword, int pageNum, int pageSize);

    /**
     * 高级搜索帖子
     *
     * @param request 搜索请求
     * @return 分页帖子文档
     */
    Page<PostDocument> advancedSearch(PostSearchRequest request);

    /**
     * 根据分类搜索帖子
     *
     * @param category 分类
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页帖子文档
     */
    Page<PostDocument> searchByCategory(String category, int pageNum, int pageSize);

    /**
     * 根据目的地搜索帖子
     *
     * @param destination 目的地
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @return 分页帖子文档
     */
    Page<PostDocument> searchByDestination(String destination, int pageNum, int pageSize);

    /**
     * 获取搜索建议
     *
     * @param keyword 关键词前缀
     * @param size    建议数量
     * @return 建议列表
     */
    List<String> getSearchSuggestions(String keyword, int size);

    /**
     * 获取热门搜索词
     *
     * @param size 数量
     * @return 热门搜索词列表
     */
    List<String> getHotSearchKeywords(int size);
}
