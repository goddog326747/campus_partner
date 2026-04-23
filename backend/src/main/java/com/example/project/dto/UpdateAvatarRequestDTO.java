package com.example.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新头像请求DTO
 * <p>
 * 封装用户更新头像接口的请求参数
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAvatarRequestDTO {

    /** 新头像的URL */
    private String avatar;
}
