package com.campus.studyroom.aop;

import com.campus.studyroom.entity.OperationLog;
import com.campus.studyroom.mapper.OperationLogMapper;
import com.campus.studyroom.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;

    public OperationLogAspect(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint pjp, OpLog opLog) throws Throwable {
        Long userId = SecurityUtils.currentUserId();
        String ip = "unknown";
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            ip = request.getRemoteAddr();
        }

        OperationLog record = new OperationLog();
        record.setUserId(userId);
        record.setOperationType(opLog.value());
        record.setContent(pjp.getSignature().toShortString());
        record.setIp(ip);
        record.setOperationTime(LocalDateTime.now());

        try {
            Object result = pjp.proceed();
            record.setResult("SUCCESS");
            operationLogMapper.insert(record);
            return result;
        } catch (Throwable ex) {
            record.setResult("FAIL: " + ex.getMessage());
            try {
                operationLogMapper.insert(record);
            } catch (Exception e) {
                log.warn("save operation log failed", e);
            }
            throw ex;
        }
    }
}
