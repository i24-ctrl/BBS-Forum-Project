package com.bbs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.annotation.RequireRole;
import com.bbs.common.BusinessException;
import com.bbs.common.Result;
import com.bbs.entity.Board;
import com.bbs.entity.Post;
import com.bbs.mapper.BoardMapper;
import com.bbs.mapper.PostMapper;
import com.bbs.vo.PostVO;
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
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员 - 帖子管理控制器
 */
@Tag(name = "管理员-帖子管理", description = "置顶、加精、物理删除帖子")
@RestController
@RequestMapping("/api/v1/admin/posts")
public class AdminPostController {

    @Resource
    private PostMapper postMapper;

    @Resource
    private BoardMapper boardMapper;

    @Operation(summary = "帖子管理列表", description = "管理员视图，包含所有状态的帖子",
            security = @SecurityRequirement(name = "Bearer"))
    @GetMapping
    @RequireRole(1)
    public Result<Map<String, Object>> listAllPosts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status) {

        Page<PostVO> pageParam = new Page<>(page, size);
        Page<PostVO> result = postMapper.selectAdminPostListWithAuthor(pageParam);

        if (status != null) {
            result.getRecords().removeIf(vo -> !status.equals(vo.getStatus()));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        data.put("pages", result.getPages());
        return Result.ok(data);
    }

    @Operation(summary = "违规下架帖子", description = "将帖子状态改为违规下架",
            security = @SecurityRequirement(name = "Bearer"))
    @PutMapping("/{id}/offline")
    @RequireRole(1)
    public Result<Void> markOffline(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            HttpServletRequest request) {
        Post post = postMapper.selectById(id);
        if (post == null || post.getDeleted() == 1) {
            throw new BusinessException("帖子不存在");
        }
        if (post.getStatus() != null && post.getStatus() == 2) {
            return Result.okMsg("帖子已是违规下架状态");
        }

        post.setStatus(2);
        postMapper.updateById(post);
        return Result.okMsg("帖子已违规下架");
    }

    @Operation(summary = "帖子置顶", description = "type: 0取消置顶/1板块置顶/2全局置顶",
            security = @SecurityRequirement(name = "Bearer"))
    @PutMapping("/{id}/top")
    @RequireRole(1)
    public Result<Void> updateTop(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Parameter(description = "置顶类型") @RequestParam Integer type,
            HttpServletRequest request) {
        if (type < 0 || type > 2) {
            throw new BusinessException("置顶类型无效，仅支持 0/1/2");
        }

        Post post = postMapper.selectById(id);
        if (post == null || post.getDeleted() == 1) {
            throw new BusinessException("帖子不存在");
        }

        post.setIsTop(type);
        postMapper.updateById(post);

        String msg = type == 2 ? "已设为全局置顶" : type == 1 ? "已设为板块置顶" : "已取消置顶";
        return Result.okMsg(msg);
    }

    @Operation(summary = "帖子加精", description = "flag: 1加精/0取消精华",
            security = @SecurityRequirement(name = "Bearer"))
    @PutMapping("/{id}/essence")
    @RequireRole(1)
    public Result<Void> updateEssence(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Parameter(description = "精华标志") @RequestParam Integer flag,
            HttpServletRequest request) {
        if (flag != 0 && flag != 1) {
            throw new BusinessException("精华标志无效，仅支持 0/1");
        }

        Post post = postMapper.selectById(id);
        if (post == null || post.getDeleted() == 1) {
            throw new BusinessException("帖子不存在");
        }

        post.setIsEssence(flag);
        postMapper.updateById(post);

        String msg = flag == 1 ? "已设为精华" : "已取消精华";
        return Result.okMsg(msg);
    }

    @Operation(summary = "恢复帖子正常", description = "将违规下架帖子恢复为正常状态",
            security = @SecurityRequirement(name = "Bearer"))
    @PutMapping("/{id}/restore")
    @RequireRole(1)
    public Result<Void> restorePost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            HttpServletRequest request) {
        Post post = postMapper.selectById(id);
        if (post == null || post.getDeleted() == 1) {
            throw new BusinessException("帖子不存在");
        }
        if (post.getStatus() == null || post.getStatus() == 1) {
            return Result.okMsg("该帖子已是正常状态");
        }

        post.setStatus(1);
        postMapper.updateById(post);
        return Result.okMsg("帖子已恢复正常");
    }

    @Operation(summary = "物理删除帖子", description = "彻底删除，同时扣减板块帖子数",
            security = @SecurityRequirement(name = "Bearer"))
    @DeleteMapping("/{id}")
    @RequireRole(1)
    public Result<Void> deletePost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            HttpServletRequest request) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        Long boardId = post.getBoardId();
        postMapper.deleteById(id);

        // 扣减板块帖子数
        Board board = boardMapper.selectById(boardId);
        if (board != null && board.getPostCount() > 0) {
            Board updateBoard = new Board();
            updateBoard.setId(boardId);
            updateBoard.setPostCount(board.getPostCount() - 1);
            boardMapper.updateById(updateBoard);
        }

        return Result.okMsg("帖子已彻底删除");
    }
}