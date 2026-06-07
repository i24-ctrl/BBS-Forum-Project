package com.bbs.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bbs.annotation.RequireRole;
import com.bbs.common.BusinessException;
import com.bbs.common.Result;
import com.bbs.entity.Board;
import com.bbs.mapper.BoardMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 管理员 - 板块管理控制器
 */
@Tag(name = "管理员-板块管理", description = "板块的CRUD、启用/禁用")
@RestController
@RequestMapping("/api/v1/admin/boards")
public class AdminBoardController {

    @Resource
    private BoardMapper boardMapper;

    @Operation(summary = "获取所有板块", description = "管理员视图，含禁用板块",
            security = @SecurityRequirement(name = "Bearer"))
    @GetMapping
    @RequireRole(1)
    public Result<List<Board>> listAll(HttpServletRequest request) {
        LambdaQueryWrapper<Board> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Board::getSortOrder)
                .orderByDesc(Board::getPostCount);
        List<Board> boards = boardMapper.selectList(queryWrapper);
        return Result.ok(boards);
    }

    @Operation(summary = "新增板块", description = "创建默认启用(status=1)的板块",
            security = @SecurityRequirement(name = "Bearer"))
    @PostMapping
    @RequireRole(1)
    public Result<Board> create(@RequestBody Map<String, Object> body,
                                HttpServletRequest request) {
        String name = (String) body.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("板块名称不能为空");
        }

        Board board = new Board();
        board.setName(name.trim());
        board.setDescription((String) body.getOrDefault("description", ""));
        board.setSortOrder(body.get("sortOrder") != null
                ? ((Number) body.get("sortOrder")).intValue() : 0);
        board.setStatus(1);
        board.setPostCount(0);
        board.setDeleted(0);
        boardMapper.insert(board);
        return Result.ok("创建成功", board);
    }

    @Operation(summary = "修改板块", description = "修改板块名称、描述、排序",
            security = @SecurityRequirement(name = "Bearer"))
    @PutMapping("/{id}")
    @RequireRole(1)
    public Result<Void> update(
            @Parameter(description = "板块ID") @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        Board board = boardMapper.selectById(id);
        if (board == null) {
            throw new BusinessException("板块不存在");
        }

        if (body.containsKey("name") && body.get("name") != null) {
            board.setName(((String) body.get("name")).trim());
        }
        if (body.containsKey("description")) {
            board.setDescription((String) body.getOrDefault("description", ""));
        }
        if (body.containsKey("sortOrder") && body.get("sortOrder") != null) {
            board.setSortOrder(((Number) body.get("sortOrder")).intValue());
        }
        boardMapper.updateById(board);
        return Result.okMsg("修改成功");
    }

    @Operation(summary = "启用/禁用板块", description = "status: 1启用/0禁用",
            security = @SecurityRequirement(name = "Bearer"))
    @PutMapping("/{id}/status")
    @RequireRole(1)
    public Result<Void> updateStatus(
            @Parameter(description = "板块ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam Integer status,
            HttpServletRequest request) {
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值无效，仅支持 0/1");
        }
        Board board = boardMapper.selectById(id);
        if (board == null) {
            throw new BusinessException("板块不存在");
        }
        board.setStatus(status);
        boardMapper.updateById(board);
        return Result.okMsg(status == 1 ? "已启用" : "已禁用");
    }

    @Operation(summary = "删除板块", description = "逻辑删除板块",
            security = @SecurityRequirement(name = "Bearer"))
    @DeleteMapping("/{id}")
    @RequireRole(1)
    public Result<Void> delete(
            @Parameter(description = "板块ID") @PathVariable Long id,
            HttpServletRequest request) {
        Board board = boardMapper.selectById(id);
        if (board == null) {
            throw new BusinessException("板块不存在");
        }
        boardMapper.deleteById(id);
        return Result.okMsg("删除成功");
    }
}