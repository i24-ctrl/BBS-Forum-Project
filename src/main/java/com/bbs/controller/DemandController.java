package com.bbs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.common.BusinessException;
import com.bbs.common.Result;
import com.bbs.dto.DemandDTO;
import com.bbs.dto.PageDTO;
import com.bbs.service.DemandService;
import com.bbs.vo.DemandDetailVO;
import com.bbs.vo.DemandVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * 需求悬赏控制器
 */
@Tag(name = "需求悬赏模块", description = "需求发布、回复、采纳、关闭")
@RestController
@RequestMapping("/api/v1/demands")
public class DemandController {

    @Resource
    private DemandService demandService;

    @Operation(summary = "发布需求", description = "扣可用积分到冻结积分（乐观锁重试），status=0进行中",
            security = @SecurityRequirement(name = "Bearer"))
    @PostMapping
    public Result<Map<String, Object>> publish(@Valid @RequestBody DemandDTO dto,
                                               HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        Long demandId = demandService.publish(dto, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("id", demandId);
        return Result.ok("需求发布成功，积分已冻结", data);
    }

    @Operation(summary = "需求列表", description = "支持status筛选：0进行中/1已解决/2已关闭，不传则查全部")
    @GetMapping
    public Result<Map<String, Object>> listDemands(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status) {
        PageDTO pageDTO = new PageDTO(page, size);
        Page<DemandVO> result = demandService.listDemands(pageDTO, status);
        Map<String, Object> data = wrapPage(result);
        return Result.ok(data);
    }

    @Operation(summary = "需求详情", description = "含回复列表，采纳的回复会高亮标记")
    @GetMapping("/{id}")
    public Result<DemandDetailVO> getDetail(@Parameter(description = "需求ID") @PathVariable Long id) {
        DemandDetailVO vo = demandService.getDetail(id);
        return Result.ok(vo);
    }

    @Operation(summary = "回复需求", description = "仅进行中的需求可回复",
            security = @SecurityRequirement(name = "Bearer"))
    @PostMapping("/{id}/reply")
    public Result<Map<String, Object>> reply(
            @Parameter(description = "需求ID") @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("回复内容不能为空");
        }
        Long replyId = demandService.reply(id, content.trim(), userId);
        Map<String, Object> data = new HashMap<>();
        data.put("id", replyId);
        return Result.ok("回复成功", data);
    }

    @Operation(summary = "采纳回复", description = "仅发布者可采纳，积分从发布者冻结→解决者可用",
            security = @SecurityRequirement(name = "Bearer"))
    @PutMapping("/{id}/adopt/{replyId}")
    public Result<Void> adopt(
            @Parameter(description = "需求ID") @PathVariable Long id,
            @Parameter(description = "回复ID") @PathVariable Long replyId,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        demandService.adopt(id, replyId, userId);
        return Result.okMsg("已采纳该回复，悬赏积分已发放");
    }

    @Operation(summary = "关闭需求", description = "仅发布者可关闭，冻结积分退回可用积分",
            security = @SecurityRequirement(name = "Bearer"))
    @PutMapping("/{id}/close")
    public Result<Void> close(
            @Parameter(description = "需求ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        demandService.close(id, userId);
        return Result.okMsg("需求已关闭，冻结积分已退回");
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
}