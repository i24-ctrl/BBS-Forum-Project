package com.bbs.service;

/**
 * 积分服务接口
 */
public interface ScoreService {

    /**
     * 更新积分（乐观锁重试3次，记录积分流水）
     *
     * @param userId     用户ID
     * @param delta      变动数量（正数增加，负数减少）
     * @param type       类型：1获得 2支出 3冻结 4解冻
     * @param sourceType 来源类型 demand/post/system
     * @param sourceId   来源ID
     * @param remark     备注
     */
    void updateScore(Long userId, int delta, int type,
                     String sourceType, Long sourceId, String remark);

    /**
     * 积分转账（事务内：扣发布者冻结积分 + 加解决者可用积分）
     *
     * @param fromUserId 发布者ID（扣 frozen_score）
     * @param toUserId   解决者ID（加 score）
     * @param amount     金额
     * @param sourceType 来源类型
     * @param sourceId   来源ID
     */
    void transfer(Long fromUserId, Long toUserId, int amount,
                  String sourceType, Long sourceId);
}