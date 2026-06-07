package com.bbs.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;
// ...
/** 逻辑删除：0未删除 1已删除 */

/**
 * 帖子实体
 */
@TableName("post")
public class Post extends BaseEntity {

    private static final long serialVersionUID = 1L;
    /** 所属板块ID */
    private Long boardId;

    /** 作者ID */
    private Long userId;

    /** 标题 */
    private String title;

    /** 内容（富文本HTML） */
    private String content;

    /** 浏览量 */
    private Integer viewCount;

    /** 回复数 */
    private Integer replyCount;

    /** 置顶：0普通 1板块置顶 2全局置顶 */
    private Integer isTop;

    /** 精华：0否 1精华 */
    private Integer isEssence;

    /** 状态：0待审核 1正常 2违规下架 */
    private Integer status;

    /** 逻辑删除：0未删除 1已删除 */
    private Integer deleted;

    /** 最后回复时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastReplyTime;

    // ==================== Getter / Setter ====================

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

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

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }

    public Integer getIsTop() {
        return isTop;
    }

    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }

    public Integer getIsEssence() {
        return isEssence;
    }

    public void setIsEssence(Integer isEssence) {
        this.isEssence = isEssence;
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

    public LocalDateTime getLastReplyTime() {
        return lastReplyTime;
    }

    public void setLastReplyTime(LocalDateTime lastReplyTime) {
        this.lastReplyTime = lastReplyTime;
    }
}