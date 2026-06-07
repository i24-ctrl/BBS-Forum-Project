package com.bbs.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.common.BusinessException;
import com.bbs.dto.PageDTO;
import com.bbs.dto.PostDTO;
import com.bbs.entity.Board;
import com.bbs.entity.Post;
import com.bbs.entity.User;
import com.bbs.mapper.BoardMapper;
import com.bbs.mapper.PostMapper;
import com.bbs.mapper.ReplyMapper;
import com.bbs.mapper.UserMapper;
import com.bbs.service.PostService;
import com.bbs.vo.PostDetailVO;
import com.bbs.vo.PostVO;
import com.bbs.vo.ReplyVO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 帖子服务实现类
 */
@Service
public class PostServiceImpl implements PostService {

    @Resource
    private PostMapper postMapper;

    @Resource
    private ReplyMapper replyMapper;

    @Resource
    private BoardMapper boardMapper;

    @Resource
    private UserMapper userMapper;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(PostDTO dto, Long userId) {
        Board board = boardMapper.selectById(dto.getBoardId());
        if (board == null || board.getStatus() != 1) {
            throw new BusinessException("板块不存在或已禁用");
        }

        String cleanContent = cleanHtml(dto.getContent());

        Post post = new Post();
        post.setBoardId(dto.getBoardId());
        post.setUserId(userId);
        post.setTitle(dto.getTitle().trim());
        post.setContent(cleanContent);
        post.setViewCount(0);
        post.setReplyCount(0);
        post.setIsTop(0);
        post.setIsEssence(0);
        post.setStatus(1);
        post.setDeleted(0);
        post.setLastReplyTime(null);

        postMapper.insert(post);

        // ⭐ 更新板块帖子数
        Board updateBoard = new Board();
        updateBoard.setId(dto.getBoardId());
        updateBoard.setPostCount(board.getPostCount() + 1);
        boardMapper.updateById(updateBoard);

        return post.getId();
    }
    @Override
    public Page<PostVO> listHomePosts(PageDTO pageDTO, Boolean essenceOnly) {
        Page<PostVO> page = new Page<>(pageDTO.getCurrent(), pageDTO.getSafeSize());
        Page<PostVO> result = postMapper.selectPostListWithAuthor(page, null, "home", essenceOnly);
        formatPostVos(result.getRecords());
        return result;
    }

    @Override
    public Page<PostVO> listBoardPosts(Long boardId, PageDTO pageDTO, Boolean essenceOnly) {
        Page<PostVO> page = new Page<>(pageDTO.getCurrent(), pageDTO.getSafeSize());
        Page<PostVO> result = postMapper.selectPostListWithAuthor(page, boardId, "board", essenceOnly);
        formatPostVos(result.getRecords());
        return result;
    }

    @Override
    public PostDetailVO getPostDetail(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getDeleted() == 1) {
            throw new BusinessException("帖子不存在");
        }
        if (post.getStatus() != 1) {
            if (post.getStatus() == 2) {
                throw new BusinessException("该帖子因违规已下架，无法查看详情");
            }
            throw new BusinessException("该帖子当前不可查看");
        }

        // 浏览量 +1
        Post updateView = new Post();
        updateView.setId(postId);
        updateView.setViewCount(post.getViewCount() + 1);
        postMapper.updateById(updateView);
        post.setViewCount(post.getViewCount() + 1);

        PostDetailVO vo = new PostDetailVO();
        BeanUtils.copyProperties(post, vo);
        if (post.getLastReplyTime() != null) {
            vo.setLastReplyTimeStr(post.getLastReplyTime().format(DT_FMT));
        }

        User author = userMapper.selectById(post.getUserId());
        if (author != null) {
            vo.setAuthorName(author.getUsername());
            vo.setAuthorAvatar(author.getAvatar());
        }
        Board board = boardMapper.selectById(post.getBoardId());
        if (board != null) {
            vo.setBoardName(board.getName());
        }

        List<ReplyVO> allReplies = replyMapper.selectReplyListWithUserByPostId(postId);
        vo.setReplies(buildReplyTree(allReplies));

        return vo;
    }

    @Override
    public void updatePost(Long postId, PostDTO dto, Long userId, boolean isAdmin) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getDeleted() == 1) {
            throw new BusinessException("帖子不存在");
        }
        if (!isAdmin && !post.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改此帖子");
        }

        String cleanContent = cleanHtml(dto.getContent());

        post.setTitle(dto.getTitle().trim());
        post.setContent(cleanContent);
        if (dto.getBoardId() != null && !dto.getBoardId().equals(post.getBoardId())) {
            Board targetBoard = boardMapper.selectById(dto.getBoardId());
            if (targetBoard == null || targetBoard.getStatus() != 1) {
                throw new BusinessException("目标板块不存在或已禁用");
            }
            post.setBoardId(dto.getBoardId());
        }
        postMapper.updateById(post);
    }

    @Override
    public void deletePost(Long postId, Long userId, boolean isAdmin) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getDeleted() == 1) {
            throw new BusinessException("帖子不存在");
        }
        if (!isAdmin && !post.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此帖子");
        }
        postMapper.deleteById(postId);  // 逻辑删除

        // ⭐ 扣减板块帖子数
        Board board = boardMapper.selectById(post.getBoardId());
        if (board != null && board.getPostCount() > 0) {
            Board updateBoard = new Board();
            updateBoard.setId(board.getId());
            updateBoard.setPostCount(board.getPostCount() - 1);
            boardMapper.updateById(updateBoard);
        }
    }

    // ============================================================
    // ⭐ 关键方法：HTML 清洗，保留 img 的 src 属性
    // 原理：先用占位符替换 src → Jsoup.clean → 换回真实 src
    // ============================================================

    private String cleanHtml(String rawHtml) {
        if (rawHtml == null || rawHtml.trim().isEmpty()) {
            return "";
        }
        // 1. 解析 HTML
        Document doc = Jsoup.parseBodyFragment(rawHtml);
        // 2. 删除危险标签
        doc.select("script, iframe, object, embed, form, input, link, style, meta, base, applet, frame, frameset, ilayer, layer, title").remove();
        // 3. 删除所有 on* 事件属性（onclick, onerror, onload 等）
        for (Element el : doc.select("*")) {
            for (Attribute attr : el.attributes()) {
                String key = attr.getKey().toLowerCase();
                if (key.startsWith("on")) {
                    el.removeAttr(key);
                }
            }
        }
        // 4. 删除 javascript: 伪协议的链接
        for (Element el : doc.select("a[href]")) {
            String href = el.attr("href");
            if (href != null && href.toLowerCase().trim().startsWith("javascript:")) {
                el.removeAttr("href");
            }
        }
        for (Element el : doc.select("img[src]")) {
            String src = el.attr("src");
            if (src != null && src.toLowerCase().trim().startsWith("javascript:")) {
                el.removeAttr("src");
            }
        }
        // 5. 输出清洗后的 HTML
        doc.outputSettings().prettyPrint(false);
        return doc.body().html();
    }

    // ==================== 其他工具方法 ====================

    private void formatPostVos(List<PostVO> list) {
        if (list == null) return;
        for (PostVO vo : list) {
            if (vo.getLastReplyTime() != null) {
                vo.setLastReplyTimeStr(vo.getLastReplyTime().format(DT_FMT));
            }
        }
    }

    private List<ReplyVO> buildReplyTree(List<ReplyVO> allReplies) {
        if (allReplies == null || allReplies.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, List<ReplyVO>> childrenMap = new LinkedHashMap<>();
        List<ReplyVO> topReplies = new ArrayList<>();

        for (ReplyVO r : allReplies) {
            if (r.getParentId() == null) {
                topReplies.add(r);
                childrenMap.put(r.getId(), new ArrayList<>());
            } else {
                childrenMap.computeIfAbsent(r.getParentId(), k -> new ArrayList<>()).add(r);
            }
        }

        for (ReplyVO top : topReplies) {
            top.setChildren(childrenMap.getOrDefault(top.getId(), new ArrayList<>()));
        }

        return topReplies;
    }
}