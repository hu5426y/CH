package com.campus.studyroom.service;

import com.campus.studyroom.dto.RuleUpdateRequest;
import com.campus.studyroom.entity.SystemRule;

import java.util.List;

public interface RuleService {
    List<SystemRule> list();

    void upsert(RuleUpdateRequest request);

    int getIntRule(String key, int defaultValue);
}
