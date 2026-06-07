package com.bbs.common;

/**
 * 业务异常类
 * 用于在Service层抛出业务逻辑异常，由GlobalExceptionHandler统一捕获处理
 */
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private int code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}