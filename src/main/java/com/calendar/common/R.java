package com.calendar.common;

import lombok.Data;

/**
 * 通用接口返回结果
 * <p>
 * 统一封装 API 响应格式，所有 Controller 返回值均使用此类
 *
 * @param <T> 数据类型
 */
@Data
public class R<T> {

    /** 状态码：200-成功，500-服务端错误 */
    private int code;

    /** 响应消息 */
    private String msg;

    /** 响应数据 */
    private T data;

    /**
     * 成功返回（含数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 统一返回结果
     */
    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("操作成功");
        r.setData(data);
        return r;
    }

    /**
     * 成功返回（自定义消息）
     *
     * @param msg 自定义消息
     * @param <T> 数据类型
     * @return 统一返回结果
     */
    public static <T> R<T> success(String msg) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg(msg);
        return r;
    }

    /**
     * 错误返回
     *
     * @param msg 错误消息
     * @param <T> 数据类型
     * @return 统一返回结果
     */
    public static <T> R<T> error(String msg) {
        R<T> r = new R<>();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }

    /**
     * 自定义错误码返回
     *
     * @param code 状态码
     * @param msg  错误消息
     * @param <T>  数据类型
     * @return 统一返回结果
     */
    public static <T> R<T> error(int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
