package com.campus.studyroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.studyroom.dto.ReservationCreateRequest;
import com.campus.studyroom.dto.ReservationView;
import com.campus.studyroom.entity.Reservation;
import com.campus.studyroom.entity.Seat;
import com.campus.studyroom.entity.StudyRoom;
import com.campus.studyroom.entity.User;
import com.campus.studyroom.entity.Violation;
import com.campus.studyroom.exception.BizException;
import com.campus.studyroom.mapper.ReservationMapper;
import com.campus.studyroom.mapper.SeatMapper;
import com.campus.studyroom.mapper.StudyRoomMapper;
import com.campus.studyroom.mapper.UserMapper;
import com.campus.studyroom.mapper.ViolationMapper;
import com.campus.studyroom.service.ReservationService;
import com.campus.studyroom.service.RuleService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationMapper reservationMapper;
    private final SeatMapper seatMapper;
    private final UserMapper userMapper;
    private final ViolationMapper violationMapper;
    private final StudyRoomMapper studyRoomMapper;
    private final RuleService ruleService;
    private final StringRedisTemplate stringRedisTemplate;

    public ReservationServiceImpl(ReservationMapper reservationMapper,
                                  SeatMapper seatMapper,
                                  UserMapper userMapper,
                                  ViolationMapper violationMapper,
                                  StudyRoomMapper studyRoomMapper,
                                  RuleService ruleService,
                                  StringRedisTemplate stringRedisTemplate) {
        this.reservationMapper = reservationMapper;
        this.seatMapper = seatMapper;
        this.userMapper = userMapper;
        this.violationMapper = violationMapper;
        this.studyRoomMapper = studyRoomMapper;
        this.ruleService = ruleService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Long userId, ReservationCreateRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BizException(400, "预约结束时间必须晚于开始时间");
        }
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BizException(400, "预约开始时间不能早于当前时间");
        }
        if (!request.getStartTime().toLocalDate().equals(request.getEndTime().toLocalDate())) {
            throw new BizException(400, "预约必须在同一天内完成");
        }

        LocalTime limitStart = LocalTime.of(9, 0);
        LocalTime limitEnd = LocalTime.of(21, 0);
        LocalTime startLocalTime = request.getStartTime().toLocalTime();
        LocalTime endLocalTime = request.getEndTime().toLocalTime();
        if (startLocalTime.isBefore(limitStart) || endLocalTime.isAfter(limitEnd)) {
            throw new BizException(400, "预约时间仅支持 09:00-21:00");
        }

        int maxHours = ruleService.getIntRule("MAX_RESERVATION_HOURS", 4);
        if (maxHours <= 0 || maxHours > 12) {
            maxHours = 4;
        }
        if (Duration.between(request.getStartTime(), request.getEndTime()).toMinutes() > maxHours * 60L) {
            throw new BizException(400, "超过最大预约时长限制");
        }

        int advanceHours = ruleService.getIntRule("ADVANCE_RESERVATION_HOURS", 72);
        if (advanceHours <= 0 || advanceHours > 720) {
            advanceHours = 72;
        }
        if (Duration.between(LocalDateTime.now(), request.getStartTime()).toHours() > advanceHours) {
            throw new BizException(400, "超过最大预约提前时间");
        }

        User user = userMapper.selectById(userId);
        if (user == null || !"ENABLED".equals(user.getStatus())) {
            throw new BizException(400, "用户不可用");
        }
        int creditThreshold = ruleService.getIntRule("CREDIT_THRESHOLD", 60);
        if (user.getCreditScore() < creditThreshold) {
            throw new BizException(400, "信用积分不足，暂不可预约");
        }

        Long activeCount = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .in(Reservation::getStatus, "RESERVED", "CHECKED_IN")
                .gt(Reservation::getEndTime, LocalDateTime.now()));
        if (activeCount != null && activeCount > 0) {
            throw new BizException(400, "已存在有效预约，不能重复预约");
        }

        Seat seat = seatMapper.selectById(request.getSeatId());
        if (seat == null) {
            throw new BizException(404, "座位不存在");
        }
        if (!"FREE".equals(seat.getStatus())) {
            throw new BizException(400, "座位当前不可预约");
        }

        StudyRoom room = studyRoomMapper.selectById(seat.getRoomId());
        if (room == null || !"OPEN".equals(room.getStatus())) {
            throw new BizException(400, "自习室不可用");
        }
        if (request.getStartTime().toLocalTime().isBefore(room.getOpenTime())
                || request.getEndTime().toLocalTime().isAfter(room.getCloseTime())) {
            throw new BizException(400, "预约时间超出自习室开放时段");
        }

        String lockKey = "lock:seat:" + seat.getId();
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, String.valueOf(userId), Duration.ofSeconds(10));
        if (locked == null || !locked) {
            throw new BizException(409, "座位正在被其他用户预约，请稍后重试");
        }

        try {
            Long overlap = reservationMapper.countOverlap(seat.getId(), request.getStartTime(), request.getEndTime());
            if (overlap != null && overlap > 0) {
                throw new BizException(409, "该时间段座位已被预约");
            }

            Reservation reservation = new Reservation();
            reservation.setUserId(userId);
            reservation.setSeatId(seat.getId());
            reservation.setStartTime(request.getStartTime());
            reservation.setEndTime(request.getEndTime());
            reservation.setStatus("RESERVED");
            reservationMapper.insert(reservation);

            seat.setStatus("RESERVED");
            seatMapper.updateById(seat);
            stringRedisTemplate.opsForValue().set("seat:status:" + seat.getId(), "RESERVED", Duration.ofHours(6));
        } finally {
            stringRedisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, Long reservationId, boolean adminAction) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new BizException(404, "预约不存在");
        }
        if (!adminAction && !reservation.getUserId().equals(userId)) {
            throw new BizException(403, "无权取消他人预约");
        }
        if (!"RESERVED".equals(reservation.getStatus())) {
            throw new BizException(400, "当前状态不可取消");
        }
        if (LocalDateTime.now().isAfter(reservation.getStartTime()) && !adminAction) {
            throw new BizException(400, "预约开始后不可取消");
        }

        reservation.setStatus("CANCELED");
        reservationMapper.updateById(reservation);
        updateSeatToFree(reservation.getSeatId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkin(Long userId, Long reservationId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null || !reservation.getUserId().equals(userId)) {
            throw new BizException(404, "预约不存在");
        }
        if (!"RESERVED".equals(reservation.getStatus())) {
            throw new BizException(400, "当前状态不可签到");
        }

        int timeoutMins = ruleService.getIntRule("CHECKIN_TIMEOUT_MINUTES", 15);
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(reservation.getStartTime().plusMinutes(timeoutMins))) {
            throw new BizException(400, "签到超时");
        }
        if (now.isBefore(reservation.getStartTime().minusMinutes(10))) {
            throw new BizException(400, "尚未到签到时间");
        }

        reservation.setCheckinTime(now);
        reservation.setStatus("CHECKED_IN");
        reservationMapper.updateById(reservation);

        Seat seat = seatMapper.selectById(reservation.getSeatId());
        if (seat != null) {
            seat.setStatus("OCCUPIED");
            seatMapper.updateById(seat);
            stringRedisTemplate.opsForValue().set("seat:status:" + seat.getId(), "OCCUPIED", Duration.ofHours(6));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkout(Long userId, Long reservationId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null || !reservation.getUserId().equals(userId)) {
            throw new BizException(404, "预约不存在");
        }
        if (!"CHECKED_IN".equals(reservation.getStatus())) {
            throw new BizException(400, "当前状态不可签离");
        }

        LocalDateTime now = LocalDateTime.now();
        reservation.setLeaveTime(now);
        reservation.setStatus("COMPLETED");
        reservationMapper.updateById(reservation);

        if (now.isBefore(reservation.getEndTime().minusMinutes(30))) {
            createViolation(reservation, "EARLY_LEAVE");
        }
        updateSeatToFree(reservation.getSeatId());
    }

    @Override
    public List<ReservationView> listMy(Long userId) {
        return reservationMapper.listByUserId(userId);
    }

    @Override
    public List<ReservationView> listAll(String keyword, Long roomId, String status) {
        return reservationMapper.listAll(blankToNull(keyword), roomId, blankToNull(status));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void detectNoShowViolations() {
        int timeoutMins = ruleService.getIntRule("CHECKIN_TIMEOUT_MINUTES", 15);
        List<Reservation> timedOut = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getStatus, "RESERVED")
                .lt(Reservation::getStartTime, LocalDateTime.now().minusMinutes(timeoutMins)));

        for (Reservation reservation : timedOut) {
            reservation.setStatus("VIOLATED");
            reservationMapper.updateById(reservation);
            createViolation(reservation, "NO_SHOW");
            updateSeatToFree(reservation.getSeatId());
        }
    }

    private void createViolation(Reservation reservation, String type) {
        Long existing = violationMapper.selectCount(new LambdaQueryWrapper<Violation>()
                .eq(Violation::getReservationId, reservation.getId())
                .eq(Violation::getViolationType, type)
                .eq(Violation::getProcessStatus, "ACTIVE"));
        if (existing != null && existing > 0) {
            return;
        }

        int deduct = ruleService.getIntRule("VIOLATION_DEDUCT_SCORE", 10);
        Violation violation = new Violation();
        violation.setReservationId(reservation.getId());
        violation.setUserId(reservation.getUserId());
        violation.setViolationType(type);
        violation.setViolationTime(LocalDateTime.now());
        violation.setScoreDeducted(deduct);
        violation.setProcessStatus("ACTIVE");
        violation.setCreatedAt(LocalDateTime.now());
        violationMapper.insert(violation);

        User user = userMapper.selectById(reservation.getUserId());
        if (user != null) {
            int score = user.getCreditScore() == null ? 100 : user.getCreditScore();
            user.setCreditScore(Math.max(0, score - deduct));
            userMapper.updateById(user);
        }
    }

    private void updateSeatToFree(Long seatId) {
        Seat seat = seatMapper.selectById(seatId);
        if (seat != null && ("RESERVED".equals(seat.getStatus()) || "OCCUPIED".equals(seat.getStatus()))) {
            seat.setStatus("FREE");
            seatMapper.updateById(seat);
            stringRedisTemplate.opsForValue().set("seat:status:" + seat.getId(), "FREE", Duration.ofHours(2));
        }
    }

    private String blankToNull(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return text;
    }
}
