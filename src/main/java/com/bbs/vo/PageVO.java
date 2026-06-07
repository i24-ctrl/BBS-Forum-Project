package com.bbs.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果视图对象
 */
public class PageVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 数据列表 */
    private List<T> records;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private long current;

    /** 每页大小 */
    private long size;

    /** 总页数 */
    private long pages;

    public PageVO() {
    }

    public PageVO(List<T> records, long total, long current, long size, long pages) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = pages;
    }

    // ==================== Getter / Setter ====================

    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getCurrent() { return current; }
    public void setCurrent(long current) { this.current = current; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public long getPages() { return pages; }
    public void setPages(long pages) { this.pages = pages; }
}