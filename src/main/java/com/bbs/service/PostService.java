package com.bbs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.dto.PageDTO;
import com.bbs.dto.PostDTO;
import com.bbs.vo.PostDetailVO;
import com.bbs.vo.PostVO;

/**
 * 帖子服务接口
 */
public interface PostService {

    /**
     * 发帖
     *
     * @param dto    帖子信息
     * @param userId 作者ID
     * @return 帖子ID
     */
    Long createPost(PostDTO dto, Long userId);

    /**
     * 首页帖子列表（全局置顶优先）
     *
     * @param pageDTO 分页参数
     * @return 分页结果
     */
    Page<PostVO> listHomePosts(PageDTO pageDTO, Boolean essenceOnly);

    /**
     * 板块内帖子列表（置顶优先按最后回复排序）
     *
     * @param boardId 板块ID
     * @param pageDTO 分页参数
     * @return 分页结果
     */
    Page<PostVO> listBoardPosts(Long boardId, PageDTO pageDTO, Boolean essenceOnly);

    /**
     * 帖子详情（含回复列表，浏览量+1）
     *
     * @param postId 帖子ID
     * @return 帖子详情VO
     */
    PostDetailVO getPostDetail(Long postId);

    /**
     * 修改帖子（仅作者或管理员）
     *
     * @param postId  帖子ID
     * @param dto     新内容
     * @param userId  当前用户ID
     * @param isAdmin 是否管理员
     */
    void updatePost(Long postId, PostDTO dto, Long userId, boolean isAdmin);

    /**
     * 删除帖子（逻辑删除，仅作者或管理员）
     *
     * @param postId  帖子ID
     * @param userId  当前用户ID
     * @param isAdmin 是否管理员
     */
    void deletePost(Long postId, Long userId, boolean isAdmin);
}