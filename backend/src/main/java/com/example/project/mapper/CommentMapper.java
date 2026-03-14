package com.example.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.project.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    // Optional: Custom query for reply counts if needed
    @Select("SELECT COUNT(*) FROM comment WHERE parent_id = #{commentId}")
    Integer countReplies(@Param("commentId") Long commentId);
}