package com.bbs.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
// ...
/** 逻辑删除：0未删除 1已删除 */

/**
 * 需求回复实体
 */
@TableName("demand_reply")
public class DemandReply extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 需求ID */
    private Long demandId;

    /** 回复者ID */
    private Long userId;

    /** 回复内容 */
    private String content;

    /** 状态 */
    private Integer status;

    /** 逻辑删除：0未删除 1已删除 */
    private Integer deleted;

    // ==================== Getter / Setter ====================

    public Long getDemandId() {
        return demandId;
    }

    public void setDemandId(Long demandId) {
        this.demandId = demandId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}