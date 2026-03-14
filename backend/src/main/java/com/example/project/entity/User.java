package com.example.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;
    private String bio;
    private String location;
    private String school;
    private String schoolEmail;
    private Integer verified;
    private LocalDateTime verifyTime;
    private String phone;
    private String email;
    private String wechat;
    private String qq;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private Integer privacyProfile;
    private Integer privacyContact;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
