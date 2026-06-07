package com.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bbs.entity.Board;
import org.apache.ibatis.annotations.Mapper;

/**
 * 板块 Mapper 接口
 */
@Mapper
public interface BoardMapper extends BaseMapper<Board> {
}