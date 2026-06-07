package com.bbs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解
 * 标记在 Controller 方法上，由 AdminLogAspect 拦截并校验管理员身份
 * value 默认 1（管理员）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * 所需角色：0普通用户 1管理员
     */
    int value() default 1;
}