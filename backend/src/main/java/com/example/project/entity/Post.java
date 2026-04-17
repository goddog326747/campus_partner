package com.example.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子实体类
 * <p>
 * 对应帖子表，存储帖子内容和关联的用户信息
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
@TableName("post")
public class Post {
    
    /** 帖子ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 帖子标题 */
    private String title;
    
    /** 帖子内容 */
    private String content;
    
    /** 帖子分类 */
    private String category;
    
    /** 发布用户ID */
    private Long userId;
    
    /** 图片URL列表，JSON数组格式 */
    private String images;
    
    /** 目的地 */
    private String destination;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    /** 发布者用户名（非数据库字段） */
    @TableField(exist = false)
    private String username;
    
    /** 发布者头像（非数据库字段） */
    @TableField(exist = false)
    private String avatar;
    
    /** 发布者所在地（非数据库字段） */
    @TableField(exist = false)
    private String userLocation;
    
    /** 发布者学校（非数据库字段） */
    @TableField(exist = false)
    private String userSchool;
    
    /** 发布者认证状态（非数据库字段） */
    @TableField(exist = false)
    private Integer userVerified;
    
    /** 发布者性别（非数据库字段） */
    @TableField(exist = false)
    private Integer userGender;
}
