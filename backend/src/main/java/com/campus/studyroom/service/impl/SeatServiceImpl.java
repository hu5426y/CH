package com.campus.studyroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.studyroom.dto.SeatUpsertRequest;
import com.campus.studyroom.entity.Seat;
import com.campus.studyroom.exception.BizException;
import com.campus.studyroom.mapper.SeatMapper;
import com.campus.studyroom.mapper.StudyRoomMapper;
import com.campus.studyroom.service.SeatService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatServiceImpl implements SeatService {

    private final SeatMapper seatMapper;
    private final StudyRoomMapper studyRoomMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public SeatServiceImpl(SeatMapper seatMapper,
                           StudyRoomMapper studyRoomMapper,
                           StringRedisTemplate stringRedisTemplate) {
        this.seatMapper = seatMapper;
        this.studyRoomMapper = studyRoomMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public List<Seat> list(Long roomId, String status, String seatNo) {
        LambdaQueryWrapper<Seat> wrapper = new LambdaQueryWrapper<>();
        if (roomId != null) {
            wrapper.eq(Seat::getRoomId, roomId);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(Seat::getStatus, status);
        }
        if (seatNo != null && !seatNo.isBlank()) {
            wrapper.like(Seat::getSeatNo, seatNo);
        }
        wrapper.orderByAsc(Seat::getRoomId).orderByAsc(Seat::getSeatNo);
        List<Seat> seats = seatMapper.selectList(wrapper);
        seats.forEach(seat -> {
            String redisStatus = stringRedisTemplate.opsForValue().get("seat:status:" + seat.getId());
            if (redisStatus != null && !redisStatus.isBlank()) {
                seat.setStatus(trimQuotes(redisStatus));
            }
        });
        return seats;
    }

    @Override
    public void create(SeatUpsertRequest request) {
        if (studyRoomMapper.selectById(request.getRoomId()) == null) {
            throw new BizException(404, "自习室不存在");
        }
        Seat seat = new Seat();
        seat.setRoomId(request.getRoomId());
        seat.setSeatNo(request.getSeatNo());
        seat.setStatus(request.getStatus());
        seat.setUnderMaintenance(request.getUnderMaintenance() == null ? 0 : request.getUnderMaintenance());
        seatMapper.insert(seat);
    }

    @Override
    public void update(Long id, SeatUpsertRequest request) {
        Seat seat = seatMapper.selectById(id);
        if (seat == null) {
            throw new BizException(404, "座位不存在");
        }
        seat.setRoomId(request.getRoomId());
        seat.setSeatNo(request.getSeatNo());
        seat.setStatus(request.getStatus());
        seat.setUnderMaintenance(request.getUnderMaintenance() == null ? 0 : request.getUnderMaintenance());
        seatMapper.updateById(seat);
        stringRedisTemplate.opsForValue().set("seat:status:" + seat.getId(), seat.getStatus());
    }

    @Override
    public void delete(Long id) {
        seatMapper.deleteById(id);
        stringRedisTemplate.delete("seat:status:" + id);
    }

    private String trimQuotes(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
