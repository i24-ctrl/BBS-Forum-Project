package com.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.entity.Demand;
import com.bbs.vo.DemandVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 需求悬赏 Mapper 接口
 */
@Mapper
public interface DemandMapper extends BaseMapper<Demand> {

    /**
     * 分页查询需求列表（含发布者用户名）
     *
     * @param page   分页对象
     * @param status 状态筛选（null 表示全部）
     * @return 需求VO分页结果
     */
    Page<DemandVO> selectDemandListWithUser(Page<DemandVO> page, @Param("status") Integer status);
}