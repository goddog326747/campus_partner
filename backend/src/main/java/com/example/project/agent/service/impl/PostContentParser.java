package com.example.project.agent.service.impl;

import com.example.project.agent.dto.PostContent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostContentParser {

    private static final Pattern TITLE_PATTERN = Pattern.compile("【标题】\\s*(.*?)(?=\\s*【正文】|\\s*$)", Pattern.DOTALL);
    private static final Pattern CONTENT_PATTERN = Pattern.compile("【正文】\\s*(.*?)(?=\\s*【标签】|\\s*$)", Pattern.DOTALL);
    private static final Pattern TAGS_PATTERN = Pattern.compile("【标签】\\s*(.*?)$", Pattern.DOTALL);

    public static PostContent parse(String content) {
        if (content == null || content.isEmpty()) {
            return PostContent.empty();
        }

        Matcher titleMatcher = TITLE_PATTERN.matcher(content);
        Matcher contentMatcher = CONTENT_PATTERN.matcher(content);
        Matcher tagsMatcher = TAGS_PATTERN.matcher(content);

        boolean hasTitle = titleMatcher.find();
        boolean hasContent = contentMatcher.find();
        boolean hasTags = tagsMatcher.find();

        if (hasTitle || hasContent) {
            PostContent result = new PostContent();
            result.setTitle(hasTitle ? titleMatcher.group(1).trim() : "生成的帖子");
            result.setContent(hasContent ? contentMatcher.group(1).trim() : content);
            result.setTags(hasTags ? tagsMatcher.group(1).trim() : "");
            return result;
        }

        String[] lines = content.split("\n");
        if (lines.length > 0) {
            String firstLine = lines[0].trim();
            if (firstLine.length() < 30) {
                PostContent result = new PostContent();
                result.setTitle(firstLine);
                result.setContent(content.substring(firstLine.length()).trim());
                return result;
            }
        }

        PostContent result = new PostContent();
        result.setTitle("生成的帖子");
        result.setContent(content);
        return result;
    }
}
