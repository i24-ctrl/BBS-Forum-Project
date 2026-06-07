package com.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bbs.entity.DemandReply;
import com.bbs.vo.DemandReplyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 需求回复 Mapper 接口
 */
@Mapper
public interface DemandReplyMapper extends BaseMapper<DemandReply> {

    /**
     * 查询需求下所有回复（含用户信息）
     *
     * @param demandId 需求ID
     * @return 回复VO列表
     */
    List<DemandReplyVO> selectReplyListWithUserByDemandId(@Param("demandId") Long demandId);
}