package com.bbs.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.TableLogic;
// ...
/** 逻辑删除：0未删除 1已删除 */

import java.time.LocalDateTime;

/**
 * 需求悬赏实体
 */
@TableName("demand")
public class Demand extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 发布者ID */
    private Long userId;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 悬赏积分 */
    private Integer score;

    /** 状态：0进行中 1已解决 2已关闭 */
    private Integer status;

    /** 解决者ID */
    private Long resolverId;

    /** 采纳的回复ID */
    private Long adoptReplyId;

    /** 解决时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resolveTime;

    /** 逻辑删除：0未删除 1已删除 */

    private Integer deleted;

    // ==================== Getter / Setter ====================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getResolverId() {
        return resolverId;
    }

    public void setResolverId(Long resolverId) {
        this.resolverId = resolverId;
    }

    public Long getAdoptReplyId() {
        return adoptReplyId;
    }

    public void setAdoptReplyId(Long adoptReplyId) {
        this.adoptReplyId = adoptReplyId;
    }

    public LocalDateTime getResolveTime() {
        return resolveTime;
    }

    public void setResolveTime(LocalDateTime resolveTime) {
        this.resolveTime = resolveTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}