package com.campus.studyroom.service;

import com.campus.studyroom.dto.StudyRoomUpsertRequest;
import com.campus.studyroom.entity.StudyRoom;

import java.util.List;

public interface StudyRoomService {
    List<StudyRoom> listAll();

    void create(StudyRoomUpsertRequest request);

    void update(Long id, StudyRoomUpsertRequest request);

    void delete(Long id);
}
