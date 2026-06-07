package com.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.entity.Post;
import com.bbs.vo.PostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 帖子 Mapper 接口
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 分页查询帖子列表（含作者名、板块名）
     *
     * @param page      分页对象
     * @param boardId   板块ID（null 表示全局）
     * @param orderType 排序类型：home(首页) / board(板块内)
     * @return 帖子VO分页结果（返回传入的 Page 对象本身）
     */
    Page<PostVO> selectPostListWithAuthor(Page<PostVO> page,
                                          @Param("boardId") Long boardId,
                                          @Param("orderType") String orderType,
                                          @Param("essenceOnly") Boolean essenceOnly);

    /**
     * 管理员分页查询帖子列表（包含所有状态）
     */
    Page<PostVO> selectAdminPostListWithAuthor(Page<PostVO> page);

    /**
     * 查询帖子最大楼层号
     */
    Integer selectMaxFloorByPostId(@Param("postId") Long postId);

    /**
     * 更新帖子回复数和最后回复时间
     */
    int updateReplyCountAndTime(@Param("postId") Long postId,
                                @Param("lastReplyTime") LocalDateTime lastReplyTime);
}