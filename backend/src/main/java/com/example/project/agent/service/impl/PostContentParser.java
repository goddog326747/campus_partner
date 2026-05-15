package com.example.project.agent.service.impl;

import com.example.project.agent.dto.PostContent;

public class PostContentParser {

    public static PostContent parse(String content) {
        PostContent result = new PostContent();

        if (content == null || content.isEmpty()) {
            return PostContent.empty();
        }

        String title = extractSection(content, "标题", "【标题】");
        String body = extractSection(content, "正文", "【正文】");
        String tags = extractSection(content, "标签", "【标签】");

        if (!title.isEmpty()) {
            result.setTitle(title);
            result.setContent(body.isEmpty() ? content : body);
            result.setTags(tags);
            return result;
        }

        String[] lines = content.split("\n");
        if (lines.length > 0) {
            String firstLine = lines[0].trim();
            if (firstLine.startsWith("标题：") || firstLine.startsWith("标题:")) {
                result.setTitle(firstLine.substring(3).trim());
                result.setContent(content.substring(firstLine.length()).trim());
            } else if (firstLine.length() < 30) {
                result.setTitle(firstLine);
                result.setContent(content.substring(firstLine.length()).trim());
            } else {
                result.setTitle("生成的帖子");
                result.setContent(content);
            }
        } else {
            result.setTitle("生成的帖子");
            result.setContent(content);
        }

        return result;
    }

    private static String extractSection(String content, String keyword, String marker) {
        int start = content.indexOf(marker);
        if (start == -1) {
            start = content.indexOf(keyword + "：");
            if (start == -1) {
                start = content.indexOf(keyword + ":");
            }
        }

        if (start == -1) {
            return "";
        }

        start = content.indexOf("：", start);
        if (start == -1) {
            start = content.indexOf(":", start);
        }
        if (start == -1) {
            return "";
        }
        start++;

        int end = content.length();
        String[] nextMarkers = {"【", "标题", "正文", "标签", "\n\n"};
        for (String nextMarker : nextMarkers) {
            int nextPos = content.indexOf(nextMarker, start);
            if (nextPos != -1 && nextPos < end) {
                end = nextPos;
            }
        }

        return content.substring(start, end).trim();
    }
}
