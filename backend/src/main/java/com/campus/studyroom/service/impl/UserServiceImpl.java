package com.campus.studyroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.studyroom.dto.ProfileUpdateRequest;
import com.campus.studyroom.dto.UserResetPasswordRequest;
import com.campus.studyroom.dto.UserStatusUpdateRequest;
import com.campus.studyroom.entity.User;
import com.campus.studyroom.exception.BizException;
import com.campus.studyroom.mapper.UserMapper;
import com.campus.studyroom.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User profile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setRealName(request.getRealName() == null ? user.getRealName() : request.getRealName());
        user.setGender(request.getGender() == null ? user.getGender() : request.getGender());
        user.setPhone(request.getPhone() == null ? user.getPhone() : request.getPhone());
        userMapper.updateById(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BizException(400, "旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public List<User> listStudents(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getRole, "STUDENT");
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getStudentNo, keyword));
        }
        List<User> users = userMapper.selectList(wrapper.orderByDesc(User::getCreatedAt));
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @Override
    public void updateStatus(UserStatusUpdateRequest request) {
        User user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setStatus(request.getEnabled() ? "ENABLED" : "DISABLED");
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(UserResetPasswordRequest request) {
        User user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }
}
