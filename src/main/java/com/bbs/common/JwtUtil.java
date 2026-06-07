package com.bbs.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 负责Token的生成、解析和验证
 * Token有效期：7天
 */
@Component
public class JwtUtil {

    /**
     * 密钥（Base64编码，256位以上）
     */
    private static final String SECRET_BASE64 =
            "YjNkYzQ1ZjY3ODkwYWJjZGVmMDEyMzQ1Njc4OTAxMjM0NTY3ODkwYWJjZGVmMDEyMzQ1Njc4OTAxMjM0NTY3ODkwYWJjZGVm";

    /**
     * SecretKey 实例
     */
    private final SecretKey secretKey;

    /**
     * Token有效期：7天（毫秒）
     */
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    public JwtUtil() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET_BASE64);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成JWT Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色
     * @return JWT Token字符串
     */
    public String generateToken(Long userId, String username, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析Token，获取Claims
     *
     * @param token JWT Token
     * @return Claims对象
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证Token是否有效（不抛出异常即有效）
     *
     * @param token JWT Token
     * @return true有效 / false无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从Token中获取角色
     *
     * @param token JWT Token
     * @return 角色
     */
    public Integer getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", Integer.class);
    }

    /**
     * 获取Token过期时间（毫秒时间戳）
     *
     * @return 过期时间
     */
    public long getExpiresIn() {
        return EXPIRATION / 1000;
    }
}