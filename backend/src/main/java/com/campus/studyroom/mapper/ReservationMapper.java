package com.campus.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.studyroom.dto.ReservationView;
import com.campus.studyroom.entity.Reservation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReservationMapper extends BaseMapper<Reservation> {

    @Select("""
            SELECT r.id, r.user_id, u.username, u.real_name, r.seat_id, s.seat_no, s.room_id, sr.name AS room_name,
                   r.start_time, r.end_time, r.checkin_time, r.leave_time, r.status, r.created_at
            FROM reservation r
            LEFT JOIN user u ON r.user_id = u.id
            LEFT JOIN seat s ON r.seat_id = s.id
            LEFT JOIN study_room sr ON s.room_id = sr.id
            WHERE r.user_id = #{userId}
            ORDER BY r.created_at DESC
            """)
    List<ReservationView> listByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT r.id, r.user_id, u.username, u.real_name, r.seat_id, s.seat_no, s.room_id, sr.name AS room_name,
                   r.start_time, r.end_time, r.checkin_time, r.leave_time, r.status, r.created_at
            FROM reservation r
            LEFT JOIN user u ON r.user_id = u.id
            LEFT JOIN seat s ON r.seat_id = s.id
            LEFT JOIN study_room sr ON s.room_id = sr.id
            WHERE (#{keyword} IS NULL OR u.username LIKE CONCAT('%', #{keyword}, '%') OR u.real_name LIKE CONCAT('%', #{keyword}, '%'))
              AND (#{roomId} IS NULL OR s.room_id = #{roomId})
              AND (#{status} IS NULL OR r.status = #{status})
            ORDER BY r.created_at DESC
            """)
    List<ReservationView> listAll(@Param("keyword") String keyword,
                                  @Param("roomId") Long roomId,
                                  @Param("status") String status);

    @Select("""
            SELECT COUNT(*)
            FROM reservation
            WHERE seat_id = #{seatId}
              AND status IN ('RESERVED', 'CHECKED_IN')
              AND start_time < #{endTime}
              AND end_time > #{startTime}
            """)
    Long countOverlap(@Param("seatId") Long seatId,
                      @Param("startTime") LocalDateTime startTime,
                      @Param("endTime") LocalDateTime endTime);

    @Select("""
            SELECT sr.name AS roomName, COUNT(*) AS reservationCount
            FROM reservation r
            JOIN seat s ON r.seat_id = s.id
            JOIN study_room sr ON s.room_id = sr.id
            WHERE r.status IN ('CHECKED_IN', 'COMPLETED', 'RESERVED')
            GROUP BY sr.id, sr.name
            ORDER BY reservationCount DESC
            """)
    List<Map<String, Object>> roomUsageStats();

    @Select("""
            SELECT DATE_FORMAT(start_time, '%Y-%m-%d') AS day, COUNT(*) AS count
            FROM reservation
            WHERE start_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY)
            GROUP BY DATE_FORMAT(start_time, '%Y-%m-%d')
            ORDER BY day
            """)
    List<Map<String, Object>> trendStats(@Param("days") int days);
}
