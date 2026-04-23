package com.example.project.elasticsearch.repository;

import com.example.project.elasticsearch.document.PostDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 帖子搜索 Repository 接口
 * <p>
 * 提供 Elasticsearch 帖子文档的增删改查和搜索功能
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Repository
public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, Long> {

    /**
     * 根据用户ID查询帖子
     *
     * @param userId 用户ID
     * @return 帖子文档列表
     */
    List<PostDocument> findByUserId(Long userId);

    /**
     * 根据分类查询帖子
     *
     * @param category 分类
     * @param pageable 分页参数
     * @return 分页帖子文档
     */
    Page<PostDocument> findByCategory(String category, Pageable pageable);

    /**
     * 根据目的地查询帖子
     *
     * @param destination 目的地
     * @param pageable 分页参数
     * @return 分页帖子文档
     */
    Page<PostDocument> findByDestination(String destination, Pageable pageable);

    /**
     * 多字段搜索（标题和内容）
     *
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 分页帖子文档
     */
    @Query("{" +
            "  \"multi_match\": {" +
            "    \"query\": \"?0\"," +
            "    \"fields\": [\"title^3\", \"content\", \"destination^2\"]," +
            "    \"type\": \"best_fields\"," +
            "    \"fuzziness\": \"AUTO\"" +
            "  }" +
            "}")
    Page<PostDocument> searchByKeyword(String keyword, Pageable pageable);

    /**
     * 高级搜索（带筛选条件）
     *
     * @param keyword 关键词
     * @param category 分类
     * @param userLocation 用户位置
     * @param userSchool 用户学校
     * @param userVerified 认证状态
     * @param userGender 性别
     * @param pageable 分页参数
     * @return 分页帖子文档
     */
    @Query("{" +
            "  \"bool\": {" +
            "    \"must\": [" +
            "      {\"multi_match\": {" +
            "        \"query\": \"?0\"," +
            "        \"fields\": [\"title^3\", \"content\", \"destination^2\"]," +
            "        \"type\": \"best_fields\"" +
            "      }}" +
            "    ]," +
            "    \"filter\": [" +
            "      {\"term\": {\"category\": \"?1\"}}," +
            "      {\"term\": {\"userLocation\": \"?2\"}}," +
            "      {\"term\": {\"userSchool\": \"?3\"}}," +
            "      {\"term\": {\"userVerified\": \"?4\"}}," +
            "      {\"term\": {\"userGender\": \"?5\"}}" +
            "    ]" +
            "  }" +
            "}")
    Page<PostDocument> advancedSearch(String keyword, String category, String userLocation, 
                                       String userSchool, Integer userVerified, Integer userGender, 
                                       Pageable pageable);
}
