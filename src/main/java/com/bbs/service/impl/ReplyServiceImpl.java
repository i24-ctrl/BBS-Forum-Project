package com.bbs.service.impl;

import com.bbs.common.BusinessException;
import com.bbs.dto.ReplyDTO;
import com.bbs.entity.Post;
import com.bbs.entity.Reply;
import com.bbs.mapper.PostMapper;
import com.bbs.mapper.ReplyMapper;
import com.bbs.service.ReplyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 回复服务实现类
 */
@Service
public class ReplyServiceImpl implements ReplyService {

    @Resource
    private ReplyMapper replyMapper;

    @Resource
    private PostMapper postMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReply(Long postId, ReplyDTO dto, Long userId) {
        // 校验帖子存在且正常
        Post post = postMapper.selectById(postId);
        if (post == null || post.getDeleted() == 1) {
            throw new BusinessException("帖子不存在");
        }
        if (post.getStatus() != 1) {
            throw new BusinessException("帖子已下架，无法回复");
        }

        // 校验 parentId：若不为 null，父回复必须是顶级回复（parent_id IS NULL），防止三级嵌套
        if (dto.getParentId() != null) {
            Reply parentReply = replyMapper.selectById(dto.getParentId());
            if (parentReply == null || parentReply.getDeleted() == 1) {
                throw new BusinessException("引用的回复不存在");
            }
            if (parentReply.getParentId() != null) {
                throw new BusinessException("不允许三级嵌套回复，请直接回复楼中楼");
            }
            if (!parentReply.getPostId().equals(postId)) {
                throw new BusinessException("引用的回复不属于该帖子");
            }
        }

        // FOR UPDATE 查询当前最大楼层号（悲观锁防并发）
        Integer maxFloor = replyMapper.selectMaxFloorByPostIdForUpdate(postId);
        int floor = (maxFloor == null) ? 1 : maxFloor + 1;

        // 插入回复
        Reply reply = new Reply();
        reply.setPostId(postId);
        reply.setUserId(userId);
        reply.setParentId(dto.getParentId());
        reply.setFloor(floor);
        reply.setContent(dto.getContent().trim());
        reply.setStatus(1);
        reply.setDeleted(0);   // ← 加这一行

        replyMapper.insert(reply);

        // 更新帖子回复数和最后回复时间
        postMapper.updateReplyCountAndTime(postId, LocalDateTime.now());

        return reply.getId();
    }
}