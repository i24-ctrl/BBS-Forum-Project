package com.bbs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.dto.LoginDTO;
import com.bbs.dto.RegisterDTO;
import com.bbs.entity.ScoreRecord;
import com.bbs.entity.User;
import com.bbs.vo.TokenVO;
import com.bbs.vo.UserVO;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     * BCrypt加密密码，status=0待审核，初始score=100
     *
     * @param dto 注册信息
     */
    void register(RegisterDTO dto);

    /**
     * 用户登录
     * 校验status=1正常用户，生成JWT，记录登录信息
     *
     * @param dto 登录信息
     * @return TokenVO
     */
    TokenVO login(LoginDTO dto);

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return UserVO
     */
    UserVO getProfile(Long userId);

    /**
     * 更新个人资料
     * 允许修改 realName / phone / email / workPlace / jobNature / avatar
     *
     * @param userId  用户ID
     * @param updates 要更新的字段 Map
     */
    void updateProfile(Long userId, Map<String, Object> updates);

    /**
     * 分页查询积分流水
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 分页结果
     */
    Page<ScoreRecord> getScoreRecords(Long userId, int page, int size);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return User
     */
    User getByUsername(String username);

    /**
     * 根据ID查询用户
     *
     * @param userId 用户ID
     * @return User
     */
    User getById(Long userId);
}