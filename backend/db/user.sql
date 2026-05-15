USE `campus_partner`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMM ENT '主键ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(100) NOT NULL COMMENT '密码',
    `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
    `avatar` TEXT DEFAULT NULL COMMENT '头像URL或Base64数据',
    `gender` tinyint DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
    `birthday` date DEFAULT NULL COMMENT '生日',
    `bio` varchar(500) DEFAULT NULL COMMENT '个人简介',
    `location` varchar(100) DEFAULT NULL COMMENT '所在地',
    `school` varchar(100) DEFAULT NULL COMMENT '学校',
    `school_email` varchar(100) DEFAULT NULL COMMENT '学校邮箱(用于认证)',
    `verified` tinyint DEFAULT 0 COMMENT '认证状态: 0-未认证, 1-认证中, 2-已认证',
    `verify_time` datetime DEFAULT NULL COMMENT '认证时间',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `wechat` varchar(50) DEFAULT NULL COMMENT '微信号',
    `qq` varchar(20) DEFAULT NULL COMMENT 'QQ号',
    `status` tinyint DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `privacy_profile` tinyint DEFAULT 0 COMMENT '资料可见性: 0-公开, 1-仅关注可见, 2-完全私密',
    `privacy_contact` tinyint DEFAULT 1 COMMENT '联系方式可见性: 0-公开, 1-仅关注可见, 2-完全私密',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_email` (`email`),
    KEY `idx_school_email` (`school_email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

SET FOREIGN_KEY_CHECKS = 1;
