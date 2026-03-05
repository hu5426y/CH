package com.campus.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.studyroom.dto.ViolationView;
import com.campus.studyroom.entity.Violation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ViolationMapper extends BaseMapper<Violation> {

    @Select("""
            SELECT v.id, v.reservation_id, v.user_id, u.username, u.real_name, v.violation_type, v.violation_time,
                   v.score_deducted, v.process_status, v.created_at
            FROM violation v
            LEFT JOIN user u ON v.user_id = u.id
            WHERE #{keyword} IS NULL OR u.username LIKE CONCAT('%', #{keyword}, '%') OR u.real_name LIKE CONCAT('%', #{keyword}, '%')
            ORDER BY v.created_at DESC
            """)
    List<ViolationView> listView(@Param("keyword") String keyword);
}
