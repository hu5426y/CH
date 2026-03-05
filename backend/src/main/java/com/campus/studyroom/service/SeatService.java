package com.campus.studyroom.service;

import com.campus.studyroom.dto.SeatUpsertRequest;
import com.campus.studyroom.entity.Seat;

import java.util.List;

public interface SeatService {
    List<Seat> list(Long roomId, String status, String seatNo);

    void create(SeatUpsertRequest request);

    void update(Long id, SeatUpsertRequest request);

    void delete(Long id);
}
