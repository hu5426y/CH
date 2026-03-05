package com.campus.studyroom.controller;

import com.campus.studyroom.aop.OpLog;
import com.campus.studyroom.dto.ApiResponse;
import com.campus.studyroom.dto.StudyRoomUpsertRequest;
import com.campus.studyroom.entity.StudyRoom;
import com.campus.studyroom.service.StudyRoomService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class StudyRoomController {

    private final StudyRoomService studyRoomService;

    public StudyRoomController(StudyRoomService studyRoomService) {
        this.studyRoomService = studyRoomService;
    }

    @GetMapping("/rooms")
    public ApiResponse<List<StudyRoom>> list() {
        return ApiResponse.ok(studyRoomService.listAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @OpLog("ADMIN_CREATE_ROOM")
    @PostMapping("/admin/rooms")
    public ApiResponse<Void> create(@Valid @RequestBody StudyRoomUpsertRequest request) {
        studyRoomService.create(request);
        return ApiResponse.ok(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @OpLog("ADMIN_UPDATE_ROOM")
    @PutMapping("/admin/rooms/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody StudyRoomUpsertRequest request) {
        studyRoomService.update(id, request);
        return ApiResponse.ok(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @OpLog("ADMIN_DELETE_ROOM")
    @DeleteMapping("/admin/rooms/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        studyRoomService.delete(id);
        return ApiResponse.ok(null);
    }
}
