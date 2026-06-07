package com.bbs.controller;

import com.bbs.common.Result;
import com.bbs.dto.LoginDTO;
import com.bbs.dto.RegisterDTO;
import com.bbs.service.UserService;
import com.bbs.vo.TokenVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 认证控制器
 */
@Tag(name = "认证模块", description = "用户注册、登录、登出")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Resource
    private UserService userService;

    @Operation(summary = "用户注册", description = "注册成功后状态为待审核，需管理员审核通过后才能登录")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.okMsg("注册成功，请等待管理员审核");
    }

    @Operation(summary = "用户登录", description = "验证用户名密码，返回JWT Token，有效期7天")
    @PostMapping("/login")
    public Result<TokenVO> login(@Valid @RequestBody LoginDTO dto) {
        TokenVO tokenVO = userService.login(dto);
        return Result.ok("登录成功", tokenVO);
    }

    @Operation(summary = "用户登出", description = "前端清除localStorage中的token即可")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.okMsg("已登出");
    }
}