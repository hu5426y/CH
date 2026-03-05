package com.campus.studyroom.controller;

import com.campus.studyroom.aop.OpLog;
import com.campus.studyroom.dto.ApiResponse;
import com.campus.studyroom.dto.LoginRequest;
import com.campus.studyroom.dto.LoginResponse;
import com.campus.studyroom.dto.RegisterRequest;
import com.campus.studyroom.service.AuthService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @OpLog("AUTH_LOGIN")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @OpLog("AUTH_REGISTER")
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.ok(null);
    }
}
