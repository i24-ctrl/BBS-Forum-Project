package com.bbs.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;

// ...

/** 逻辑删除：0未删除 1已删除 */

/**
 * 板块实体
 */
@TableName("board")
public class Board extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 板块名称 */
    private String name;

    /** 板块描述 */
    private String description;

    /** 图标 */
    private String icon;

    /** 排序顺序 */
    private Integer sortOrder;

    /** 帖子数 */
    private Integer postCount;

    /** 状态：0禁用 1正常 */
    private Integer status;

    /** 逻辑删除：0未删除 1已删除 */
    private Integer deleted;
    // ==================== Getter / Setter ====================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getPostCount() {
        return postCount;
    }

    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
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