package com.bbs.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一响应结果封装
 *
 * @param <T> 响应数据类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /** 状态码 */
    private int code;

    /** 消息 */
    private String msg;

    /** 数据 */
    private T data;

    /** 时间戳 */
    private long timestamp;

    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功响应（无数据 + 默认消息）
     */
    public static Result<Void> ok() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功响应（仅消息，无数据，返回 Result&lt;Void&gt;）
     * 专门用于无数据返回的操作，如删除/更新/状态变更等
     */
    public static Result<Void> okMsg(String msg) {
        return new Result<>(200, msg, null);
    }

    /**
     * 成功响应（自定义消息 + 数据）
     */
    public static <T> Result<T> ok(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 失败响应（默认400）
     */
    public static <T> Result<T> fail(String msg) {
        return new Result<>(400, msg, null);
    }

    // ==================== Getter / Setter ====================

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}