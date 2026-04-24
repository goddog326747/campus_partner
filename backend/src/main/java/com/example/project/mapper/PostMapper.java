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
}
