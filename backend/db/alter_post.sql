USE `campus_partner`;

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 帖子表 - 添加图片和目的地字段
ALTER TABLE `post` ADD COLUMN `images` TEXT COMMENT '图片URL列表(JSON数组)' AFTER `user_id`;
ALTER TABLE `post` ADD COLUMN `destination` VARCHAR(100) COMMENT '目的地/地点' AFTER `images`;

-- 启用外键检查
SET FOREIGN_KEY_CHECKS = 1;
