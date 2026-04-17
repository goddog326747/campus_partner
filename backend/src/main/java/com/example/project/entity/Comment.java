package com.example.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论实体类
 * <p>
 * 对应评论表，存储帖子的评论和回复信息
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
@TableName("comment")
public class Comment {
    
    /** 评论ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属帖子ID */
    private Long postId;

    /** 评论用户ID */
    private Long userId;

    /** 评论内容 */
    private String content;

    /** 父评论ID，顶级评论为null，回复时为被回复评论的ID */
    private Long parentId;

    /** 点赞数量 */
    private Integer likeCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 评论者用户名（非数据库字段） */
    @TableField(exist = false)
    private String username;

    /** 评论者头像URL（非数据库字段） */
    @TableField(exist = false)
    private String avatar;

    /** 回复数量（非数据库字段） */
    @TableField(exist = false)
    private Integer replyCount;
}