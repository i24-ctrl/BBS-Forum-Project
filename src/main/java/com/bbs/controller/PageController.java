package com.bbs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面路由控制器
 * 负责将 URL 映射到 Thymeleaf 模板视图
 * 不需要 Swagger 注解（非 REST API）
 */
@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/register")
    public String register() {
        return "user/register";
    }

    @GetMapping("/profile")
    public String profile() {
        return "user/profile";
    }

    @GetMapping("/boards")
    public String boards() {
        return "board/board-list";
    }

    @GetMapping("/posts")
    public String posts() {
        return "post/post-list";
    }

    @GetMapping("/post-detail")
    public String postDetail() {
        return "post/post-detail";
    }

    @GetMapping("/post-form")
    public String postForm() {
        return "post/post-form";
    }

    @GetMapping("/demands")
    public String demands() {
        return "demand/demand-list";
    }

    @GetMapping("/demand-detail")
    public String demandDetail() {
        return "demand/demand-detail";
    }

    @GetMapping("/demand-form")
    public String demandForm() {
        return "demand/demand-form";
    }

    @GetMapping("/admin/user-list")
    public String adminUserList() {
        return "admin/user-list";
    }

    @GetMapping("/admin/post-list")
    public String adminPostList() {
        return "admin/post-list";
    }

    @GetMapping("/admin/board-list")
    public String adminBoardList() {
        return "admin/board-list";
    }
}