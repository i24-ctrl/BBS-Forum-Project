package com.bbs.service;

import com.bbs.entity.Board;

import java.util.List;

/**
 * 板块服务接口
 */
public interface BoardService {

    /**
     * 查询所有正常状态的板块，按 sort_order 升序；相同时 post_count 降序
     *
     * @return 板块列表
     */
    List<Board> listAll();
}