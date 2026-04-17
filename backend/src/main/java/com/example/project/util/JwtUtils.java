package com.example.project.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT工具类
 * <p>
 * 提供JWT令牌的生成、验证和解析功能，用于用户认证和授权
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Component
public class JwtUtils {

    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;
    private static final String SECRET = "MySecretKey123456";

    /**
     * 生成JWT令牌
     *
     * @param username 用户名
     * @param userId   用户ID
     * @return 生成的JWT令牌，生成失败返回null
     */
    public static String sign(String username, Long userId) {
        try {
            Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            return JWT.create()
                    .withClaim("username", username)
                    .withClaim("userId", userId)
                    .withExpiresAt(date)
                    .sign(algorithm);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 生成JWT令牌（兼容旧版本，只传入用户名）
     *
     * @param username 用户名
     * @return 生成的JWT令牌，生成失败返回null
     */
    public static String sign(String username) {
        return sign(username, null);
    }

    /**
     * 验证JWT令牌是否有效
     *
     * @param token JWT令牌
     * @return 有效返回true，否则返回false
     */
    public static boolean verify(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * 从JWT令牌中获取用户名
     * <p>
     * 无需密钥即可解析
     * </p>
     *
     * @param token JWT令牌
     * @return 用户名，解析失败返回null
     */
    public static String getUsername(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("username").asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 从JWT令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID，解析失败返回null
     */
    public static Long getUserId(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("userId").asLong();
        } catch (JWTDecodeException e) {
            return null;
        }
    }
}
