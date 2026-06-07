package com.bbs.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 发布需求请求 DTO
 */
public class DemandDTO {

    @NotBlank(message = "标题不能为空")
    @Size(min = 3, max = 200, message = "标题长度3-200位")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @Min(value = 1, message = "悬赏积分至少为1")
    private Integer score;

    // ==================== Getter / Setter ====================

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}