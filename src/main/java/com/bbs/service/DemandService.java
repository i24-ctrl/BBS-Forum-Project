package com.bbs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.dto.DemandDTO;
import com.bbs.dto.PageDTO;
import com.bbs.vo.DemandDetailVO;
import com.bbs.vo.DemandVO;

/**
 * 需求悬赏服务接口
 */
public interface DemandService {

    /**
     * 发布需求
     * 扣 user.score → user.frozen_score（乐观锁重试），记录积分流水 type=3
     *
     * @param dto    需求信息
     * @param userId 发布者ID
     * @return 需求ID
     */
    Long publish(DemandDTO dto, Long userId);

    /**
     * 分页查询需求列表
     *
     * @param pageDTO 分页参数
     * @param status  状态筛选（null=全部, 0=进行中, 1=已解决, 2=已关闭）
     * @return 分页结果
     */
    Page<DemandVO> listDemands(PageDTO pageDTO, Integer status);

    /**
     * 需求详情（含回复列表）
     */
    DemandDetailVO getDetail(Long demandId);

    /**
     * 回复需求
     */
    Long reply(Long demandId, String content, Long userId);

    /**
     * 采纳回复
     * 1. 校验当前用户是发布者
     * 2. 校验 demand.status=0
     * 3. 校验回复未被采纳
     * 4. 更新 demand status=1, resolverId, adoptReplyId, resolveTime
     * 5. 积分 transfer：fromUser.frozenScore -= amount, toUser.score += amount
     * 6. 记录两条 score_record
     *
     * @param demandId 需求ID
     * @param replyId  回复ID
     * @param userId   当前用户（发布者）
     */
    void adopt(Long demandId, Long replyId, Long userId);

    /**
     * 关闭需求
     * 仅发布者可关闭，status=0 → status=2
     * frozenScore 退回 score，记录 score_record type=4
     *
     * @param demandId 需求ID
     * @param userId   当前用户（发布者）
     */
    void close(Long demandId, Long userId);
}