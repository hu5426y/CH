package com.campus.studyroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("study_room")
public class StudyRoom extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer floor;
    private Integer seatCount;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String status;
}
