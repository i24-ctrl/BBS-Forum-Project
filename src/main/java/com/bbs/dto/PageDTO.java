package com.bbs.dto;

/**
 * 分页请求参数 DTO
 */
public class PageDTO {

    /** 当前页码，默认 1 */
    private long current = 1;

    /** 每页大小，默认 10，最大 50 */
    private long size = 10;

    public PageDTO() {
    }

    public PageDTO(long current, long size) {
        this.current = current;
        this.size = size;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = Math.max(1, current);
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = Math.min(50, Math.max(1, size));
    }

    /**
     * 获取安全的分页大小
     */
    public long getSafeSize() {
        return Math.min(50, Math.max(1, this.size));
    }
}