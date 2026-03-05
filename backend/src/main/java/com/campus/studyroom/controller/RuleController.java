package com.campus.studyroom.controller;

import com.campus.studyroom.aop.OpLog;
import com.campus.studyroom.dto.ApiResponse;
import com.campus.studyroom.dto.RuleUpdateRequest;
import com.campus.studyroom.entity.SystemRule;
import com.campus.studyroom.service.RuleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin/rule")
@PreAuthorize("hasRole('ADMIN')")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping
    public ApiResponse<List<SystemRule>> list() {
        return ApiResponse.ok(ruleService.list());
    }

    @OpLog("ADMIN_UPSERT_RULE")
    @PostMapping
    public ApiResponse<Void> upsert(@Valid @RequestBody RuleUpdateRequest request) {
        ruleService.upsert(request);
        return ApiResponse.ok(null);
    }
}
