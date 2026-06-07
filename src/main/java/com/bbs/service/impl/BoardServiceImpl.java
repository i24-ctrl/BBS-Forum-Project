package com.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bbs.entity.Board;
import com.bbs.mapper.BoardMapper;
import com.bbs.service.BoardService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 板块服务实现类
 */
@Service
public class BoardServiceImpl implements BoardService {

    @Resource
    private BoardMapper boardMapper;

    @Override
    public List<Board> listAll() {
        LambdaQueryWrapper<Board> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Board::getStatus, 1)
                .orderByAsc(Board::getSortOrder)
                .orderByDesc(Board::getPostCount);
        return boardMapper.selectList(queryWrapper);
    }
}