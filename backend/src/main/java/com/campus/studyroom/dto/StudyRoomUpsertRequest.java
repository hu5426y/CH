package com.campus.studyroom.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;

@Data
public class StudyRoomUpsertRequest {
    @NotBlank
    private String name;
    @NotNull
    private Integer floor;
    @NotNull
    private Integer seatCount;
    @NotNull
    private LocalTime openTime;
    @NotNull
    private LocalTime closeTime;
    @NotBlank
    private String status;
}
