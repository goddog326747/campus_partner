package com.example.project.agent;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;

import com.example.project.entity.Post;
import com.example.project.entity.User;
import com.example.project.service.PostService;
import com.example.project.mapper.UserMapper;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 帖子相关工具类
 * 
 * ============================================================
 *                 Tool Calling 核心概念
 * ============================================================
 * 
 * 这里的 @Tool 注解定义了 AI 可以调用的工具。
 * 
 * 工作原理：
 * 1. 用户发送消息给 AI
 * 2. AI 分析用户意图，决定是否需要调用工具
 * 3. 如果需要，AI 自动调用对应的工具方法
 * 4. 工具方法返回结果
 * 5. AI 根据结果生成最终回复
 * 
 * 你不需要写调用逻辑！AI 自己决定何时调用！
 * 
 * ============================================================
 */
@Component
public class PostTools {

    private final PostService postService;
    private final UserMapper userMapper;

    public PostTools(PostService postService, UserMapper userMapper) {
        this.postService = postService;
        this.userMapper = userMapper;
    }

    /**
     * 查询用户历史帖子的写作风格
     * 
     * ============================================================
     *                    风格分析原理
     * ============================================================
     * 
     * 这个方法展示了如何从历史帖子中分析出写作风格：
     * 
     * 1. 规则分析（不消耗 AI Token）
     *    - 统计标题长度偏好
     *    - 统计内容长度偏好
     *    - 检测表情符号使用频率
     *    - 检测标点符号使用习惯
     *    - 检测常用词汇
     * 
     * 2. 特征提取
     *    - 正式 vs 随意
     *    - 简洁 vs 详细
     *    - 严肃 vs 活泼
     *    - 是否使用表情符号
     * 
     * 3. 综合判断
     *    - 根据多个特征综合判断风格
     * 
     * ============================================================
     */
    @Tool("查询用户历史帖子的写作风格，返回风格描述")
    public String getUserPostStyle(@P("用户ID") Long userId) {
        List<Post> posts = postService.getPostsByUserId(userId);
        
        if (posts == null || posts.isEmpty()) {
            return "用户暂无历史帖子，建议使用轻松活泼的默认风格";
        }
        
        // ========== 第一步：统计特征 ==========
        
        // 1. 标题长度统计
        double avgTitleLength = posts.stream()
                .filter(p -> p.getTitle() != null)
                .mapToInt(p -> p.getTitle().length())
                .average()
                .orElse(10);
        
        // 2. 内容长度统计
        double avgContentLength = posts.stream()
                .filter(p -> p.getContent() != null)
                .mapToInt(p -> p.getContent().length())
                .average()
                .orElse(200);
        
        // 3. 表情符号统计
        int emojiCount = posts.stream()
                .filter(p -> p.getContent() != null)
                .map(p -> p.getContent())
                .mapToInt(this::countEmojis)
                .sum();
        double emojiPerPost = (double) emojiCount / posts.size();
        
        // 4. 感叹号统计（表示热情）
        long exclamationCount = posts.stream()
                .filter(p -> p.getContent() != null)
                .flatMap(p -> p.getContent().chars().boxed())
                .filter(c -> c == '!' || c == '！')
                .count();
        double exclamationPerPost = (double) exclamationCount / posts.size();
        
        // 5. 问号统计（表示互动性）
        long questionCount = posts.stream()
                .filter(p -> p.getContent() != null)
                .flatMap(p -> p.getContent().chars().boxed())
                .filter(c -> c == '?' || c == '？')
                .count();
        double questionPerPost = (double) questionCount / posts.size();
        
        // ========== 第二步：分析风格特征 ==========
        
        StringBuilder styleBuilder = new StringBuilder();
        
        // 分析：正式 vs 随意
        if (avgTitleLength < 8) {
            styleBuilder.append("标题简洁有力，");
        } else if (avgTitleLength > 15) {
            styleBuilder.append("标题详细描述性强，");
        } else {
            styleBuilder.append("标题长度适中，");
        }
        
        // 分析：简洁 vs 详细
        if (avgContentLength < 150) {
            styleBuilder.append("内容简洁明了，");
        } else if (avgContentLength > 300) {
            styleBuilder.append("内容详细丰富，");
        } else {
            styleBuilder.append("内容详略得当，");
        }
        
        // 分析：严肃 vs 活泼
        if (emojiPerPost > 2) {
            styleBuilder.append("风格活泼有趣，常用表情符号，");
        } else if (emojiPerPost > 0.5) {
            styleBuilder.append("风格轻松友好，偶尔使用表情符号，");
        } else {
            styleBuilder.append("风格正式专业，");
        }
        
        // 分析：热情程度
        if (exclamationPerPost > 2) {
            styleBuilder.append("表达热情洋溢，");
        } else if (exclamationPerPost > 0.5) {
            styleBuilder.append("表达积极友好，");
        }
        
        // 分析：互动性
        if (questionPerPost > 1) {
            styleBuilder.append("善于与读者互动提问");
        }
        
        // ========== 第三步：生成最终风格描述 ==========
        
        String style = styleBuilder.toString();
        
        // 添加示例（取最近一篇帖子作为参考）
        Post recentPost = posts.get(0);
        if (recentPost.getTitle() != null && recentPost.getContent() != null) {
            String exampleContent = recentPost.getContent().length() > 50 
                    ? recentPost.getContent().substring(0, 50) + "..."
                    : recentPost.getContent();
            style += String.format("。参考示例：《%s》- %s", 
                    recentPost.getTitle(), exampleContent);
        }
        
        return style;
    }
    
    /**
     * 统计文本中的表情符号数量
     * 
     * 使用 Unicode 范围检测常见表情符号
     */
    private int countEmojis(String text) {
        // 常见表情符号的 Unicode 范围
        Pattern emojiPattern = Pattern.compile(
            "[\uD83C\uDF00-\uD83D\uDDFF]|" +  // 各种符号和图标
            "[\uD83D\uDE00-\uD83D\uDE4F]|" +  // 表情符号
            "[\uD83D\uDE80-\uD83D\uDEFF]|" +  // 交通和地图符号
            "[\uD83E\uDD00-\uD83E\uDDFF]|" +  // 补充符号和图标
            "[\u2600-\u26FF]|" +               // 杂项符号
            "[\u2700-\u27BF]"                  // 装饰符号
        );
        
        Matcher matcher = emojiPattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * 获取当前热门话题
     * 
     * AI 可以在生成帖子时参考这些热门话题
     * 
     * @return 热门话题列表
     */
    @Tool("获取当前平台的热门话题列表")
    public String getHotTopics() {
        // 实际项目可以从数据库统计或缓存获取
        // 这里返回模拟数据
        return "当前热门话题：周末爬山、图书馆自习、羽毛球约球、游戏开黑、电影搭子";
    }

    /**
     * 检查内容是否包含敏感词
     * 
     * AI 生成内容后可以调用此工具检查
     * 
     * @param content 要检查的内容
     * @return 检查结果
     */
    @Tool("检查内容是否包含敏感词，返回检查结果")
    public String checkSensitiveContent(@P("要检查的内容") String content) {
        if (content == null || content.isEmpty()) {
            return "内容为空";
        }
        
        // 简单的敏感词检测（实际项目应该用专业的敏感词库）
        List<String> sensitiveWords = List.of(
            "广告", "推销", "加微信", "转账", "代购", "兼职"
        );
        
        for (String word : sensitiveWords) {
            if (content.contains(word)) {
                return "⚠️ 内容包含敏感词：" + word + "，建议修改";
            }
        }
        
        return "✅ 内容检查通过，无敏感词";
    }

    /**
     * 获取用户基本信息
     * 
     * AI 可以根据用户信息个性化生成内容
     * 
     * @param userId 用户ID
     * @return 用户信息描述
     */
    @Tool("获取用户的基本信息，包括昵称、所在地、学校等")
    public String getUserInfo(@P("用户ID") Long userId) {
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            return "用户不存在";
        }
        
        return String.format(
            "用户信息：昵称%s，所在地%s，学校%s",
            user.getNickname() != null ? user.getNickname() : user.getUsername(),
            user.getLocation() != null ? user.getLocation() : "未知",
            user.getSchool() != null ? user.getSchool() : "未知"
        );
    }

    /**
     * 搜索相关帖子
     * 
     * AI 可以参考这些帖子生成新内容
     * 
     * @param keyword 搜索关键词
     * @return 相关帖子摘要
     */
    @Tool("搜索平台上的相关帖子，返回摘要供参考")
    public String searchRelatedPosts(@P("搜索关键词") String keyword) {
        List<Post> posts = postService.listPostsWithPage(null, keyword, 1, 10).getRecords();
        
        if (posts == null || posts.isEmpty()) {
            return "未找到相关帖子";
        }
        
        // 返回前3篇帖子的摘要
        StringBuilder sb = new StringBuilder("找到以下相关帖子：\n");
        int count = 0;
        for (Post post : posts) {
            if (count >= 3) break;
            sb.append(String.format("%d. 《%s》- %s\n", 
                    count + 1,
                    post.getTitle(),
                    post.getContent() != null && post.getContent().length() > 50 
                            ? post.getContent().substring(0, 50) + "..." 
                            : post.getContent()
            ));
            count++;
        }
        
        return sb.toString();
    }
}
