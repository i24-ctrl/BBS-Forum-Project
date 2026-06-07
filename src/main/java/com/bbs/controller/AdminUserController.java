package com.bbs.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.common.BusinessException;
import com.bbs.common.Result;
import com.bbs.entity.User;
import com.bbs.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 管理员 - 用户管理控制器
 */
@Tag(name = "管理员-用户管理", description = "用户审核、状态变更、删除")
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    @Resource
    private UserMapper userMapper;

    @Operation(summary = "分页查询用户列表", description = "可按status筛选：0待审核/1正常/2禁用",
            security = @SecurityRequirement(name = "Bearer"))
    @GetMapping
    public Result<Page<User>> listUsers(
            HttpServletRequest request,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status) {

        Integer role = (Integer) request.getAttribute("currentRole");
        if (role == null || role != 1) {
            throw new BusinessException(403, "无权限访问");
        }

        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }
        queryWrapper.orderByDesc(User::getCreateTime);

        Page<User> result = userMapper.selectPage(pageParam, queryWrapper);
        return Result.ok(result);
    }

    @Operation(summary = "修改用户状态", description = "审核通过(status=1)/禁用(status=2)/恢复",
            security = @SecurityRequirement(name = "Bearer"))
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(
            HttpServletRequest request,
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "状态 0/1/2") @RequestParam Integer status) {

        Integer role = (Integer) request.getAttribute("currentRole");
        if (role == null || role != 1) {
            throw new BusinessException(403, "无权限访问");
        }

        if (status < 0 || status > 2) {
            throw new BusinessException("状态值无效，仅支持 0/1/2");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setStatus(status);
        userMapper.updateById(user);

        String msg = status == 1 ? "审核通过" : status == 2 ? "已禁用" : "状态已更新";
        return Result.okMsg(msg);
    }

    @Operation(summary = "删除待审核用户", description = "仅允许删除status=0的待审核用户",
            security = @SecurityRequirement(name = "Bearer"))
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(
            HttpServletRequest request,
            @Parameter(description = "用户ID") @PathVariable Long id) {

        Integer role = (Integer) request.getAttribute("currentRole");
        if (role == null || role != 1) {
            throw new BusinessException(403, "无权限访问");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getStatus() != 0) {
            throw new BusinessException("仅允许删除待审核状态的用户");
        }

        userMapper.deleteById(id);
        return Result.okMsg("删除成功");
    }
}