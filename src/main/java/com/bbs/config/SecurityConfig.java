package com.bbs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security 配置
 * 关闭CSRF、禁用默认Session登录、放行所有请求（由JWT拦截器负责认证）
 * 仅暴露BCryptPasswordEncoder Bean供业务层使用
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 关闭CSRF（前后端分离 + JWT 无需CSRF防护）
                .csrf().disable()
                // 不创建Session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 放行所有请求（认证由JwtAuthenticationInterceptor处理）
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()
                // 禁用默认表单登录
                .formLogin().disable()
                // 禁用HTTP Basic认证
                .httpBasic().disable()
                // 禁用默认登出
                .logout().disable();
    }

    /**
     * BCrypt 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}