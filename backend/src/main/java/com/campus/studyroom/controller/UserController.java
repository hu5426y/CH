package com.campus.studyroom.controller;

import com.campus.studyroom.aop.OpLog;
import com.campus.studyroom.dto.*;
import com.campus.studyroom.entity.User;
import com.campus.studyroom.service.UserService;
import com.campus.studyroom.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user/profile")
    public ApiResponse<User> profile() {
        return ApiResponse.ok(userService.profile(SecurityUtils.currentUserId()));
    }

    @OpLog("USER_UPDATE_PROFILE")
    @PutMapping("/user/profile")
    public ApiResponse<Void> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        userService.updateProfile(SecurityUtils.currentUserId(), request);
        return ApiResponse.ok(null);
    }

    @OpLog("USER_CHANGE_PASSWORD")
    @PutMapping("/user/password")
    public ApiResponse<Void> changePassword(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("密码参数不能为空");
        }
        userService.changePassword(SecurityUtils.currentUserId(), oldPassword, newPassword);
        return ApiResponse.ok(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public ApiResponse<List<User>> listStudents(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(userService.listStudents(keyword));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @OpLog("ADMIN_UPDATE_USER_STATUS")
    @PutMapping("/admin/users/status")
    public ApiResponse<Void> updateStatus(@Valid @RequestBody UserStatusUpdateRequest request) {
        userService.updateStatus(request);
        return ApiResponse.ok(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @OpLog("ADMIN_RESET_PASSWORD")
    @PutMapping("/admin/users/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody UserResetPasswordRequest request) {
        userService.resetPassword(request);
        return ApiResponse.ok(null);
    }
}
