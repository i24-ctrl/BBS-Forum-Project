package com.bbs.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
// ...
/** 逻辑删除：0未删除 1已删除 */

/**
 * 回复实体
 */
@TableName("reply")
public class Reply extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 帖子ID */
    private Long postId;

    /** 回复者ID */
    private Long userId;

    /** 楼中楼父回复ID，NULL为一级回复 */
    private Long parentId;

    /** 楼层号 */
    private Integer floor;

    /** 回复内容 */
    private String content;

    /** 状态：0待审核 1正常 */
    private Integer status;

    /** 逻辑删除：0未删除 1已删除 */
    private Integer deleted;

    // ==================== Getter / Setter ====================

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
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