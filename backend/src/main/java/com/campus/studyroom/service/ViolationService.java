package com.campus.studyroom.service;

import com.campus.studyroom.dto.ViolationView;

import java.util.List;

public interface ViolationService {
    List<ViolationView> list(String keyword);

    void revoke(Long violationId);
}
