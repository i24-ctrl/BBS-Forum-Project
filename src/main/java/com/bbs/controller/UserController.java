package com.bbs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.common.Result;
import com.bbs.entity.ScoreRecord;
import com.bbs.service.UserService;
import com.bbs.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户控制器
 */
@Tag(name = "用户模块", description = "个人资料查看/编辑、积分流水查询")
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Resource
    private UserService userService;

    @Operation(summary = "获取当前用户资料", description = "需要登录",
            security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/profile")
    public Result<UserVO> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        UserVO userVO = userService.getProfile(userId);
        return Result.ok(userVO);
    }

    @Operation(summary = "更新个人资料", description = "仅允许更新realName/phone/email/workPlace/jobNature/avatar",
            security = @SecurityRequirement(name = "Bearer"))
    @PutMapping("/profile")
    public Result<Void> updateProfile(HttpServletRequest request,
                                      @RequestBody Map<String, Object> updates) {
        Long userId = (Long) request.getAttribute("currentUserId");
        userService.updateProfile(userId, updates);
        return Result.okMsg("资料更新成功");
    }

    @Operation(summary = "查询积分流水", description = "分页查询当前用户的积分变动记录",
            security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/score")
    public Result<Page<ScoreRecord>> getScoreRecords(
            HttpServletRequest request,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("currentUserId");
        Page<ScoreRecord> records = userService.getScoreRecords(userId, page, size);
        return Result.ok(records);
    }
}