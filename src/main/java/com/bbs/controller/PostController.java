package com.bbs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.common.BusinessException;
import com.bbs.common.Result;
import com.bbs.dto.PageDTO;
import com.bbs.dto.PostDTO;
import com.bbs.service.PostService;
import com.bbs.vo.PostDetailVO;
import com.bbs.vo.PostVO;
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
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 帖子控制器
 */
@Tag(name = "帖子模块", description = "帖子CRUD、列表、详情")
@RestController
@RequestMapping("/api/v1")
public class PostController {

    @Resource
    private PostService postService;

    @Operation(summary = "首页帖子列表", description = "全局置顶(isTop=2)优先，其次按创建时间倒序；essenceOnly=true 时仅返回精华帖")
    @GetMapping("/posts")
    public Result<Map<String, Object>> listHomePosts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "仅精华帖") @RequestParam(required = false, defaultValue = "false") boolean essenceOnly) {
        PageDTO pageDTO = new PageDTO(page, size);
        Page<PostVO> result = postService.listHomePosts(pageDTO, essenceOnly);
        Map<String, Object> data = wrapPage(result);
        return Result.ok(data);
    }

    @Operation(summary = "板块内帖子列表", description = "板块置顶优先，按最后回复时间倒序；essenceOnly=true 时仅返回精华帖")
    @GetMapping("/boards/{boardId}/posts")
    public Result<Map<String, Object>> listBoardPosts(
            @Parameter(description = "板块ID") @PathVariable Long boardId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "仅精华帖") @RequestParam(required = false, defaultValue = "false") boolean essenceOnly) {
        PageDTO pageDTO = new PageDTO(page, size);
        Page<PostVO> result = postService.listBoardPosts(boardId, pageDTO, essenceOnly);
        Map<String, Object> data = wrapPage(result);
        return Result.ok(data);
    }

    @Operation(summary = "帖子详情", description = "浏览量+1，返回内容含回复列表")
    @GetMapping("/posts/{id}")
    public Result<PostDetailVO> getPostDetail(@Parameter(description = "帖子ID") @PathVariable Long id) {
        PostDetailVO vo = postService.getPostDetail(id);
        return Result.ok(vo);
    }

    @Operation(summary = "发帖", description = "需登录，内容经Jsoup手动清洗防XSS",
            security = @SecurityRequirement(name = "Bearer"))
    @PostMapping("/posts")
    public Result<Map<String, Object>> createPost(
            @Valid @RequestBody PostDTO dto,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        Long postId = postService.createPost(dto, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("id", postId);
        return Result.ok("发帖成功", data);
    }

    @Operation(summary = "修改帖子", description = "仅作者本人或管理员可修改",
            security = @SecurityRequirement(name = "Bearer"))
    @PutMapping("/posts/{id}")
    public Result<Void> updatePost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Valid @RequestBody PostDTO dto,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        boolean isAdmin = isAdmin(request);
        postService.updatePost(id, dto, userId, isAdmin);
        return Result.okMsg("修改成功");
    }

    @Operation(summary = "删除帖子", description = "逻辑删除，仅作者本人或管理员可删除",
            security = @SecurityRequirement(name = "Bearer"))
    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        boolean isAdmin = isAdmin(request);
        postService.deletePost(id, userId, isAdmin);
        return Result.okMsg("删除成功");
    }

    // ==================== 私有工具方法 ====================

    private Map<String, Object> wrapPage(Page<?> page) {
        Map<String, Object> data = new HashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());
        data.put("current", page.getCurrent());
        data.put("size", page.getSize());
        data.put("pages", page.getPages());
        return data;
    }

    private Long getLoginUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }

    private boolean isAdmin(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("currentRole");
        return role != null && role == 1;
    }
}