package com.campus.studyroom.controller;

import com.campus.studyroom.aop.OpLog;
import com.campus.studyroom.dto.ApiResponse;
import com.campus.studyroom.dto.ReservationCreateRequest;
import com.campus.studyroom.dto.ReservationView;
import com.campus.studyroom.service.ReservationService;
import com.campus.studyroom.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @OpLog("STUDENT_RESERVATION_CREATE")
    @PostMapping("/reservation/add")
    public ApiResponse<Void> create(@Valid @RequestBody ReservationCreateRequest request) {
        reservationService.create(SecurityUtils.currentUserId(), request);
        return ApiResponse.ok(null);
    }

    @OpLog("STUDENT_RESERVATION_CANCEL")
    @PostMapping("/reservation/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        reservationService.cancel(SecurityUtils.currentUserId(), id, false);
        return ApiResponse.ok(null);
    }

    @OpLog("STUDENT_RESERVATION_CHECKIN")
    @PostMapping("/reservation/{id}/checkin")
    public ApiResponse<Void> checkin(@PathVariable Long id) {
        reservationService.checkin(SecurityUtils.currentUserId(), id);
        return ApiResponse.ok(null);
    }

    @OpLog("STUDENT_RESERVATION_CHECKOUT")
    @PostMapping("/reservation/{id}/checkout")
    public ApiResponse<Void> checkout(@PathVariable Long id) {
        reservationService.checkout(SecurityUtils.currentUserId(), id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/reservation/my")
    public ApiResponse<List<ReservationView>> myList() {
        return ApiResponse.ok(reservationService.listMy(SecurityUtils.currentUserId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/reservation")
    public ApiResponse<List<ReservationView>> listAll(@RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) Long roomId,
                                                      @RequestParam(required = false) String status) {
        return ApiResponse.ok(reservationService.listAll(keyword, roomId, status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @OpLog("ADMIN_CANCEL_RESERVATION")
    @PostMapping("/admin/reservation/{id}/cancel")
    public ApiResponse<Void> adminCancel(@PathVariable Long id) {
        reservationService.cancel(SecurityUtils.currentUserId(), id, true);
        return ApiResponse.ok(null);
    }
}
