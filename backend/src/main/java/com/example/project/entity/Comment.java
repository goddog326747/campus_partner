package com.example.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private Long userId;

    private String content;

    private Long parentId; // NULL for top-level comments, non-NULL for replies

    private Integer likeCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // Transient fields for API responses (not in database)
    @TableField(exist = false)
    private String username; // Commenter's display name (nickname or username)

    @TableField(exist = false)
    private String avatar; // Commenter's avatar URL

    @TableField(exist = false)
    private Integer replyCount; // Number of replies to this comment
}