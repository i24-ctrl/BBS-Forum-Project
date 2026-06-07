package com.bbs.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 配置（Swagger UI）
 * 访问地址：http://localhost:8080/swagger-ui.html
 * API文档JSON：http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校内BBS论坛系统 API")
                        .description("基于 Spring Boot 2.7 + MyBatis-Plus 3.5.7 的校内社区平台\n\n"
                                + "**测试账号：**\n"
                                + "- 管理员：admin / admin123\n"
                                + "- 普通用户：testuser / 123456\n"
                                + "- 待审核：newuser / 123456")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("开发小组")
                                .email("bbs@school.edu.cn"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                // JWT 认证配置
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .name("Bearer")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("输入 JWT Token（登录接口返回的 token 字段值）")));
    }
}