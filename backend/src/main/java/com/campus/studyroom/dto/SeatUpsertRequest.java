package com.campus.studyroom.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SeatUpsertRequest {
    @NotNull
    private Long roomId;
    @NotBlank
    private String seatNo;
    @NotBlank
    private String status;
    private Integer underMaintenance;
}
