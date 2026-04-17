package com.example.project.common;

import lombok.Data;

/**
 * 统一返回结果封装类
 * <p>
 * 用于封装API接口的返回结果，包含状态码、消息和数据
 * </p>
 *
 * @param <T> 数据类型
 * @author system
 * @since 1.0
 */
@Data
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    /**
     * 返回成功结果
     *
     * @param data 返回的数据
     * @param <T>  数据类型
     * @return 成功结果对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    /**
     * 返回成功结果（带自定义消息）
     *
     * @param msg  成功消息
     * @param data 返回的数据
     * @param <T>  数据类型
     * @return 成功结果对象
     */
    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    /**
     * 返回错误结果
     *
     * @param code 错误码
     * @param msg  错误消息
     * @param <T>  数据类型
     * @return 错误结果对象
     */
    public static <T> Result<T> error(int code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
