package com.example.project.agent.dto;

import lombok.Data;

@Data
public class PostContent {
    private String title;
    private String content;
    private String tags;

    public static PostContent empty() {
        PostContent pc = new PostContent();
        pc.setTitle("生成的帖子");
        pc.setContent("");
        return pc;
    }
}
