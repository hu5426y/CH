package com.campus.studyroom.service;

import com.campus.studyroom.entity.OperationLog;

import java.util.List;

public interface OperationLogService {
    List<OperationLog> list(Long userId, String operationType);
}
