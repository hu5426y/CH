package com.campus.studyroom.service;

import java.util.List;
import java.util.Map;

public interface StatisticsService {
    Map<String, Object> overview();

    List<Map<String, Object>> roomUsage();

    List<Map<String, Object>> reservationTrend(int days);
}
