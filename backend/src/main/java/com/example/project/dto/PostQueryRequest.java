package com.example.project.dto;

import lombok.Data;

@Data
public class PostQueryRequest {
    private String category;
    private String keyword;
    private String location;
    private String school;
    private Boolean verified;
    private Integer gender;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    
    public boolean hasUserFilters() {
        return location != null || school != null || verified != null || gender != null;
    }
}
