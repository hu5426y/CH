package com.campus.studyroom.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationView {
    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private Long seatId;
    private String seatNo;
    private Long roomId;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime checkinTime;
    private LocalDateTime leaveTime;
    private String status;
    private LocalDateTime createdAt;
}
