package com.bbs.aspect;

import com.bbs.annotation.RequireRole;
import com.bbs.common.BusinessException;
import com.bbs.entity.AdminLog;
import com.bbs.mapper.AdminLogMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 管理员操作日志 AOP 切面
 * 拦截 @RequireRole 注解的方法，校验管理员权限并记录操作日志
 */
@Aspect
@Component
public class AdminLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AdminLogAspect.class);

    @Resource
    private AdminLogMapper adminLogMapper;

    /**
     * 环绕通知：拦截 @RequireRole 注解的方法
     */
    @Around("@annotation(com.bbs.annotation.RequireRole)")
    public Object aroundAdminMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException(500, "无法获取请求上下文");
        }
        HttpServletRequest request = attributes.getRequest();

        // 获取当前用户信息
        Long adminId = (Long) request.getAttribute("currentUserId");
        Integer role = (Integer) request.getAttribute("currentRole");
        if (adminId == null || role == null || role != 1) {
            throw new BusinessException(403, "无管理员权限");
        }

        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole requireRole = method.getAnnotation(RequireRole.class);

        // 构建日志对象
        AdminLog adminLog = new AdminLog();
        adminLog.setAdminId(adminId);

        // 从方法名推断 action
        String methodName = method.getName();
        adminLog.setAction(buildAction(methodName, joinPoint.getArgs()));

        // 从参数推断 targetType 和 targetId
        adminLog.setTargetType(inferTargetType(methodName));
        adminLog.setTargetId(extractTargetId(joinPoint.getArgs()));

        // 记录 IP
        String ip = getClientIp(request);
        adminLog.setIp(ip);

        // 记录旧值（执行前）
        adminLog.setOldValue(null);

        // 执行目标方法
        Object result;
        try {
            result = joinPoint.proceed();
            adminLog.setNewValue("操作成功");
        } catch (Exception e) {
            adminLog.setNewValue("操作失败: " + e.getMessage());
            // 记录失败日志
            try {
                adminLogMapper.insert(adminLog);
            } catch (Exception ex) {
                log.error("记录管理员日志失败", ex);
            }
            throw e;
        }

        // 记录成功日志
        try {
            adminLogMapper.insert(adminLog);
            log.info("管理员操作日志 - adminId: {}, action: {}, targetType: {}, targetId: {}, ip: {}",
                    adminId, adminLog.getAction(), adminLog.getTargetType(), adminLog.getTargetId(), ip);
        } catch (Exception e) {
            log.error("记录管理员日志失败", e);
        }

        return result;
    }

    /**
     * 根据方法名构建操作描述
     */
    private String buildAction(String methodName, Object[] args) {
        if (methodName.contains("top") || methodName.contains("Top")) {
            return "帖子置顶操作";
        }
        if (methodName.contains("essence") || methodName.contains("Essence")) {
            return "帖子精华操作";
        }
        if (methodName.contains("delete") || methodName.contains("Delete")) {
            return "删除操作";
        }
        if (methodName.contains("status") || methodName.contains("Status")) {
            return "修改状态";
        }
        return "管理员操作";
    }

    /**
     * 根据方法名推断操作对象类型
     */
    private String inferTargetType(String methodName) {
        if (methodName.contains("post") || methodName.contains("Post")) {
            return "post";
        }
        if (methodName.contains("user") || methodName.contains("User")) {
            return "user";
        }
        if (methodName.contains("board") || methodName.contains("Board")) {
            return "board";
        }
        if (methodName.contains("demand") || methodName.contains("Demand")) {
            return "demand";
        }
        return "unknown";
    }

    /**
     * 从参数中提取目标ID（取第一个 Long 类型参数）
     */
    private Long extractTargetId(Object[] args) {
        if (args == null) return null;
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        return null;
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}