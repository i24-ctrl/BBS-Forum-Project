package com.bbs.vo;

import java.io.Serializable;

/**
 * 登录令牌视图对象
 */
public class TokenVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** JWT令牌 */
    private String token;

    /** 过期时间（秒） */
    private long expiresIn;

    public TokenVO() {
    }

    public TokenVO(String token, long expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
    }

    // ==================== Getter / Setter ====================

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}