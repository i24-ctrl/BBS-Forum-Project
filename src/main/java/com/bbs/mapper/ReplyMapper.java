package com.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bbs.entity.Reply;
import com.bbs.vo.ReplyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 回复 Mapper 接口
 */
@Mapper
public interface ReplyMapper extends BaseMapper<Reply> {

    /**
     * 查询帖子下所有回复（含用户信息），按楼层升序
     *
     * @param postId 帖子ID
     * @return 回复VO列表
     */
    List<ReplyVO> selectReplyListWithUserByPostId(@Param("postId") Long postId);

    /**
     * 查询帖子最大楼层号（FOR UPDATE，事务内悲观锁）
     *
     * @param postId 帖子ID
     * @return 最大楼层号
     */
    Integer selectMaxFloorByPostIdForUpdate(@Param("postId") Long postId);
}