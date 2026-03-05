package com.campus.studyroom.service;

import com.campus.studyroom.dto.ProfileUpdateRequest;
import com.campus.studyroom.dto.UserResetPasswordRequest;
import com.campus.studyroom.dto.UserStatusUpdateRequest;
import com.campus.studyroom.entity.User;

import java.util.List;

public interface UserService {
    User profile(Long userId);

    void updateProfile(Long userId, ProfileUpdateRequest request);

    void changePassword(Long userId, String oldPassword, String newPassword);

    List<User> listStudents(String keyword);

    void updateStatus(UserStatusUpdateRequest request);

    void resetPassword(UserResetPasswordRequest request);
}
