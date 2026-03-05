package com.campus.studyroom.controller;

import com.campus.studyroom.aop.OpLog;
import com.campus.studyroom.dto.ApiResponse;
import com.campus.studyroom.dto.SeatUpsertRequest;
import com.campus.studyroom.entity.Seat;
import com.campus.studyroom.service.SeatService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/seats")
    public ApiResponse<List<Seat>> list(@RequestParam(required = false) Long roomId,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) String seatNo) {
        return ApiResponse.ok(seatService.list(roomId, status, seatNo));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @OpLog("ADMIN_CREATE_SEAT")
    @PostMapping("/admin/seats")
    public ApiResponse<Void> create(@Valid @RequestBody SeatUpsertRequest request) {
        seatService.create(request);
        return ApiResponse.ok(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @OpLog("ADMIN_UPDATE_SEAT")
    @PutMapping("/admin/seats/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody SeatUpsertRequest request) {
        seatService.update(id, request);
        return ApiResponse.ok(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @OpLog("ADMIN_DELETE_SEAT")
    @DeleteMapping("/admin/seats/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        seatService.delete(id);
        return ApiResponse.ok(null);
    }
}
