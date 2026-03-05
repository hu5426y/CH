package com.campus.studyroom.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ViolationView {
    private Long id;
    private Long reservationId;
    private Long userId;
    private String username;
    private String realName;
    private String violationType;
    private LocalDateTime violationTime;
    private Integer scoreDeducted;
    private String processStatus;
    private LocalDateTime createdAt;
}
