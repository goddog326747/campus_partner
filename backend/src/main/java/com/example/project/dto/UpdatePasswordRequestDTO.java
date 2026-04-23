package com.example.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改密码请求DTO
 * <p>
 * 封装用户修改密码接口的请求参数
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordRequestDTO {

    /** 旧密码 */
    private String oldPassword;

    /** 新密码 */
    private String newPassword;
}
