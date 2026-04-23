package com.example.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论查询请求DTO
 * <p>
 * 封装评论列表查询接口的请求参数
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentQueryRequestDTO {

    /** 帖子ID */
    private Long postId;

    /** 页码（从1开始），默认为1 */
    @Builder.Default
    private Integer pageNum = 1;

    /** 每页大小，默认为10 */
    @Builder.Default
    private Integer pageSize = 10;
}
