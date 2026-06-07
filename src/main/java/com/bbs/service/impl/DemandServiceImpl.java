package com.bbs.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.common.BusinessException;
import com.bbs.dto.DemandDTO;
import com.bbs.dto.PageDTO;
import com.bbs.entity.Demand;
import com.bbs.entity.DemandReply;
import com.bbs.entity.User;
import com.bbs.mapper.DemandMapper;
import com.bbs.mapper.DemandReplyMapper;
import com.bbs.mapper.UserMapper;
import com.bbs.service.DemandService;
import com.bbs.service.ScoreService;
import com.bbs.vo.DemandDetailVO;
import com.bbs.vo.DemandReplyVO;
import com.bbs.vo.DemandVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 需求悬赏服务实现类
 */
@Service
public class DemandServiceImpl implements DemandService {

    @Resource
    private DemandMapper demandMapper;

    @Resource
    private DemandReplyMapper demandReplyMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ScoreService scoreService;

    private static final String STATUS_STR_0 = "进行中";
    private static final String STATUS_STR_1 = "已解决";
    private static final String STATUS_STR_2 = "已关闭";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(DemandDTO dto, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getScore() < dto.getScore()) {
            throw new BusinessException("可用积分不足，当前积分: " + user.getScore());
        }

        scoreService.updateScore(userId, dto.getScore(), 3,
                "demand", null, "发布需求【" + dto.getTitle() + "】冻结积分");

        Demand demand = new Demand();
        demand.setUserId(userId);
        demand.setTitle(dto.getTitle().trim());
        demand.setContent(dto.getContent());
        demand.setScore(dto.getScore());
        demand.setStatus(0);
        demand.setDeleted(0);   // ← 加这一行
        demandMapper.insert(demand);

        return demand.getId();
    }

    @Override
    public Page<DemandVO> listDemands(PageDTO pageDTO, Integer status) {
        Page<DemandVO> page = new Page<>(pageDTO.getCurrent(), pageDTO.getSafeSize());
        Page<DemandVO> result = demandMapper.selectDemandListWithUser(page, status);
        if (result.getRecords() != null) {
            result.getRecords().forEach(vo -> vo.setStatusStr(statusToStr(vo.getStatus())));
        }
        return result;
    }

    @Override
    public DemandDetailVO getDetail(Long demandId) {
        Demand demand = demandMapper.selectById(demandId);
        if (demand == null || demand.getDeleted() == 1) {
            throw new BusinessException("需求不存在");
        }

        DemandDetailVO vo = new DemandDetailVO();
        BeanUtils.copyProperties(demand, vo);
        vo.setStatusStr(statusToStr(demand.getStatus()));

        User author = userMapper.selectById(demand.getUserId());
        if (author != null) {
            vo.setAuthorName(author.getUsername());
            vo.setAuthorAvatar(author.getAvatar());
        }

        if (demand.getResolverId() != null) {
            User resolver = userMapper.selectById(demand.getResolverId());
            if (resolver != null) {
                vo.setResolverName(resolver.getUsername());
            }
        }

        List<DemandReplyVO> replies = demandReplyMapper.selectReplyListWithUserByDemandId(demandId);
        if (replies != null && demand.getAdoptReplyId() != null) {
            for (DemandReplyVO reply : replies) {
                reply.setIsAdopted(reply.getId().equals(demand.getAdoptReplyId()));
            }
        }
        vo.setReplies(replies);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reply(Long demandId, String content, Long userId) {
        Demand demand = demandMapper.selectById(demandId);
        if (demand == null || demand.getDeleted() == 1) {
            throw new BusinessException("需求不存在");
        }
        if (demand.getStatus() != 0) {
            throw new BusinessException("需求已结束，无法回复");
        }

        DemandReply reply = new DemandReply();
        reply.setDemandId(demandId);
        reply.setUserId(userId);
        reply.setContent(content.trim());
        reply.setStatus(1);
        demandReplyMapper.insert(reply);

        return reply.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adopt(Long demandId, Long replyId, Long userId) {
        Demand demand = demandMapper.selectById(demandId);
        if (demand == null || demand.getDeleted() == 1) {
            throw new BusinessException("需求不存在");
        }

        if (!demand.getUserId().equals(userId)) {
            throw new BusinessException(403, "仅发布者可采纳回复");
        }

        if (demand.getStatus() != 0) {
            throw new BusinessException("需求已结束，无法采纳");
        }

        DemandReply reply = demandReplyMapper.selectById(replyId);
        if (reply == null || reply.getDeleted() == 1) {
            throw new BusinessException("回复不存在");
        }
        if (!reply.getDemandId().equals(demandId)) {
            throw new BusinessException("回复不属于该需求");
        }

        if (demand.getAdoptReplyId() != null && demand.getAdoptReplyId().equals(replyId)) {
            throw new BusinessException("该回复已被采纳");
        }

        if (reply.getUserId().equals(userId)) {
            throw new BusinessException("不能采纳自己的回复");
        }

        demand.setStatus(1);
        demand.setResolverId(reply.getUserId());
        demand.setAdoptReplyId(replyId);
        demand.setResolveTime(LocalDateTime.now());
        demandMapper.updateById(demand);

        scoreService.transfer(demand.getUserId(), reply.getUserId(), demand.getScore(),
                "demand", demandId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(Long demandId, Long userId) {
        Demand demand = demandMapper.selectById(demandId);
        if (demand == null || demand.getDeleted() == 1) {
            throw new BusinessException("需求不存在");
        }

        if (!demand.getUserId().equals(userId)) {
            throw new BusinessException(403, "仅发布者可关闭需求");
        }

        if (demand.getStatus() != 0) {
            throw new BusinessException("仅进行中的需求可以关闭");
        }

        demand.setStatus(2);
        demandMapper.updateById(demand);

        scoreService.updateScore(userId, demand.getScore(), 4,
                "demand", demandId, "关闭需求【" + demand.getTitle() + "】退回积分");
    }

    private String statusToStr(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return STATUS_STR_0;
            case 1: return STATUS_STR_1;
            case 2: return STATUS_STR_2;
            default: return "未知";
        }
    }
}