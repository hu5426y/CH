package com.campus.studyroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.studyroom.entity.Reservation;
import com.campus.studyroom.entity.Seat;
import com.campus.studyroom.entity.StudyRoom;
import com.campus.studyroom.entity.User;
import com.campus.studyroom.entity.Violation;
import com.campus.studyroom.mapper.ReservationMapper;
import com.campus.studyroom.mapper.SeatMapper;
import com.campus.studyroom.mapper.StudyRoomMapper;
import com.campus.studyroom.mapper.UserMapper;
import com.campus.studyroom.mapper.ViolationMapper;
import com.campus.studyroom.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final UserMapper userMapper;
    private final StudyRoomMapper studyRoomMapper;
    private final SeatMapper seatMapper;
    private final ReservationMapper reservationMapper;
    private final ViolationMapper violationMapper;

    public StatisticsServiceImpl(UserMapper userMapper,
                                 StudyRoomMapper studyRoomMapper,
                                 SeatMapper seatMapper,
                                 ReservationMapper reservationMapper,
                                 ViolationMapper violationMapper) {
        this.userMapper = userMapper;
        this.studyRoomMapper = studyRoomMapper;
        this.seatMapper = seatMapper;
        this.reservationMapper = reservationMapper;
        this.violationMapper = violationMapper;
    }

    @Override
    public Map<String, Object> overview() {
        Long studentCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, "STUDENT"));
        Long roomCount = studyRoomMapper.selectCount(null);
        Long seatCount = seatMapper.selectCount(null);
        Long reservationToday = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                .ge(Reservation::getStartTime, LocalDateTime.now().toLocalDate().atStartOfDay()));
        Long violationCount = violationMapper.selectCount(new LambdaQueryWrapper<Violation>()
                .eq(Violation::getProcessStatus, "ACTIVE"));
        Long occupied = seatMapper.selectCount(new LambdaQueryWrapper<Seat>().in(Seat::getStatus, "RESERVED", "OCCUPIED"));

        double utilization = (seatCount == null || seatCount == 0) ? 0.0 : occupied * 100.0 / seatCount;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("studentCount", studentCount);
        map.put("roomCount", roomCount);
        map.put("seatCount", seatCount);
        map.put("reservationToday", reservationToday);
        map.put("violationCount", violationCount);
        map.put("utilization", Math.round(utilization * 100.0) / 100.0);
        return map;
    }

    @Override
    public List<Map<String, Object>> roomUsage() {
        return reservationMapper.roomUsageStats();
    }

    @Override
    public List<Map<String, Object>> reservationTrend(int days) {
        return reservationMapper.trendStats(days);
    }
}
