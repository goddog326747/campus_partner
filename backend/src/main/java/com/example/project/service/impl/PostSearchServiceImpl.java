package com.example.project.service.impl;

import com.example.project.common.PageResult;
import com.example.project.dto.PostSearchRequest;
import com.example.project.elasticsearch.document.PostDocument;
import com.example.project.elasticsearch.repository.PostSearchRepository;
import com.example.project.entity.Post;
import com.example.project.service.PostSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 帖子搜索服务实现类（Elasticsearch 版本）
 * <p>
 * 实现基于 Elasticsearch 的帖子搜索功能，前端采用此实现
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostSearchServiceImpl implements PostSearchService {

    private final PostSearchRepository postSearchRepository;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public PageResult<Post> searchByKeyword(String keyword, int pageNum, int pageSize) {
        log.info("Searching posts by keyword: {}, page: {}, size: {}", keyword, pageNum, pageSize);
        try {
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
            Page<PostDocument> result = postSearchRepository.searchByKeyword(keyword, pageable);
            log.info("Found {} posts for keyword: {}", result.getTotalElements(), keyword);
            return convertToPageResult(result, pageNum, pageSize);
        } catch (Exception e) {
            log.error("Error searching posts by keyword: {}", keyword, e);
            throw e;
        }
    }

    @Override
    public PageResult<Post> advancedSearch(PostSearchRequest request) {
        log.info("Advanced search with request: {}", request);
        try {
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            if (StringUtils.hasText(request.getKeyword())) {
                boolQuery.must(QueryBuilders.multiMatchQuery(request.getKeyword())
                        .field("title", 3.0f)
                        .field("content")
                        .field("destination", 2.0f)
                        .type("best_fields")
                        .fuzziness("AUTO"));
            }

            if (StringUtils.hasText(request.getCategory())) {
                boolQuery.filter(QueryBuilders.termQuery("category", request.getCategory()));
            }

            if (StringUtils.hasText(request.getLocation())) {
                boolQuery.filter(QueryBuilders.termQuery("userLocation", request.getLocation()));
            }

            if (StringUtils.hasText(request.getSchool())) {
                boolQuery.filter(QueryBuilders.termQuery("userSchool", request.getSchool()));
            }

            if (request.getVerified() != null) {
                boolQuery.filter(QueryBuilders.termQuery("userVerified", request.getVerified() ? 1 : 0));
            }

            if (request.getGender() != null) {
                boolQuery.filter(QueryBuilders.termQuery("userGender", request.getGender()));
            }

            if (StringUtils.hasText(request.getDestination())) {
                boolQuery.filter(QueryBuilders.matchQuery("destination", request.getDestination()));
            }

            SortOrder sortOrder = "asc".equalsIgnoreCase(request.getSortOrder()) ? SortOrder.ASC : SortOrder.DESC;
            String sortField = request.getSortField();
            if (!StringUtils.hasText(sortField)) {
                sortField = "createTime";
            }

            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withPageable(PageRequest.of(request.getPageNum() - 1, request.getPageSize()))
                    .withSort(SortBuilders.fieldSort(sortField).order(sortOrder));

            NativeSearchQuery searchQuery = queryBuilder.build();
            SearchHits<PostDocument> searchHits = elasticsearchRestTemplate.search(searchQuery, PostDocument.class);

            List<PostDocument> documents = searchHits.getSearchHits().stream()
                    .map(hit -> hit.getContent())
                    .collect(Collectors.toList());

            long totalHits = searchHits.getTotalHits();
            Pageable pageable = PageRequest.of(request.getPageNum() - 1, request.getPageSize());

            log.info("Advanced search found {} posts", totalHits);
            return convertDocumentsToPageResult(documents, pageable, totalHits, request.getPageNum(), request.getPageSize());

        } catch (Exception e) {
            log.error("Error in advanced search", e);
            throw e;
        }
    }

    @Override
    public PageResult<Post> searchByCategory(String category, int pageNum, int pageSize) {
        log.info("Searching posts by category: {}, page: {}, size: {}", category, pageNum, pageSize);
        try {
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
            Page<PostDocument> result = postSearchRepository.findByCategory(category, pageable);
            log.info("Found {} posts for category: {}", result.getTotalElements(), category);
            return convertToPageResult(result, pageNum, pageSize);
        } catch (Exception e) {
            log.error("Error searching posts by category: {}", category, e);
            throw e;
        }
    }

    @Override
    public PageResult<Post> searchByDestination(String destination, int pageNum, int pageSize) {
        log.info("Searching posts by destination: {}, page: {}, size: {}", destination, pageNum, pageSize);
        try {
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
            Page<PostDocument> result = postSearchRepository.findByDestination(destination, pageable);
            log.info("Found {} posts for destination: {}", result.getTotalElements(), destination);
            return convertToPageResult(result, pageNum, pageSize);
        } catch (Exception e) {
            log.error("Error searching posts by destination: {}", destination, e);
            throw e;
        }
    }

    @Override
    public List<String> getSearchSuggestions(String keyword, int size) {
        log.info("Getting search suggestions for keyword: {}, size: {}", keyword, size);
        try {
            if (!StringUtils.hasText(keyword)) {
                return new ArrayList<>();
            }

            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                    .should(QueryBuilders.prefixQuery("title", keyword))
                    .should(QueryBuilders.prefixQuery("destination", keyword));

            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withPageable(PageRequest.of(0, size))
                    .build();

            SearchHits<PostDocument> searchHits = elasticsearchRestTemplate.search(searchQuery, PostDocument.class);

            List<String> suggestions = searchHits.getSearchHits().stream()
                    .map(hit -> hit.getContent().getTitle())
                    .distinct()
                    .limit(size)
                    .collect(Collectors.toList());

            log.info("Found {} search suggestions", suggestions.size());
            return suggestions;

        } catch (Exception e) {
            log.error("Error getting search suggestions", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getHotSearchKeywords(int size) {
        log.info("Getting hot search keywords, size: {}", size);
        return new ArrayList<>();
    }

    /**
     * 将 ES 分页结果转换为业务分页结果
     */
    private PageResult<Post> convertToPageResult(Page<PostDocument> page, int pageNum, int pageSize) {
        List<Post> posts = page.getContent().stream()
                .map(this::convertToPost)
                .collect(Collectors.toList());
        return PageResult.of(posts, page.getTotalElements(), pageNum, pageSize);
    }

    /**
     * 将 ES 文档列表转换为业务分页结果
     */
    private PageResult<Post> convertDocumentsToPageResult(List<PostDocument> documents, Pageable pageable, long total, int pageNum, int pageSize) {
        List<Post> posts = documents.stream()
                .map(this::convertToPost)
                .collect(Collectors.toList());
        return PageResult.of(posts, total, pageNum, pageSize);
    }

    /**
     * 将 PostDocument 转换为 Post 实体
     */
    private Post convertToPost(PostDocument document) {
        Post post = new Post();
        post.setId(document.getId());
        post.setTitle(document.getTitle());
        post.setContent(document.getContent());
        post.setCategory(document.getCategory());
        post.setUserId(document.getUserId());
        post.setDestination(document.getDestination());
        post.setCreateTime(document.getCreateTime());
        post.setUpdateTime(document.getUpdateTime());
        post.setImages(document.getImages() != null ? String.join(",", document.getImages()) : null);
        post.setUsername(document.getUsername());
        post.setAvatar(document.getAvatar());
        post.setUserLocation(document.getUserLocation());
        post.setUserSchool(document.getUserSchool());
        post.setUserVerified(document.getUserVerified());
        post.setUserGender(document.getUserGender());
        return post;
    }
}
