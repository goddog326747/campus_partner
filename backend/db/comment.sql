USE `campus_partner`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
    `user_id` bigint(20) NOT NULL COMMENT '评论者ID',
    `content` text NOT NULL COMMENT '评论内容',
    `parent_id` bigint(20) DEFAULT NULL COMMENT '父评论ID（用于回复功能，NULL表示顶级评论）',
    `like_count` int(11) DEFAULT 0 COMMENT '点赞数',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(1, 3, '我也在找队友，星耀段位，可以一起', NULL, 3, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 6, '我可以！晚上加个好友？', NULL, 2, DATE_SUB(NOW(), INTERVAL 20 HOUR)),
(1, 2, '虽然我不打王者，但可以围观你们上分哈哈', NULL, 1, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(2, 4, '我也想去！你出什么角色？', NULL, 4, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 7, '漫展在哪办啊？具体地址有吗', NULL, 2, DATE_SUB(NOW(), INTERVAL 18 HOUR)),
(2, 2, '在国际会展中心，地铁可以直达', 7, 1, DATE_SUB(NOW(), INTERVAL 17 HOUR)),
(3, 1, '华山我去年去过，建议带够水和吃的，山上东西贵', NULL, 5, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 8, '我也想去！可以一起拼住宿', NULL, 3, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 10, '体力活，我经常爬山，可以带路', NULL, 2, DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(4, 1, '我也想打球，不过我打控卫，可以组个队', NULL, 4, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 5, '周末下午有空，一起？', NULL, 2, DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(5, 2, '那家火锅我也去过！确实好吃，推荐点他们的鲜毛肚', NULL, 6, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 4, '下次约一波！我请客', NULL, 3, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 9, '哈哈昨天刚发就有人评论，确实好吃！', 4, 2, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(6, 5, '我可以带你，UID多少？', NULL, 3, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(6, 1, '大佬带我一个呗，我也想玩', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(7, 3, '青海湖超美！我前年去过，记得带防晒', NULL, 4, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 9, '毕业旅行好主意，可惜我还没毕业', NULL, 1, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(8, 1, '小寨那家日料我知道，三文鱼很新鲜', NULL, 3, DATE_SUB(NOW(), INTERVAL 10 HOUR)),
(8, 6, '我也想去！可以一起', NULL, 2, DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(9, 2, '我不打LOL，但可以给你们加油', NULL, 1, DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(9, 7, '白银可以吗？想上黄金', NULL, 2, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(10, 1, '秦岭哪条线？我也经常徒步', NULL, 3, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(10, 8, '体力活，我可能不行', NULL, 1, DATE_SUB(NOW(), INTERVAL 18 HOUR)),
(11, 3, '城墙骑行很浪漫的，适合情侣', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(11, 5, '傍晚去正好，不晒', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(12, 7, '健身房我也办了卡，可以一起练', NULL, 2, DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(13, 2, '音乐喷泉超好看！建议早点去占位置', NULL, 4, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(13, 6, '晚上8点那场最漂亮', NULL, 2, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(14, 3, '永劫无间我也在玩，不过比较菜', NULL, 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(15, 1, '大唐不夜城夜景绝了，拍照很出片', NULL, 5, DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(15, 4, '我也想去！可以一起拍照', NULL, 3, DATE_SUB(NOW(), INTERVAL 6 HOUR));

SET FOREIGN_KEY_CHECKS = 1;
