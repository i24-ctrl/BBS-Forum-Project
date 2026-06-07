package com.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.common.BusinessException;
import com.bbs.common.JwtUtil;
import com.bbs.dto.LoginDTO;
import com.bbs.dto.RegisterDTO;
import com.bbs.entity.ScoreRecord;
import com.bbs.entity.User;
import com.bbs.mapper.ScoreRecordMapper;
import com.bbs.mapper.UserMapper;
import com.bbs.service.UserService;
import com.bbs.vo.TokenVO;
import com.bbs.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ScoreRecordMapper scoreRecordMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        // 校验用户名是否已存在
        User existUser = getByUsername(dto.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 构建用户对象
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setWorkPlace(dto.getWorkPlace());
        user.setJobNature(dto.getJobNature());
        user.setRole(0);
        user.setStatus(0);      // 待审核
        user.setScore(100);     // 初始100积分
        user.setFrozenScore(0);
        user.setVersion(0);
        user.setLoginCount(0);

        userMapper.insert(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenVO login(LoginDTO dto) {
        // 查询用户
        User user = getByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 校验用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号待审核，请等待管理员审核");
        }
        if (user.getStatus() == 2) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号状态异常");
        }

        // 生成JWT
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        long expiresIn = jwtUtil.getExpiresIn();

        // 更新登录信息
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, user.getId())
                .set(User::getLastLoginTime, LocalDateTime.now())
                .setSql("login_count = login_count + 1");
        userMapper.update(null, updateWrapper);

        return new TokenVO(token, expiresIn);
    }

    @Override
    public UserVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public void updateProfile(Long userId, Map<String, Object> updates) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId);

        // 仅允许更新以下字段
        if (updates.containsKey("realName")) {
            updateWrapper.set(User::getRealName, updates.get("realName"));
        }
        if (updates.containsKey("phone")) {
            updateWrapper.set(User::getPhone, updates.get("phone"));
        }
        if (updates.containsKey("email")) {
            updateWrapper.set(User::getEmail, updates.get("email"));
        }
        if (updates.containsKey("workPlace")) {
            updateWrapper.set(User::getWorkPlace, updates.get("workPlace"));
        }
        if (updates.containsKey("jobNature")) {
            updateWrapper.set(User::getJobNature, updates.get("jobNature"));
        }
        if (updates.containsKey("avatar")) {
            updateWrapper.set(User::getAvatar, updates.get("avatar"));
        }

        userMapper.update(null, updateWrapper);
    }

    @Override
    public Page<ScoreRecord> getScoreRecords(Long userId, int page, int size) {
        Page<ScoreRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ScoreRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScoreRecord::getUserId, userId)
                .orderByDesc(ScoreRecord::getCreateTime);
        return scoreRecordMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public User getById(Long userId) {
        return userMapper.selectById(userId);
    }
}