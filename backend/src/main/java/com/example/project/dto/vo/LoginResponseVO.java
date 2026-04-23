package com.example.project.dto.vo;

import com.example.project.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应VO
 * <p>
 * 封装用户登录接口的响应数据，包含用户认证token和用户信息
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseVO {

    /** JWT认证token */
    private String token;

    /** 用户信息 */
    private User userInfo;
}
