package com.example.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.project.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子数据访问接口
 * <p>
 * 提供帖子表的数据访问操作，继承MyBatis-Plus的BaseMapper接口
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 根据条件查询帖子列表
     *
     * @param category 分类
     * @param keyword  关键词
     * @return 帖子列表
     */
    List<Post> selectByCondition(@Param("category") String category, @Param("keyword") String keyword);

    /**
     * 根据用户ID查询帖子
     *
     * @param userId 用户ID
     * @return 帖子列表
     */
    List<Post> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询指定时间之后更新的帖子（用于增量同步）
     *
     * @param lastSyncTime 上次同步时间
     * @return 帖子列表
     */
    List<Post> selectByUpdateTimeAfter(@Param("lastSyncTime") String lastSyncTime);

    /**
     * 根据完整条件查询帖子列表（支持用户属性过滤）
     *
     * @param category 分类
     * @param keyword  关键词
     * @param location 用户所在地
     * @param school   用户学校
     * @param verified 是否认证（2=已认证）
     * @param gender   性别（0=女，1=男）
     * @param offset   分页偏移量
     * @param pageSize 每页大小
     * @return 帖子列表
     */
    List<Post> selectByConditionWithFilters(@Param("category") String category,
                                             @Param("keyword") String keyword,
                                             @Param("location") String location,
                                             @Param("school") String school,
                                             @Param("verified") Integer verified,
                                             @Param("gender") Integer gender,
                                             @Param("offset") int offset,
                                             @Param("pageSize") int pageSize);

    /**
     * 根据完整条件查询帖子总数
     *
     * @param category 分类
     * @param keyword  关键词
     * @param location 用户所在地
     * @param school   用户学校
     * @param verified 是否认证
     * @param gender   性别
     * @return 总数
     */
    long countByConditionWithFilters(@Param("category") String category,
                                      @Param("keyword") String keyword,
                                      @Param("location") String location,
                                      @Param("school") String school,
                                      @Param("verified") Integer verified,
                                      @Param("gender") Integer gender);

    List<Post> selectPostsWithUser(@Param("category") String category,
                                    @Param("keyword") String keyword,
                                    @Param("offset") int offset,
                                    @Param("pageSize") int pageSize);

    long countPosts(@Param("category") String category,
                    @Param("keyword") String keyword);
}
