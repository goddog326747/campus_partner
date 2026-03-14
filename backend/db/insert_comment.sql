USE `campus_partner`;

DELETE FROM `comment`;

-- =============================================
-- 帖子1: 周末有人一起打王者吗
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(1, 11, '我也在找队友，星耀段位，可以一起', NULL, 3, DATE_SUB(NOW(), INTERVAL 1 DAY));
SET @c1_id = LAST_INSERT_ID();

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(1, 14, '我可以！晚上加个好友？', NULL, 2, DATE_SUB(NOW(), INTERVAL 20 HOUR));
SET @c2_id = LAST_INSERT_ID();

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(1, 10, '虽然我不打王者，但可以围观你们上分哈哈', NULL, 1, DATE_SUB(NOW(), INTERVAL 5 HOUR));
SET @c3_id = LAST_INSERT_ID();

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(1, 12, '什么段位？我钻石可以吗', @c1_id, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(1, 9, '可以，晚上一起', @c2_id, 0, DATE_SUB(NOW(), INTERVAL 15 MINUTE));

-- =============================================
-- 帖子2: 西安漫展有人一起吗
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(2, 12, '我也想去！你出什么角色？', NULL, 4, DATE_SUB(NOW(), INTERVAL 1 DAY));
SET @c4_id = LAST_INSERT_ID();

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(2, 15, '漫展在哪办啊？具体地址有吗', NULL, 2, DATE_SUB(NOW(), INTERVAL 18 HOUR));
SET @c5_id = LAST_INSERT_ID();

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(2, 10, '在国际会展中心，地铁可以直达', @c5_id, 1, DATE_SUB(NOW(), INTERVAL 17 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(2, 9, '我可以帮忙拍照！有相机', NULL, 3, DATE_SUB(NOW(), INTERVAL 12 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(2, 10, '出的刻晴，期待集邮！', @c4_id, 1, DATE_SUB(NOW(), INTERVAL 10 HOUR));

-- =============================================
-- 帖子3: 五一想去华山有人吗
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(3, 9, '华山我去年去过，建议带够水和吃的，山上东西贵', NULL, 5, DATE_SUB(NOW(), INTERVAL 2 DAY));
SET @c6_id = LAST_INSERT_ID();

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(3, 16, '我也想去！可以一起拼住宿', NULL, 3, DATE_SUB(NOW(), INTERVAL 1 DAY));
SET @c7_id = LAST_INSERT_ID();

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(3, 18, '体力活，我经常爬山，可以带路', NULL, 2, DATE_SUB(NOW(), INTERVAL 12 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(3, 11, '两天一夜够吗？要不要三天', @c7_id, 1, DATE_SUB(NOW(), INTERVAL 6 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(3, 13, '两天够了，第一天爬上去住一晚', @c7_id, 0, DATE_SUB(NOW(), INTERVAL 4 HOUR));

-- =============================================
-- 帖子4: 有没有一起打篮球的
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(4, 9, '我也想打球，不过我打控卫，可以组个队', NULL, 4, DATE_SUB(NOW(), INTERVAL 1 DAY));
SET @c8_id = LAST_INSERT_ID();

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(4, 13, '周末下午有空，一起？', NULL, 2, DATE_SUB(NOW(), INTERVAL 6 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(4, 11, '我也可以！身高175打后卫', NULL, 1, DATE_SUB(NOW(), INTERVAL 3 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(4, 15, '好的，周末下午北校区篮球场见', @c8_id, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- =============================================
-- 帖子5: 探店！回民街新开的火锅店
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(5, 10, '那家火锅我也去过！确实好吃，推荐点他们的鲜毛肚', NULL, 6, DATE_SUB(NOW(), INTERVAL 3 DAY));
SET @c9_id = LAST_INSERT_ID();

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(5, 12, '下次约一波！我请客', NULL, 3, DATE_SUB(NOW(), INTERVAL 2 DAY));
SET @c10_id = LAST_INSERT_ID();

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(5, 17, '哈哈昨天刚发就有人评论，确实好吃！', @c10_id, 2, DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(5, 14, '人均多少？学生党伤不起', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(5, 17, '人均80左右，性价比很高', @c10_id, 0, DATE_SUB(NOW(), INTERVAL 20 HOUR));

-- =============================================
-- 帖子6: 原神萌新求带
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(6, 13, '我可以带你，UID多少？', NULL, 3, DATE_SUB(NOW(), INTERVAL 3 HOUR));
SET @c11_id = LAST_INSERT_ID();

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(6, 9, '大佬带我一个呗，我也想玩', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(6, 14, 'UID 12345678，谢谢大佬！', @c11_id, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(6, 11, '我也在玩，可以一起', NULL, 2, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- =============================================
-- 帖子7: 毕业旅行想去青海湖
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(7, 11, '青海湖超美！我前年去过，记得带防晒', NULL, 4, DATE_SUB(NOW(), INTERVAL 5 DAY));
SET @c12_id = LAST_INSERT_ID();

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(7, 17, '毕业旅行好主意，可惜我还没毕业', NULL, 1, DATE_SUB(NOW(), INTERVAL 4 DAY));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(7, 10, '我也想去！可以一起吗', NULL, 2, DATE_SUB(NOW(), INTERVAL 3 DAY));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(7, 16, '可以，私信聊细节', @c12_id, 0, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- =============================================
-- 帖子8: 周末约饭！
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(8, 9, '小寨那家日料我知道，三文鱼很新鲜', NULL, 3, DATE_SUB(NOW(), INTERVAL 10 HOUR));
SET @c13_id = LAST_INSERT_ID();

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(8, 14, '我也想去！可以一起', NULL, 2, DATE_SUB(NOW(), INTERVAL 8 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(8, 11, '那家店叫什么名字？', NULL, 1, DATE_SUB(NOW(), INTERVAL 6 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(8, 12, '叫"樱花日料"，在小寨赛格旁边', @c13_id, 0, DATE_SUB(NOW(), INTERVAL 5 HOUR));

-- =============================================
-- 帖子9: 组队开黑！英雄联盟
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(9, 10, '我不打LOL，但可以给你们加油', NULL, 1, DATE_SUB(NOW(), INTERVAL 6 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(9, 15, '白银可以吗？想上黄金', NULL, 2, DATE_SUB(NOW(), INTERVAL 4 HOUR));
SET @c14_id = LAST_INSERT_ID();

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(9, 11, '可以，一起排', @c14_id, 0, DATE_SUB(NOW(), INTERVAL 3 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(9, 13, '我打野，可以来', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- =============================================
-- 帖子10: 秦岭徒步有人吗
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(10, 9, '秦岭哪条线？我也经常徒步', NULL, 3, DATE_SUB(NOW(), INTERVAL 1 DAY));
SET @c15_id = LAST_INSERT_ID();

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(10, 16, '体力活，我可能不行', NULL, 1, DATE_SUB(NOW(), INTERVAL 18 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(10, 18, '走嘉午台那条线，中等难度', @c15_id, 0, DATE_SUB(NOW(), INTERVAL 12 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(10, 11, '我可以！有经验吗', NULL, 2, DATE_SUB(NOW(), INTERVAL 6 HOUR));

-- =============================================
-- 帖子11: 西安城墙骑行
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(11, 11, '城墙骑行很浪漫的，适合情侣', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(11, 13, '傍晚去正好，不晒', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(11, 9, '我也想去！可以一起', NULL, 1, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- =============================================
-- 帖子12: 有没有一起健身的
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(12, 15, '健身房我也办了卡，可以一起练', NULL, 2, DATE_SUB(NOW(), INTERVAL 12 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(12, 13, '我也想增肌，可以互相监督', NULL, 1, DATE_SUB(NOW(), INTERVAL 6 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(12, 9, '好的，周末约起来', NULL, 0, DATE_SUB(NOW(), INTERVAL 3 HOUR));

-- =============================================
-- 帖子13: 大雁塔音乐喷泉
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(13, 10, '音乐喷泉超好看！建议早点去占位置', NULL, 4, DATE_SUB(NOW(), INTERVAL 5 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(13, 14, '晚上8点那场最漂亮', NULL, 2, DATE_SUB(NOW(), INTERVAL 4 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(13, 11, '我也想去！一起占位置', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- =============================================
-- 帖子14: 永劫无间找人组队
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(14, 11, '永劫无间我也在玩，不过比较菜', NULL, 1, DATE_SUB(NOW(), INTERVAL 3 DAY));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(14, 14, '我可以！什么段位都行', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(14, 13, '黄金段位，可以一起排', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =============================================
-- 帖子15: 大唐不夜城夜游
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(15, 9, '大唐不夜城夜景绝了，拍照很出片', NULL, 5, DATE_SUB(NOW(), INTERVAL 8 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(15, 12, '我也想去！可以一起拍照', NULL, 3, DATE_SUB(NOW(), INTERVAL 6 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(15, 10, '不倒翁小姐姐必打卡', NULL, 2, DATE_SUB(NOW(), INTERVAL 4 HOUR));

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(15, 14, '好的，今晚去？', NULL, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR));
