package com.campus.studyroom.controller;

import com.campus.studyroom.aop.OpLog;
import com.campus.studyroom.dto.ApiResponse;
import com.campus.studyroom.dto.ViolationView;
import com.campus.studyroom.service.ViolationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/violation")
@PreAuthorize("hasRole('ADMIN')")
public class ViolationController {

    private final ViolationService violationService;

    public ViolationController(ViolationService violationService) {
        this.violationService = violationService;
    }

    @GetMapping
    public ApiResponse<List<ViolationView>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(violationService.list(keyword));
    }

    @OpLog("ADMIN_REVOKE_VIOLATION")
    @PostMapping("/{id}/revoke")
    public ApiResponse<Void> revoke(@PathVariable Long id) {
        violationService.revoke(id);
        return ApiResponse.ok(null);
    }
}
