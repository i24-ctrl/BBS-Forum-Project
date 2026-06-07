package com.bbs.interceptor;

import com.alibaba.fastjson.JSON;
import com.bbs.common.JwtUtil;
import com.bbs.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * JWT 认证拦截器
 * GET/HEAD/OPTIONS 请求直接放行（公开读取）
 * POST/PUT/DELETE 请求校验 Header 中的 Bearer Token
 * 校验通过后将 userId、username、role 存入 request Attribute
 */
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationInterceptor.class);

    public static final String USER_ID_KEY = "currentUserId";
    public static final String USERNAME_KEY = "currentUsername";
    public static final String ROLE_KEY = "currentRole";

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // OPTIONS 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // GET / HEAD 请求放行（公开读取），不校验 Token
        String method = request.getMethod().toUpperCase();
        if ("GET".equals(method) || "HEAD".equals(method)) {
            // 仍尝试解析 Token 以便可选地注入用户信息
            tryInjectUserAttributes(request);
            return true;
        }

        // POST / PUT / DELETE 需要认证
        String authHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "未登录，请先登录");
            return false;
        }

        String token = authHeader.substring(7).trim();

        if (!jwtUtil.validateToken(token)) {
            sendUnauthorized(response, "Token无效或已过期，请重新登录");
            return false;
        }

        try {
            Long userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);
            Integer role = jwtUtil.getRole(token);

            request.setAttribute(USER_ID_KEY, userId);
            request.setAttribute(USERNAME_KEY, username);
            request.setAttribute(ROLE_KEY, role);

            log.debug("JWT认证通过 - userId: {}, username: {}, role: {}", userId, username, role);
        } catch (Exception e) {
            log.warn("JWT解析用户信息失败: {}", e.getMessage());
            sendUnauthorized(response, "Token解析失败");
            return false;
        }

        return true;
    }

    /**
     * 尝试从 Token 注入用户属性（用于公开 GET 请求的可选用户识别）
     */
    private void tryInjectUserAttributes(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (jwtUtil.validateToken(token)) {
                try {
                    request.setAttribute(USER_ID_KEY, jwtUtil.getUserId(token));
                    request.setAttribute(USERNAME_KEY, jwtUtil.getUsername(token));
                    request.setAttribute(ROLE_KEY, jwtUtil.getRole(token));
                } catch (Exception ignored) {
                    // Token 无效时忽略，不阻塞公开访问
                }
            }
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.fail(401, message);
        PrintWriter writer = response.getWriter();
        writer.write(JSON.toJSONString(result));
        writer.flush();
        writer.close();
    }
}