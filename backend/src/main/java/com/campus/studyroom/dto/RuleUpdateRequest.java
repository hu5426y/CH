package com.campus.studyroom.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RuleUpdateRequest {
    @NotBlank
    private String ruleKey;
    @NotBlank
    private String ruleValue;
    private String description;
}
