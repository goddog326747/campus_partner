package com.example.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求DTO
 * <p>
 * 封装用户登录接口的请求参数
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    /** 用户名 */
    private String username;

    /** 密码 */
    private String password;
}
