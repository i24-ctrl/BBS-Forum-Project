package com.bbs.service;

import com.bbs.dto.ReplyDTO;

/**
 * 回复服务接口
 */
public interface ReplyService {

    /**
     * 创建回复（@Transactional）
     * 1. 校验 parentId 只支持一级嵌套
     * 2. FOR UPDATE 查最大楼层
     * 3. floor = maxFloor + 1
     * 4. 插入回复
     * 5. 更新帖子 reply_count + 1, last_reply_time = now
     *
     * @param postId 帖子ID
     * @param dto    回复内容
     * @param userId 回复者ID
     * @return 回复ID
     */
    Long createReply(Long postId, ReplyDTO dto, Long userId);
}