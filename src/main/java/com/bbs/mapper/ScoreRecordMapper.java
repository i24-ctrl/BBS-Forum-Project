package com.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bbs.entity.ScoreRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分记录 Mapper 接口
 */
@Mapper
public interface ScoreRecordMapper extends BaseMapper<ScoreRecord> {
}