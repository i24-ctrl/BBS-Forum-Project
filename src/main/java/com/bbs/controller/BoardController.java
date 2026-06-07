package com.bbs.controller;

import com.bbs.common.Result;
import com.bbs.entity.Board;
import com.bbs.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 板块控制器
 */
@Tag(name = "板块模块", description = "板块列表查询")
@RestController
@RequestMapping("/api/v1/boards")
public class BoardController {

    @Resource
    private BoardService boardService;

    @Operation(summary = "获取所有启用的板块", description = "按 sort_order 升序；相同时帖子数多的靠前")
    @GetMapping
    public Result<List<Board>> listBoards() {
        List<Board> boards = boardService.listAll();
        return Result.ok(boards);
    }
}