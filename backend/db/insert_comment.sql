USE `campus_partner`;

DELETE FROM `comment`;
ALTER TABLE `comment` AUTO_INCREMENT = 1;

-- =============================================
-- 帖子1: 周末有人一起打王者吗 (游戏组队)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(1, 11, '我也在找队友，星耀段位，可以一起', NULL, 3, DATE_SUB(NOW(), INTERVAL 1 DAY));
SET @c1_id = LAST_INSERT_ID();
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(1, 14, '我可以！晚上加个好友？', NULL, 2, DATE_SUB(NOW(), INTERVAL 20 HOUR)),
(1, 10, '虽然我不打王者，但可以围观你们上分哈哈', NULL, 1, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(1, 12, '什么段位？我钻石可以吗', @c1_id, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(1, 9, '可以，晚上一起', @c1_id, 0, DATE_SUB(NOW(), INTERVAL 15 MINUTE));

-- =============================================
-- 帖子2: 街霸6找人对练 (游戏组队)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(2, 13, '我也在玩街霸6！主玩肯，可以一起练', NULL, 3, DATE_SUB(NOW(), INTERVAL 3 HOUR));
SET @c2_id = LAST_INSERT_ID();
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(2, 9, '格斗游戏苦手，但想围观学习', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, 14, '可以！我一般在南校区活动室，周末都可以', @c2_id, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 11, '我也买了摇杆，一起进步', NULL, 2, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- =============================================
-- 帖子3: 组队开黑！英雄联盟 (游戏组队)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(3, 10, '我不打LOL，但可以给你们加油', NULL, 1, DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(3, 15, '白银可以吗？想上黄金', NULL, 2, DATE_SUB(NOW(), INTERVAL 4 HOUR));
SET @c3_id = LAST_INSERT_ID();
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(3, 11, '可以，一起排', @c3_id, 0, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(3, 13, '我打野，可以来', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- =============================================
-- 帖子4: 永劫无间找人组队 (游戏组队)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(4, 11, '永劫无间我也在玩，不过比较菜', NULL, 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 14, '我可以！什么段位都行', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 13, '黄金段位，可以一起排', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =============================================
-- 帖子5: CS2找队友一起上分 (游戏组队)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(5, 16, '我A-，可以一起', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(5, 10, '完美B，主玩狙击位，要不', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(5, 18, '我也在玩，晚上可以一起', NULL, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- =============================================
-- 帖子6: 和平精英四排缺人 (游戏组队)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(6, 17, '我kd4.0，可以带飞', NULL, 3, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(6, 11, '我意识还行，枪法一般，要不', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(6, 15, '有固定时间吗？我周末可以', NULL, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- =============================================
-- 帖子7: 洛克王国世界牵手抓宠物 (游戏组队)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(7, 14, '我也在玩！抓到什么稀有宠物了吗', NULL, 2, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(7, 9, '我抓了只火神，可以交换', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- =============================================
-- 帖子8: DOTA2开黑5=1 (游戏组队)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(8, 16, '我玩辅助，传奇段位', NULL, 2, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(8, 10, '我不玩DOTA2，祝你们开黑愉快', NULL, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(8, 18, '我可以！什么时间', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子9: 金铲铲之战双排 (游戏组队)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(9, 11, '我大师，可以一起', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(9, 13, '我钻石，不嫌弃的话可以一起', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子10: 瓦罗兰特找固定队 (游戏组队)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(10, 15, '我港服铂金，主玩烟位', NULL, 2, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(10, 17, '我钻石，主玩哨位，可以一起', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(10, 12, '我不玩瓦，但帮顶', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子11: 西安城墙骑行约伴 (出游搭子)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(11, 11, '城墙骑行很浪漫的，适合情侣', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(11, 13, '傍晚去正好，不晒', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(11, 9, '我也想去！可以一起', NULL, 1, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- =============================================
-- 帖子12: 大雁塔音乐喷泉约看 (出游搭子)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(12, 10, '音乐喷泉超好看！建议早点去占位置', NULL, 4, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(12, 14, '晚上8点那场最漂亮', NULL, 2, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(12, 11, '我也想去！一起占位置', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- =============================================
-- 帖子13: 大唐不夜城夜游 (出游搭子)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(13, 9, '大唐不夜城夜景绝了，拍照很出片', NULL, 5, DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(13, 12, '我也想去！可以一起拍照', NULL, 3, DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(13, 10, '不倒翁小姐姐必打卡', NULL, 2, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(13, 14, '好的，今晚去？', NULL, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- =============================================
-- 帖子14: 周末约饭！小寨日料 (出游搭子)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(14, 9, '小寨那家日料我知道，三文鱼很新鲜', NULL, 3, DATE_SUB(NOW(), INTERVAL 10 HOUR));
SET @c14_id = LAST_INSERT_ID();
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(14, 14, '我也想去！可以一起', NULL, 2, DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(14, 11, '那家店叫什么名字？', NULL, 1, DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(14, 12, '叫"樱花日料"，在小寨赛格旁边', @c14_id, 0, DATE_SUB(NOW(), INTERVAL 5 HOUR));

-- =============================================
-- 帖子15: 回民街探店约起 (出游搭子)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(15, 10, '回民街哪家烤肉？求店名', NULL, 2, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(15, 17, '叫"老马家烤肉"，味道不错', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(15, 13, '我也想去！周末一起', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子16: 曲江池遗址公园散步 (出游搭子)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(16, 11, '曲江池风景确实不错', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(16, 15, '我也想去散步，一起吗', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子17: 赛格逛街搭子 (出游搭子)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(17, 13, '我也想去逛街！可以一起', NULL, 2, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(17, 16, '赛格最近有什么活动吗', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- =============================================
-- 帖子18: 永兴坊美食打卡 (出游搭子)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(18, 9, '永兴坊的摔碗酒很有名', NULL, 2, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(18, 14, '我也想去！一起', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(18, 11, '什么时候去？周末可以吗', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子19: 南湖公园野餐 (出游搭子)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(19, 12, '野餐好主意！我带水果', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(19, 10, '我可以带桌游', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子20: 钟楼鼓楼夜景拍照 (出游搭子)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(20, 15, '我有相机！可以一起', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(20, 13, '钟楼夜景确实好看', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(20, 9, '什么时候去？这周末可以吗', NULL, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- =============================================
-- 帖子21: 五一想去华山有人吗 (旅行远游)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(21, 9, '华山我去年去过，建议带够水和吃的，山上东西贵', NULL, 5, DATE_SUB(NOW(), INTERVAL 2 DAY));
SET @c21_id = LAST_INSERT_ID();
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(21, 16, '我也想去！可以一起拼住宿', NULL, 3, DATE_SUB(NOW(), INTERVAL 1 DAY));
SET @c21b_id = LAST_INSERT_ID();
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(21, 18, '体力活，我经常爬山，可以带路', NULL, 2, DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(21, 11, '两天一夜够吗？要不要三天', @c21b_id, 1, DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(21, 13, '两天够了，第一天爬上去住一晚', @c21b_id, 0, DATE_SUB(NOW(), INTERVAL 4 HOUR));

-- =============================================
-- 帖子22: 毕业旅行想去青海湖 (旅行远游)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(22, 11, '青海湖超美！我前年去过，记得带防晒', NULL, 4, DATE_SUB(NOW(), INTERVAL 5 DAY));
SET @c22_id = LAST_INSERT_ID();
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(22, 17, '毕业旅行好主意，可惜我还没毕业', NULL, 1, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(22, 10, '我也想去！可以一起吗', NULL, 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(22, 16, '可以，私信聊细节', @c22_id, 0, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- =============================================
-- 帖子23: 秦岭徒步有人吗 (旅行远游)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(23, 9, '秦岭哪条线？我也经常徒步', NULL, 3, DATE_SUB(NOW(), INTERVAL 1 DAY));
SET @c23_id = LAST_INSERT_ID();
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(23, 16, '体力活，我可能不行', NULL, 1, DATE_SUB(NOW(), INTERVAL 18 HOUR)),
(23, 18, '走嘉午台那条线，中等难度', @c23_id, 0, DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(23, 11, '我可以！有经验吗', NULL, 2, DATE_SUB(NOW(), INTERVAL 6 HOUR));

-- =============================================
-- 帖子24: 国庆假期川西自驾 (旅行远游)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(24, 12, '我会开车！可以一起', NULL, 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(24, 14, '川西风景绝美，我也想去', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(24, 17, '费用大概多少', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =============================================
-- 帖子25: 周末太白山一日游 (旅行远游)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(25, 10, '太白山风景不错，我去过', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(25, 15, '当天往返时间够吗', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子26: 暑假西藏行找同伴 (旅行远游)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(26, 11, '西藏是我梦想的地方！', NULL, 3, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(26, 18, '我去过西藏，可以分享经验', NULL, 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(26, 13, '15天够玩吗', NULL, 0, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- =============================================
-- 帖子27: 敦煌莫高窟+鸣沙山 (旅行远游)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(27, 14, '莫高窟一定要提前订票', NULL, 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(27, 9, '鸣沙山月牙泉超美', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- =============================================
-- 帖子28: 云南大理丽江7日游 (旅行远游)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(28, 12, '洱海骑行超舒服', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(28, 16, '玉龙雪山要穿厚点', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(28, 10, '我也想去！可以一起', NULL, 0, DATE_SUB(NOW(), INTERVAL 12 HOUR));

-- =============================================
-- 帖子29: 张家界+凤凰古城 (旅行远游)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(29, 11, '张家界玻璃桥很刺激', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(29, 15, '凤凰古城夜景不错', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =============================================
-- 帖子30: winter哈尔滨冰雪大世界 (旅行远游)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(30, 13, '南方人表示很想去', NULL, 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(30, 17, '哈尔滨冬天超冷，多穿点', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(30, 9, '冰雪大世界什么时候开', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =============================================
-- 帖子31: Go后端学习小组 (自习监督)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(31, 10, '我也在学Go！gin框架用得多吗', NULL, 2, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(31, 12, '可以一起review代码，我在学gorm', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(31, 11, 'Go的并发模型确实好用，一起交流', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子32: Java后端春招备战 (自习监督)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(32, 14, '我也在准备Java春招！一起刷八股', NULL, 2, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(32, 16, 'Spring Boot源码要看吗', NULL, 1, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(32, 18, '我可以加入吗，正在学Redis', NULL, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- =============================================
-- 帖子33: 前端Vue+React交流群 (自习监督)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(33, 11, '我主要用Vue3，Composition API真香', NULL, 2, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(33, 13, 'React Hooks有点难理解，求教', NULL, 1, DATE_SUB(NOW(), INTERVAL 4 HOUR));

-- =============================================
-- 帖子34: 算法刷题打卡 (自习监督)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(34, 12, 'Hot 100刷完一半了，一起加油', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(34, 15, '动态规划好难，有技巧吗', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子35: 就业信息分享群 (自习监督)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(35, 10, '字节跳动有内推吗', NULL, 2, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(35, 17, '腾讯校招开始了，可以投简历了', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(35, 11, '求分享面经！', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子36: Python数据分析学习 (自习监督)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(36, 9, 'pandas数据处理确实方便', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(36, 14, '有推荐的数据集练习吗', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子37: 考研自习室组队 (自习监督)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(37, 15, '我也在准备考研！一起', NULL, 2, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(37, 18, '目标哪个学校', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(37, 12, '我可以加入吗', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子38: 嵌入式开发交流 (自习监督)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(38, 13, '我在做STM32项目，可以交流', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(38, 16, 'ESP32的WiFi模块好用吗', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子39: 保研准备交流群 (自习监督)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(39, 11, '我也在准备保研', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(39, 14, '可以分享一些经验吗', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子40: 全栈开发学习路线 (自习监督)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(40, 12, '我也在走全栈路线！一起', NULL, 2, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(40, 17, 'Node.js用什么框架比较好', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(40, 10, '全栈要学的确实多，一起加油', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子41: 有没有一起打篮球的 (运动健身)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(41, 9, '我也想打球，不过我打控卫，可以组个队', NULL, 4, DATE_SUB(NOW(), INTERVAL 1 DAY));
SET @c41_id = LAST_INSERT_ID();
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(41, 13, '周末下午有空，一起？', NULL, 2, DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(41, 11, '我也可以！身高175打后卫', NULL, 1, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(41, 15, '好的，周末下午北校区篮球场见', @c41_id, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- =============================================
-- 帖子42: 有没有一起健身的 (运动健身)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(42, 15, '健身房我也办了卡，可以一起练', NULL, 2, DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(42, 13, '我也想增肌，可以互相监督', NULL, 1, DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(42, 9, '好的，周末约起来', NULL, 0, DATE_SUB(NOW(), INTERVAL 3 HOUR));

-- =============================================
-- 帖子43: 羽毛球约球 (运动健身)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(43, 10, '我也喜欢打羽毛球，一起', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(43, 14, '我有拍子，周末下午可以', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子44: 夜跑小队招募 (运动健身)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(44, 11, '3-5公里我可以！', NULL, 2, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(44, 16, '配速6分适合新手，不错', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(44, 9, '我也加入！晚上9点见', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子45: 游泳搭子 (运动健身)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(45, 12, '我会蝶泳！可以教你', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(45, 17, '我也想学游泳，可以一起', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子46: 乒乓球约战 (运动健身)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(46, 10, '我也打横拍，切磋一下', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(46, 15, '我直拍，可以吗', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子47: 瑜伽/普拉提小班 (运动健身)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(47, 13, '我也想练瑜伽！一起', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(47, 11, '附近哪家瑜伽馆比较好', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子48: 攀岩体验约伴 (运动健身)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(48, 14, '我想试试！零基础可以吗', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(48, 18, '可以的，有教练教', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子49: 足球5v5约战 (运动健身)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(49, 9, '我踢前锋，可以来', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(49, 16, '我守门，缺人叫我', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(49, 12, '我也加入！', NULL, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- =============================================
-- 帖子50: 骑行小队 (运动健身)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(50, 11, '我有自行车！可以一起', NULL, 2, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(50, 15, '30-50公里我可以', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- =============================================
-- 帖子51: 摄影爱好者交流群 (其他活动)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(51, 10, '我有微单，可以一起交流', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(51, 14, '扫街好主意！周末去', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(51, 12, '后期修图求教', NULL, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- =============================================
-- 帖子52: 桌游局组起来 (其他活动)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(52, 11, '狼人杀我可以！', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(52, 15, '阿瓦隆好玩，我想玩', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(52, 9, '周末晚上可以吗', NULL, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- =============================================
-- 帖子53: 吉他弹唱交流 (其他活动)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(53, 12, '我会弹吉他！可以一起', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(53, 16, '我想学吉他，可以旁听吗', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子54: 电影观影团 (其他活动)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(54, 13, '想看诺兰的电影！', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(54, 17, '经典电影也可以', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(54, 10, '每周一部，我可以', NULL, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- =============================================
-- 帖子55: 手工DIY工作坊 (其他活动)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(55, 14, '手账我可以！', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(55, 11, '橡皮章怎么刻', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子56: 英语角活动 (其他活动)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(56, 12, '想练口语，我可以参加', NULL, 2, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(56, 15, '水平不限太好了', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(56, 9, '每周什么时候', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子57: 剧本杀组队 (其他活动)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(57, 10, '我想玩！什么本', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(57, 16, '4-6人本我可以', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(57, 13, '推理本还是情感本', NULL, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- =============================================
-- 帖子58: 志愿者活动招募 (其他活动)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(58, 11, '想参加！可以积累时长', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(58, 14, '帮助老人很有意义', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- =============================================
-- 帖子59: 宠物交流群 (其他活动)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(59, 12, '我养了只猫！', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(59, 15, '我养狗，可以一起遛狗', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(59, 9, '求分享养宠经验', NULL, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- =============================================
-- 帖子60: 创业想法交流 (其他活动)
-- =============================================
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `create_time`) VALUES
(60, 11, '有想法！可以聊聊', NULL, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(60, 17, '互联网+教育怎么样', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(60, 10, ' brainstorm 好主意', NULL, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE));
