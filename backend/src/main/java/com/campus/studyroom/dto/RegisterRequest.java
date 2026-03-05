package com.campus.studyroom.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class RegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String realName;
    private String gender;
    private String studentNo;
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式错误")
    private String phone;
}
