package com.campus.studyroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.studyroom.dto.LoginRequest;
import com.campus.studyroom.dto.LoginResponse;
import com.campus.studyroom.dto.RegisterRequest;
import com.campus.studyroom.entity.User;
import com.campus.studyroom.exception.BizException;
import com.campus.studyroom.mapper.UserMapper;
import com.campus.studyroom.security.JwtUtil;
import com.campus.studyroom.security.LoginUser;
import com.campus.studyroom.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           UserMapper userMapper,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            String token = jwtUtil.generateToken(loginUser);

            User user = userMapper.selectById(loginUser.getId());
            return new LoginResponse(token, loginUser.getRole(), loginUser.getId(), user.getRealName());
        } catch (BadCredentialsException ex) {
            throw new BizException(401, "用户名或密码错误");
        }
    }

    @Override
    public void register(RegisterRequest request) {
        Long existing = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (existing != null && existing > 0) {
            throw new BizException(400, "账号已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setGender(request.getGender());
        user.setStudentNo(request.getStudentNo());
        user.setPhone(request.getPhone());
        user.setRole("STUDENT");
        user.setCreditScore(100);
        user.setStatus("ENABLED");
        userMapper.insert(user);
    }
}
