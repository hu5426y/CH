package com.campus.studyroom.service;

import com.campus.studyroom.dto.ReservationCreateRequest;
import com.campus.studyroom.dto.ReservationView;

import java.util.List;

public interface ReservationService {
    void create(Long userId, ReservationCreateRequest request);

    void cancel(Long userId, Long reservationId, boolean adminAction);

    void checkin(Long userId, Long reservationId);

    void checkout(Long userId, Long reservationId);

    List<ReservationView> listMy(Long userId);

    List<ReservationView> listAll(String keyword, Long roomId, String status);

    void detectNoShowViolations();
}
