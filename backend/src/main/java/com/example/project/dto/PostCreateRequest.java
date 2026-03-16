package com.example.project.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostCreateRequest {
    private String title;
    private String content;
    private String category;
    private String destination;
    private List<String> images;
}
