package com.campus.studyroom.schedule;

import com.campus.studyroom.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReservationViolationSchedule {

    private final ReservationService reservationService;

    public ReservationViolationSchedule(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(fixedDelay = 60000)
    public void detectNoShow() {
        reservationService.detectNoShowViolations();
        log.debug("scheduled no-show detection executed");
    }
}
