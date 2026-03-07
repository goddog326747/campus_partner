package com.example.project.util;

import com.example.project.entity.User;

/**
 * 用户上下文工具类，基于ThreadLocal存储当前登录用户信息
 */
public class UserContext {

    private static final ThreadLocal<User> userHolder = new ThreadLocal<>();

    public static void set(User user) {
        userHolder.set(user);
    }

    public static User get() {
        return userHolder.get();
    }

    public static void remove() {
        userHolder.remove();
    }
}
