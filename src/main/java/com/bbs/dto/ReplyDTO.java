package com.bbs.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 回复请求 DTO
 */
public class ReplyDTO {

    @NotBlank(message = "回复内容不能为空")
    @Size(min = 1, max = 10000, message = "回复内容过长")
    private String content;

    /** 楼中楼父回复ID，NULL 表示一级回复 */
    private Long parentId;

    // ==================== Getter / Setter ====================

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}