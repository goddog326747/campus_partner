USE `campus_partner`;

-- 3. 插入测试数据

-- 用户数据 (密码均为 123456)
INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`) VALUES 
('admin', '123456', '管理员', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'),
('student1', '123456', '快乐大学生', 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'),
('gamer', '123456', '游戏大神', 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png');

-- 帖子数据
INSERT INTO `post` (`title`, `content`, `category`, `user_id`, `create_time`) VALUES 
('周末有人一起去环球影城吗？', '计划这周末去北京环球影城，想找个搭子一起玩项目，男女不限，最好是哈利波特迷！', '旅行出游', 2, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('王者荣耀排位上分车队，缺个辅助', '目前段位星耀一，差几把上王者，来个会玩硬辅的，晚上8点准时开车。', '游戏组队', 3, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('图书馆考研复习，找个监督搭子', '每天早上8点到晚上9点，坐标图书馆3楼，互相监督学习进度，拒绝摸鱼。', '自习监督', 2, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
('五一假期想去成都吃火锅', '有没有想去成都旅游的同学？计划去宽窄巷子、锦里，还有熊猫基地。求组队！', '旅行远游', 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),
('BW漫展求集邮', '这周末去BW，出的是原神里的角色，有没有摄影师或者一起逛展的小伙伴？', '出游搭子', 3, DATE_SUB(NOW(), INTERVAL 12 HOUR)),
('夜跑五公里，操场集合', '每天晚上9点操场夜跑，配速6分左右，欢迎加入一起锻炼身体。', '运动健身', 1, DATE_SUB(NOW(), INTERVAL 1 DAY));
