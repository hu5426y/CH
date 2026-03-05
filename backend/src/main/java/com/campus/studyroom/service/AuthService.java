package com.campus.studyroom.service;

import com.campus.studyroom.dto.LoginRequest;
import com.campus.studyroom.dto.LoginResponse;
import com.campus.studyroom.dto.RegisterRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);
}
