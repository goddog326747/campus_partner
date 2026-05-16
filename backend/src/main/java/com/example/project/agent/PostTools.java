package com.example.project.agent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.project.entity.Post;
import com.example.project.entity.User;
import com.example.project.service.PostService;
import com.example.project.mapper.UserMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class PostTools {

    private final PostService postService;
    private final UserMapper userMapper;

    public PostTools(PostService postService, UserMapper userMapper) {
        this.postService = postService;
        this.userMapper = userMapper;
    }

    @Tool("获取用户在平台上的活跃画像，包括发帖频率、偏好分类、互动风格等。" +
          "当需要了解用户特征以个性化生成内容时使用此工具。" +
          "返回JSON格式的用户画像数据。")
    public String getUserProfile(@P("用户ID，用于查询该用户的平台画像") Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return jsonError("用户不存在");
        }

        List<Post> posts = postService.getPostsByUserId(userId);

        JSONObject profile = new JSONObject();
        profile.put("userId", userId);
        profile.put("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
        profile.put("location", user.getLocation());
        profile.put("school", user.getSchool());
        profile.put("gender", user.getGender() != null ? (user.getGender() == 1 ? "男" : user.getGender() == 2 ? "女" : "未知") : "未知");

        JSONObject activity = new JSONObject();
        activity.put("totalPosts", posts != null ? posts.size() : 0);
        activity.put("postFrequency", posts != null && posts.size() > 5 ? "活跃" : posts != null && posts.size() > 0 ? "偶尔" : "新用户");

        if (posts != null && !posts.isEmpty()) {
            Map<String, Long> categoryCount = posts.stream()
                    .filter(p -> p.getCategory() != null)
                    .collect(Collectors.groupingBy(Post::getCategory, Collectors.counting()));
            List<String> topCategories = categoryCount.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            activity.put("preferredCategories", topCategories);
            activity.put("categoryDistribution", categoryCount);
        } else {
            activity.put("preferredCategories", Collections.emptyList());
        }

        profile.put("activity", activity);
        return JSON.toJSONString(profile);
    }

    @Tool("分析用户历史帖子的写作风格，包括语言风格、内容特征和表达习惯。" +
          "当需要模仿用户风格生成帖子，或根据风格偏好调整内容时使用。" +
          "返回JSON格式的风格分析结果，包含风格标签和参考示例。")
    public String analyzeUserStyle(@P("用户ID，用于查询该用户的历史帖子并分析写作风格") Long userId) {
        List<Post> posts = postService.getPostsByUserId(userId);

        if (posts == null || posts.isEmpty()) {
            JSONObject result = new JSONObject();
            result.put("hasHistory", false);
            result.put("suggestedStyle", "轻松活泼的社交风格，适合找搭子平台");
            return JSON.toJSONString(result);
        }

        double avgTitleLength = posts.stream()
                .filter(p -> p.getTitle() != null)
                .mapToInt(p -> p.getTitle().length())
                .average().orElse(10);

        double avgContentLength = posts.stream()
                .filter(p -> p.getContent() != null)
                .mapToInt(p -> p.getContent().length())
                .average().orElse(200);

        double emojiPerPost = posts.stream()
                .filter(p -> p.getContent() != null)
                .mapToDouble(p -> countEmojis(p.getContent()))
                .average().orElse(0);

        double exclamationPerPost = posts.stream()
                .filter(p -> p.getContent() != null)
                .mapToDouble(p -> p.getContent().chars().filter(c -> c == '!' || c == '！').count())
                .average().orElse(0);

        List<String> styleTags = new ArrayList<>();
        if (avgTitleLength < 8) styleTags.add("标题简洁");
        else if (avgTitleLength > 15) styleTags.add("标题详细");
        else styleTags.add("标题适中");

        if (avgContentLength < 150) styleTags.add("内容精炼");
        else if (avgContentLength > 300) styleTags.add("内容丰富");
        else styleTags.add("内容适中");

        if (emojiPerPost > 2) styleTags.add("活泼有趣");
        else if (emojiPerPost > 0.5) styleTags.add("轻松友好");
        else styleTags.add("正式专业");

        if (exclamationPerPost > 2) styleTags.add("热情洋溢");

        JSONObject result = new JSONObject();
        result.put("hasHistory", true);
        result.put("styleTags", styleTags);
        result.put("avgTitleLength", Math.round(avgTitleLength));
        result.put("avgContentLength", Math.round(avgContentLength));
        result.put("emojiUsage", emojiPerPost > 1 ? "频繁" : emojiPerPost > 0.3 ? "偶尔" : "很少");

        Post recentPost = posts.get(0);
        if (recentPost.getTitle() != null && recentPost.getContent() != null) {
            JSONObject example = new JSONObject();
            example.put("title", recentPost.getTitle());
            example.put("contentPreview", recentPost.getContent().length() > 80
                    ? recentPost.getContent().substring(0, 80) + "..."
                    : recentPost.getContent());
            example.put("category", recentPost.getCategory());
            result.put("recentExample", example);
        }

        return JSON.toJSONString(result);
    }

    @Tool("获取平台当前的热门话题趋势，按分类返回热门话题及其相关帖子数量。" +
          "当需要了解平台热点、为帖子选择热门话题或参考流行趋势时使用。" +
          "返回JSON格式的话题列表，每个话题包含名称、分类和热度。")
    public String getTrendingTopics(@P("帖子分类，如'出游搭子'、'学习搭子'、'游戏搭子'等，传null获取全部分类热门话题") String category) {
        List<Post> posts = postService.listPostsWithPage(category, null, 1, 50).getRecords();

        if (posts == null || posts.isEmpty()) {
            return jsonError("暂无热门话题数据");
        }

        Map<String, Long> categoryCount = posts.stream()
                .filter(p -> p.getCategory() != null)
                .collect(Collectors.groupingBy(Post::getCategory, Collectors.counting()));

        JSONArray topics = new JSONArray();
        categoryCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .forEach(entry -> {
                    JSONObject topic = new JSONObject();
                    topic.put("category", entry.getKey());
                    topic.put("postCount", entry.getValue());

                    List<String> sampleTitles = posts.stream()
                            .filter(p -> entry.getKey().equals(p.getCategory()))
                            .filter(p -> p.getTitle() != null)
                            .limit(3)
                            .map(Post::getTitle)
                            .collect(Collectors.toList());
                    topic.put("sampleTitles", sampleTitles);
                    topics.add(topic);
                });

        JSONObject result = new JSONObject();
        result.put("total", topics.size());
        result.put("topics", topics);
        return JSON.toJSONString(result);
    }

    @Tool("搜索平台上与关键词和分类相关的帖子，返回结构化的帖子摘要列表。" +
          "当需要参考已有帖子的写法、了解同类话题的内容风格或获取创作灵感时使用。" +
          "返回JSON格式的帖子列表，包含标题、内容摘要、分类和风格标签。")
    public String searchPosts(
            @P("搜索关键词，如'爬山'、'周末活动'、'图书馆'等具体话题关键词") String keyword,
            @P("帖子分类筛选，如'出游搭子'、'学习搭子'，传null不筛选分类") String category) {
        List<Post> posts = postService.listPostsWithPage(category, keyword, 1, 10).getRecords();

        if (posts == null || posts.isEmpty()) {
            JSONObject result = new JSONObject();
            result.put("total", 0);
            result.put("posts", new JSONArray());
            result.put("suggestion", "未找到相关帖子，可以尝试换个关键词或分类");
            return JSON.toJSONString(result);
        }

        JSONArray postArray = new JSONArray();
        int count = 0;
        for (Post post : posts) {
            if (count >= 5) break;

            JSONObject postObj = new JSONObject();
            postObj.put("title", post.getTitle());
            postObj.put("category", post.getCategory());
            postObj.put("destination", post.getDestination());

            if (post.getContent() != null) {
                String content = post.getContent();
                postObj.put("contentPreview", content.length() > 80 ? content.substring(0, 80) + "..." : content);
                postObj.put("contentLength", content.length());

                List<String> tags = new ArrayList<>();
                if (content.length() < 150) tags.add("精炼型");
                else if (content.length() > 300) tags.add("详细型");
                if (countEmojis(content) > 2) tags.add("活泼型");
                if (content.contains("！") || content.contains("!")) tags.add("热情型");
                postObj.put("styleTags", tags);
            }

            postObj.put("authorLocation", post.getUserLocation());
            postObj.put("authorSchool", post.getUserSchool());

            postArray.add(postObj);
            count++;
        }

        JSONObject result = new JSONObject();
        result.put("total", posts.size());
        result.put("keyword", keyword);
        result.put("category", category);
        result.put("posts", postArray);
        return JSON.toJSONString(result);
    }

    @Tool("验证生成的内容是否符合平台规范，检查敏感词、格式和可读性。" +
          "在内容生成完成后使用此工具进行质量把关。" +
          "返回JSON格式的验证结果，包含是否通过、具体问题和修改建议。")
    public String validateContent(
            @P("要验证的帖子标题") String title,
            @P("要验证的帖子正文内容") String content) {
        JSONObject result = new JSONObject();
        boolean passed = true;
        JSONArray issues = new JSONArray();
        JSONArray suggestions = new JSONArray();

        List<String> sensitiveWords = List.of("广告", "推销", "加微信", "转账", "代购", "兼职", "刷单");
        for (String word : sensitiveWords) {
            String textToCheck = (title != null ? title : "") + (content != null ? content : "");
            if (textToCheck.contains(word)) {
                passed = false;
                JSONObject issue = new JSONObject();
                issue.put("type", "sensitive_word");
                issue.put("word", word);
                issue.put("suggestion", "请移除或替换该词汇");
                issues.add(issue);
            }
        }

        if (title != null && title.length() < 4) {
            JSONObject issue = new JSONObject();
            issue.put("type", "title_too_short");
            issue.put("suggestion", "标题建议4个字以上，更吸引人");
            issues.add(issue);
            suggestions.add("可以加一些吸引眼球的修饰，如'🔥'或具体数字");
        }

        if (content != null && content.length() < 30) {
            JSONObject issue = new JSONObject();
            issue.put("type", "content_too_short");
            issue.put("suggestion", "内容建议30字以上，包含具体的时间、地点或活动细节");
            issues.add(issue);
            suggestions.add("可以补充：什么时候、在哪里、怎么联系");
        }

        if (content != null && !content.contains("，") && !content.contains("。") && content.length() > 50) {
            JSONObject issue = new JSONObject();
            issue.put("type", "no_punctuation");
            issue.put("suggestion", "内容缺少标点符号，建议分段和加标点提高可读性");
            issues.add(issue);
        }

        result.put("passed", passed);
        result.put("issues", issues);
        result.put("suggestions", suggestions);
        return JSON.toJSONString(result);
    }

    private int countEmojis(String text) {
        Pattern emojiPattern = Pattern.compile(
                "[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83D\uDE00-\uD83D\uDE4F]|" +
                        "[\uD83D\uDE80-\uD83D\uDEFF]|[\uD83E\uDD00-\uD83E\uDDFF]|" +
                        "[\u2600-\u26FF]|[\u2700-\u27BF]"
        );
        Matcher matcher = emojiPattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private String jsonError(String message) {
        JSONObject result = new JSONObject();
        result.put("error", message);
        return JSON.toJSONString(result);
    }
}
