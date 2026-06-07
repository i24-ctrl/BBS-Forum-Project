package com.bbs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 资源归属校验注解（标记用）
 * 实际校验逻辑在 Service 层通过比对 userId 实现
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireOwner {

    /**
     * 资源类型，如 "post"、"demand"
     */
    String resourceType() default "";
}