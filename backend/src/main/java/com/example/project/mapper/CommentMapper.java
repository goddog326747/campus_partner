package com.example.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.project.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论数据访问接口
 * <p>
 * 提供评论表的数据访问操作，继承MyBatis-Plus的BaseMapper接口
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 根据帖子ID查询评论列表
     *
     * @param postId 帖子ID
     * @return 评论列表
     */
    List<Comment> selectByPostId(@Param("postId") Long postId);

    /**
     * 根据父评论ID查询回复列表
     *
     * @param parentId 父评论ID
     * @return 回复列表
     */
    List<Comment> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 统计指定评论的回复数量
     *
     * @param commentId 评论ID
     * @return 回复数量
     */
    Integer countReplies(@Param("commentId") Long commentId);

    /**
     * 根据帖子ID统计评论数量
     *
     * @param postId 帖子ID
     * @return 评论数量
     */
    Integer selectCountByPostId(@Param("postId") Long postId);
}
