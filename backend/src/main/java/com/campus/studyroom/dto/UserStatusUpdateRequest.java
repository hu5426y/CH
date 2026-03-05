package com.campus.studyroom.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserStatusUpdateRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Boolean enabled;
}
