package com.campus.studyroom.controller;

import com.campus.studyroom.dto.ApiResponse;
import com.campus.studyroom.entity.OperationLog;
import com.campus.studyroom.service.OperationLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    public ApiResponse<List<OperationLog>> list(@RequestParam(required = false) Long userId,
                                                @RequestParam(required = false) String operationType) {
        return ApiResponse.ok(operationLogService.list(userId, operationType));
    }
}
