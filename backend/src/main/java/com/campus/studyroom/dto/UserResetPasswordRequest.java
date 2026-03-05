package com.campus.studyroom.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserResetPasswordRequest {
    @NotNull
    private Long userId;
    @NotBlank
    private String newPassword;
}
