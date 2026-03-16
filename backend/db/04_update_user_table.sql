USE `campus_partner`;

-- 更新用户表，添加社交软件常见字段
ALTER TABLE `user` ADD COLUMN `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女' AFTER `avatar`;
ALTER TABLE `user` ADD COLUMN `birthday` DATE DEFAULT NULL COMMENT '生日' AFTER `gender`;
ALTER TABLE `user` ADD COLUMN `bio` VARCHAR(500) DEFAULT NULL COMMENT '个人简介' AFTER `birthday`;
ALTER TABLE `user` ADD COLUMN `location` VARCHAR(100) DEFAULT NULL COMMENT '所在地' AFTER `bio`;
ALTER TABLE `user` ADD COLUMN `school` VARCHAR(100) DEFAULT NULL COMMENT '学校' AFTER `location`;
ALTER TABLE `user` ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号' AFTER `school`;
ALTER TABLE `user` ADD COLUMN `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱' AFTER `phone`;
ALTER TABLE `user` ADD COLUMN `wechat` VARCHAR(50) DEFAULT NULL COMMENT '微信号' AFTER `email`;
ALTER TABLE `user` ADD COLUMN `qq` VARCHAR(20) DEFAULT NULL COMMENT 'QQ号' AFTER `wechat`;
ALTER TABLE `user` ADD COLUMN `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常' AFTER `qq`;
ALTER TABLE `user` ADD COLUMN `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间' AFTER `status`;

-- 隐私设置字段
ALTER TABLE `user` ADD COLUMN `privacy_profile` TINYINT DEFAULT 0 COMMENT '资料可见性: 0-公开, 1-仅关注可见, 2-完全私密' AFTER `last_login_time`;
ALTER TABLE `user` ADD COLUMN `privacy_contact` TINYINT DEFAULT 1 COMMENT '联系方式可见性: 0-公开, 1-仅关注可见, 2-完全私密' AFTER `privacy_profile`;

-- 添加索引
ALTER TABLE `user` ADD INDEX `idx_phone` (`phone`);
ALTER TABLE `user` ADD INDEX `idx_email` (`email`);
