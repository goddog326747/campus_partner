USE `campus_partner`;

-- 删除现有用户数据并重置自增计数器
DELETE FROM `user`;
ALTER TABLE `user` AUTO_INCREMENT = 1;

INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`, `gender`, `birthday`, `bio`, `location`, `school`, `school_email`, `verified`, `phone`, `email`, `wechat`, `qq`, `privacy_profile`, `privacy_contact`) VALUES
('admin', '123456', '管理员', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', 1, '2000-01-15', '系统管理员，有问题欢迎私信~', '北京', '清华大学', 'admin@tsinghua.edu.cn', 2, '13800000001', 'admin@example.com', 'admin_wx', '100001', 0, 0),
('student1', '123456', '快乐大学生', 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', 2, '2002-05-20', '喜欢旅行和摄影，希望能找到志同道合的小伙伴一起玩耍！', '上海', '复旦大学', 'student1@fudan.edu.cn', 2, '13800000002', 'student1@example.com', 'happy_student', '100002', 0, 1),
('gamer', '123456', '游戏大神', 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png', 1, '2001-08-10', 'LOL、王者、原神都玩，找队友一起开黑！', '广州', '中山大学', NULL, 0, '13800000003', 'gamer@example.com', 'gamer_pro', '100003', 0, 1),
('traveler', '123456', '旅行达人', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', 2, '1999-12-25', '走遍祖国大好河山，下一站：西藏！', '成都', '四川大学', 'traveler@scu.edu.cn', 1, '13800000004', 'traveler@example.com', 'travel_lover', '100004', 0, 2),
('comic_fan', '123456', '漫展常客', 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', 0, '2003-03-08', '二次元爱好者，每年必去漫展，求coser小伙伴！', '杭州', '浙江大学', 'comicfan@zju.edu.cn', 2, '13800000005', 'comic@example.com', 'comic_fan_2024', '100005', 0, 1),
('foodie', '123456', '美食探店', 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png', 1, '2000-07-18', '吃货一枚，专门探店各种美食，欢迎一起打卡！', '南京', '南京大学', NULL, 0, '13800000006', 'foodie@example.com', 'food_hunter', '100006', 0, 0),
('sports', '123456', '运动健将', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', 1, '2001-11-30', '篮球、羽毛球、跑步，运动搭子来约！', '武汉', '武汉大学', 'sports@whu.edu.cn', 2, '13800000007', 'sports@example.com', 'sports_lover', '100007', 0, 1),
('bookworm', '123456', '书虫小窝', 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', 2, '2002-09-14', '喜欢阅读和咖啡，周末常去书店，找书友一起交流~', '西安', '西安交通大学', NULL, 0, '13800000008', 'bookworm@example.com', 'book_worm', '100008', 2, 2),
('zhangsan', '123456', '阿张', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', 1, '2002-03-15', '西电大三，喜欢打游戏和摄影，周末经常出去玩', '西安', '西安电子科技大学', 'zhangsan@stu.xidian.edu.cn', 2, '13800000009', 'zhangsan@example.com', 'azhang_xd', '100009', 0, 1),
('xiaoli', '123456', '小李同学', 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', 2, '2000-06-22', '研一在读，二次元爱好者，漫展常客', '西安', '西安电子科技大学', 'xiaoli@stu.xidian.edu.cn', 2, '13800000010', 'xiaoli@example.com', 'xiaoli_xd', '100010', 0, 1),
('wangwei', '123456', '小王', 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png', 1, '2003-01-08', '大二计院，王者钻石，求带飞', '西安', '西安电子科技大学', 'wangwei@stu.xidian.edu.cn', 2, '13800000011', 'wangwei@example.com', 'xiaowang_xd', '100011', 0, 1),
('chenxue', '123456', '雪儿', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', 2, '2002-11-28', '西电通信工程，喜欢旅行和美食', '西安', '西安电子科技大学', 'chenxue@stu.xidian.edu.cn', 2, '13800000012', 'chenxue@example.com', 'xueer_xd', '100012', 0, 0),
('liuyang', '123456', '刘洋', 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', 1, '1999-09-10', '研二老学长，毕业前想多出去走走', '西安', '西安电子科技大学', 'liuyang@stu.xidian.edu.cn', 2, '13800000013', 'liuyang@example.com', 'liuyang_xd', '100013', 0, 1),
('zhaomin', '123456', '敏敏', 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png', 2, '2002-04-05', '大三软院，原神玩家，周末约饭约玩', '西安', '西安电子科技大学', NULL, 0, '13800000014', 'zhaomin@example.com', 'minmin_xd', '100014', 0, 1),
('sunhao', '123456', '孙浩', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', 1, '2001-12-18', '西电电子工程，篮球爱好者，想找人一起打球', '西安', '西安电子科技大学', 'sunhao@stu.xidian.edu.cn', 2, '13800000015', 'sunhao@example.com', 'sunhao_xd', '100015', 0, 1),
('zhoujie', '123456', '杰哥', 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', 1, '2000-08-30', '大四即将毕业，趁着还有时间多出去玩玩', '西安', '西安电子科技大学', 'zhoujie@stu.xidian.edu.cn', 2, '13800000016', 'zhoujie@example.com', 'jiege_xd', '100016', 0, 0),
('linlin', '123456', '琳琳', 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png', 2, '2002-02-14', '西电经管院，喜欢探店打卡，美食博主预备役', '西安', '西安电子科技大学', NULL, 0, '13800000017', 'linlin@example.com', 'linlin_xd', '100017', 0, 1),
('huangfeng', '123456', '阿峰', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', 1, '1999-05-20', '研一，喜欢户外运动，周末经常爬山', '西安', '西安电子科技大学', 'huangfeng@stu.xidian.edu.cn', 2, '13800000018', 'huangfeng@example.com', 'afeng_xd', '100018', 0, 1),
('林文韬', 'lwt293406', '毕设演示', '/img/avatar/2026/05/15/d06ab92f452d43c0bf6a01c7260500be.jpg', 1, NULL, NULL, '西安', '西安电子科技大学', NULL, NULL, '15057133791', NULL, NULL, NULL, 0, 1);
