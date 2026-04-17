package com.example.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * <p>
 * 对应用户表，存储用户基本信息和认证信息
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
@TableName("user")
public class User {
    
    /** 用户ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户名，登录账号 */
    private String username;
    /** 密码，加密存储 */
    private String password;
    /** 昵称，显示名称 */
    private String nickname;
    /** 头像URL */
    private String avatar;
    /** 性别：0-未知，1-男，2-女 */
    private Integer gender;
    /** 生日 */
    private LocalDate birthday;
    /** 个人简介 */
    private String bio;
    /** 所在地 */
    private String location;
    /** 学校 */
    private String school;
    /** 学校邮箱 */
    private String schoolEmail;
    /** 认证状态：0-未认证，1-已认证 */
    private Integer verified;
    /** 认证时间 */
    private LocalDateTime verifyTime;
    /** 手机号 */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 微信号 */
    private String wechat;
    /** QQ号 */
    private String qq;
    /** 账号状态：0-禁用，1-正常 */
    private Integer status;
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
    /** 个人资料隐私设置 */
    private Integer privacyProfile;
    /** 联系方式隐私设置 */
    private Integer privacyContact;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
