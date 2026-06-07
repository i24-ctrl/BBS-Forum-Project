package com.bbs.controller;

import com.bbs.common.BusinessException;
import com.bbs.common.Result;
import com.bbs.dto.ReplyDTO;
import com.bbs.service.ReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 回复控制器
 */
@Tag(name = "回复模块", description = "帖子回复（含楼中楼）")
@RestController
@RequestMapping("/api/v1")
public class ReplyController {

    @Resource
    private ReplyService replyService;

    @Operation(summary = "回复帖子", description = "parentId为null是一级回复，非null是楼中楼（仅支持一级嵌套）",
            security = @SecurityRequirement(name = "Bearer"))
    @PostMapping("/posts/{postId}/reply")
    public Result<Map<String, Object>> createReply(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @Valid @RequestBody ReplyDTO dto,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }

        Long replyId = replyService.createReply(postId, dto, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("id", replyId);
        return Result.ok("回复成功", data);
    }
}