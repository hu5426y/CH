package com.campus.studyroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.studyroom.dto.RuleUpdateRequest;
import com.campus.studyroom.entity.SystemRule;
import com.campus.studyroom.mapper.SystemRuleMapper;
import com.campus.studyroom.service.RuleService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RuleServiceImpl implements RuleService {

    private final SystemRuleMapper systemRuleMapper;

    public RuleServiceImpl(SystemRuleMapper systemRuleMapper) {
        this.systemRuleMapper = systemRuleMapper;
    }

    @PostConstruct
    public void initDefaults() {
        ensureRule("MAX_RESERVATION_HOURS", "4", "最大预约时长(小时)");
        ensureRule("ADVANCE_RESERVATION_HOURS", "72", "最早可提前预约时间(小时)");
        ensureRule("CHECKIN_TIMEOUT_MINUTES", "15", "超时签到分钟数");
        ensureRule("CREDIT_THRESHOLD", "60", "最低可预约信用分");
        ensureRule("VIOLATION_DEDUCT_SCORE", "10", "违约扣分值");
    }

    private void ensureRule(String key, String value, String desc) {
        Long exists = systemRuleMapper.selectCount(new LambdaQueryWrapper<SystemRule>().eq(SystemRule::getRuleKey, key));
        if (exists == null || exists == 0) {
            SystemRule rule = new SystemRule();
            rule.setRuleKey(key);
            rule.setRuleValue(value);
            rule.setDescription(desc);
            rule.setUpdatedAt(LocalDateTime.now());
            systemRuleMapper.insert(rule);
        }
    }

    @Override
    public List<SystemRule> list() {
        return systemRuleMapper.selectList(new LambdaQueryWrapper<SystemRule>().orderByAsc(SystemRule::getRuleKey));
    }

    @Override
    public void upsert(RuleUpdateRequest request) {
        SystemRule rule = systemRuleMapper.selectOne(
                new LambdaQueryWrapper<SystemRule>().eq(SystemRule::getRuleKey, request.getRuleKey()));
        if (rule == null) {
            rule = new SystemRule();
            rule.setRuleKey(request.getRuleKey());
        }
        rule.setRuleValue(request.getRuleValue());
        rule.setDescription(request.getDescription());
        rule.setUpdatedAt(LocalDateTime.now());
        if (rule.getId() == null) {
            systemRuleMapper.insert(rule);
        } else {
            systemRuleMapper.updateById(rule);
        }
    }

    @Override
    public int getIntRule(String key, int defaultValue) {
        SystemRule rule = systemRuleMapper.selectOne(new LambdaQueryWrapper<SystemRule>().eq(SystemRule::getRuleKey, key));
        if (rule == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(rule.getRuleValue());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
