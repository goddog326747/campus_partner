package com.example.project.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子 ES 文档实体类
 * <p>
 * 用于 Elasticsearch 索引存储和检索
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
@Document(indexName = "post")
@Setting(settingPath = "elasticsearch/post-setting.json")
@Mapping(mappingPath = "elasticsearch/post-mapping.json")
public class PostDocument {

    @Id
    @Field(type = FieldType.Long)
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Keyword)
    private List<String> images;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String destination;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Field(type = FieldType.Keyword)
    private String username;

    @Field(type = FieldType.Keyword)
    private String avatar;

    @Field(type = FieldType.Keyword)
    private String userLocation;

    @Field(type = FieldType.Keyword)
    private String userSchool;

    @Field(type = FieldType.Integer)
    private Integer userVerified;

    @Field(type = FieldType.Integer)
    private Integer userGender;

    @Field(type = FieldType.Integer)
    private Integer commentCount = 0;

    @Field(type = FieldType.Integer)
    private Integer likeCount = 0;
}
