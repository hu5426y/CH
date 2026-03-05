package com.campus.studyroom.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;

@Data
public class ProfileUpdateRequest {
    private String realName;
    private String gender;
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式错误")
    private String phone;
}
