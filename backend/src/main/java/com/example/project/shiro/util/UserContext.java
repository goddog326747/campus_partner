package com.example.project.shiro.util;

import com.example.project.entity.User;

/**
 * 用户上下文工具类
 * <p>
 * 基于ThreadLocal存储当前登录用户信息，用于在请求处理过程中传递用户信息
 * </p>
 *
 * @author system
 * @since 1.0
 */
public class UserContext {

    private static final ThreadLocal<User> userHolder = new ThreadLocal<>();

    /**
     * 设置当前线程的用户信息
     *
     * @param user 用户对象
     */
    public static void set(User user) {
        userHolder.set(user);
    }

    /**
     * 获取当前线程的用户信息
     *
     * @return 当前用户对象，未设置时返回null
     */
    public static User get() {
        return userHolder.get();
    }

    /**
     * 清除当前线程的用户信息
     * <p>
     * 建议在请求处理完成后调用，避免内存泄漏
     * </p>
     */
    public static void remove() {
        userHolder.remove();
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID，未登录返回null
     */
    public static Long getUserId() {
        User user = userHolder.get();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名，未登录返回null
     */
    public static String getUsername() {
        User user = userHolder.get();
        return user != null ? user.getUsername() : null;
    }
}
