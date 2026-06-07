package com.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bbs.common.BusinessException;
import com.bbs.entity.ScoreRecord;
import com.bbs.entity.User;
import com.bbs.mapper.ScoreRecordMapper;
import com.bbs.mapper.UserMapper;
import com.bbs.service.ScoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 积分服务实现类
 * 乐观锁重试3次 + 记录真实balance
 */
@Service
public class ScoreServiceImpl implements ScoreService {

    private static final Logger log = LoggerFactory.getLogger(ScoreServiceImpl.class);

    private static final int MAX_RETRY = 3;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ScoreRecordMapper scoreRecordMapper;

    @Override
    public void updateScore(Long userId, int delta, int type,
                            String sourceType, Long sourceId, String remark) {
        int retryCount = 0;
        while (retryCount < MAX_RETRY) {
            retryCount++;
            try {
                doUpdateScore(userId, delta, type, sourceType, sourceId, remark);
                return;
            } catch (RuntimeException e) {
                if (retryCount >= MAX_RETRY) {
                    log.error("积分更新失败，已重试{}次 - userId:{}, delta:{}, type:{}",
                            MAX_RETRY, userId, delta, type, e);
                    throw new BusinessException("积分操作失败，请稍后重试");
                }
                log.warn("积分更新冲突，第{}次重试 - userId:{}, delta:{}", retryCount, userId, delta);
                try {
                    Thread.sleep(50L * retryCount);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException("积分操作被中断");
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void doUpdateScore(Long userId, int delta, int type,
                              String sourceType, Long sourceId, String remark) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        int oldVersion = user.getVersion();
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId).eq(User::getVersion, oldVersion);

        if (type == 3) {
            // 冻结：扣可用积分，加冻结积分
            if (user.getScore() < delta) {
                throw new BusinessException("可用积分不足");
            }
            updateWrapper.set(User::getScore, user.getScore() - delta)
                    .set(User::getFrozenScore, user.getFrozenScore() + delta);
        } else if (type == 4) {
            // 解冻：扣冻结积分，加可用积分
            if (user.getFrozenScore() < delta) {
                throw new BusinessException("冻结积分不足");
            }
            updateWrapper.set(User::getScore, user.getScore() + delta)
                    .set(User::getFrozenScore, user.getFrozenScore() - delta);
        } else if (type == 1) {
            // 获得：加可用积分
            updateWrapper.set(User::getScore, user.getScore() + delta);
        } else if (type == 2) {
            // 支出：扣冻结积分
            if (user.getFrozenScore() < delta) {
                throw new BusinessException("冻结积分不足");
            }
            updateWrapper.set(User::getFrozenScore, user.getFrozenScore() - delta);
        } else {
            throw new BusinessException("未知积分变动类型: " + type);
        }

        updateWrapper.set(User::getVersion, oldVersion + 1);

        int rows = userMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new RuntimeException("乐观锁冲突");
        }

        // 重新查询获取真实余额
        User updatedUser = userMapper.selectById(userId);
        insertScoreRecord(updatedUser, delta, type, sourceType, sourceId, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(Long fromUserId, Long toUserId, int amount,
                         String sourceType, Long sourceId) {
        // 扣发布者冻结积分
        doUpdateScore(fromUserId, amount, 2, sourceType, sourceId, "悬赏支出");
        // 加解决者可用积分
        doUpdateScore(toUserId, amount, 1, sourceType, sourceId, "悬赏获得");
    }

    /**
     * 记录积分流水，balance 使用更新后的真实值
     */
    private void insertScoreRecord(User user, int delta, int type,
                                   String sourceType, Long sourceId, String remark) {
        ScoreRecord record = new ScoreRecord();
        record.setUserId(user.getId());
        record.setType(type);
        record.setAmount(delta);
        record.setBalance(user.getScore());
        record.setSourceType(sourceType);
        record.setSourceId(sourceId);
        record.setRemark(remark);
        scoreRecordMapper.insert(record);
    }
}