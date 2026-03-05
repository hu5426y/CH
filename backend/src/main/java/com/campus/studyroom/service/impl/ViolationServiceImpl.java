package com.campus.studyroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.studyroom.dto.ViolationView;
import com.campus.studyroom.entity.User;
import com.campus.studyroom.entity.Violation;
import com.campus.studyroom.exception.BizException;
import com.campus.studyroom.mapper.UserMapper;
import com.campus.studyroom.mapper.ViolationMapper;
import com.campus.studyroom.service.ViolationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ViolationServiceImpl implements ViolationService {

    private final ViolationMapper violationMapper;
    private final UserMapper userMapper;

    public ViolationServiceImpl(ViolationMapper violationMapper, UserMapper userMapper) {
        this.violationMapper = violationMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<ViolationView> list(String keyword) {
        return violationMapper.listView(keyword == null || keyword.isBlank() ? null : keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(Long violationId) {
        Violation violation = violationMapper.selectById(violationId);
        if (violation == null) {
            throw new BizException(404, "违约记录不存在");
        }
        if (!"ACTIVE".equals(violation.getProcessStatus())) {
            throw new BizException(400, "当前记录不可撤销");
        }

        violation.setProcessStatus("REVOKED");
        violationMapper.updateById(violation);

        User user = userMapper.selectById(violation.getUserId());
        if (user != null) {
            user.setCreditScore((user.getCreditScore() == null ? 0 : user.getCreditScore()) + violation.getScoreDeducted());
            userMapper.updateById(user);
        }
    }
}
