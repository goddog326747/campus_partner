package com.example.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String content;
    
    private String category;
    
    private Long userId;
    
    private String images;
    
    private String destination;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String username;
    
    @TableField(exist = false)
    private String avatar;
    
    @TableField(exist = false)
    private String userLocation;
    
    @TableField(exist = false)
    private String userSchool;
    
    @TableField(exist = false)
    private Integer userVerified;
    
    @TableField(exist = false)
    private Integer userGender;
}
