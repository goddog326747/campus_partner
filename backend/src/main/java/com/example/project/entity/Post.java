package com.example.project.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String content;
    
    private String category;
    
    private Long userId;
    
    @TableField(exist = false) // 数据库中没有这个字段，需要关联查询或者存入时冗余，这里暂时假设存入时冗余或者前端不展示
    private String username;

    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
