package com.example.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.project.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
     * 统计指定评论的回复数量
     *
     * @param commentId 评论ID
     * @return 回复数量
     */
    @Select("SELECT COUNT(*) FROM comment WHERE parent_id = #{commentId}")
    Integer countReplies(@Param("commentId") Long commentId);
}