package com.campus.studyroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.studyroom.dto.StudyRoomUpsertRequest;
import com.campus.studyroom.entity.StudyRoom;
import com.campus.studyroom.exception.BizException;
import com.campus.studyroom.mapper.StudyRoomMapper;
import com.campus.studyroom.service.StudyRoomService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudyRoomServiceImpl implements StudyRoomService {

    private final StudyRoomMapper studyRoomMapper;

    public StudyRoomServiceImpl(StudyRoomMapper studyRoomMapper) {
        this.studyRoomMapper = studyRoomMapper;
    }

    @Override
    public List<StudyRoom> listAll() {
        return studyRoomMapper.selectList(new LambdaQueryWrapper<StudyRoom>().orderByAsc(StudyRoom::getFloor));
    }

    @Override
    public void create(StudyRoomUpsertRequest request) {
        StudyRoom room = new StudyRoom();
        room.setName(request.getName());
        room.setFloor(request.getFloor());
        room.setSeatCount(request.getSeatCount());
        room.setOpenTime(request.getOpenTime());
        room.setCloseTime(request.getCloseTime());
        room.setStatus(request.getStatus());
        studyRoomMapper.insert(room);
    }

    @Override
    public void update(Long id, StudyRoomUpsertRequest request) {
        StudyRoom room = studyRoomMapper.selectById(id);
        if (room == null) {
            throw new BizException(404, "自习室不存在");
        }
        room.setName(request.getName());
        room.setFloor(request.getFloor());
        room.setSeatCount(request.getSeatCount());
        room.setOpenTime(request.getOpenTime());
        room.setCloseTime(request.getCloseTime());
        room.setStatus(request.getStatus());
        studyRoomMapper.updateById(room);
    }

    @Override
    public void delete(Long id) {
        studyRoomMapper.deleteById(id);
    }
}
