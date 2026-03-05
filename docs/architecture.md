# 架构设计说明

## 1. 总体架构

系统采用前后端分离三层架构：
- 展示层：Vue3 + Element Plus（学生端/管理员端）。
- 服务层：SpringBoot（Controller + Service + Security + AOP + Schedule）。
- 数据层：MySQL（持久化）+ Redis（热点缓存、并发锁、限流计数）。

## 2. 核心模块

- 用户管理模块：登录注册、个人中心、管理员账号管理。
- 座位管理模块：座位状态维护（空闲/预约/占用/维修/禁用）。
- 预约管理模块：预约申请、取消、签到、签离、管理员干预。
- 自习室管理模块：自习室信息管理、开放状态管理。
- 违约管理模块：自动检测违约、扣分、管理员撤销。
- 规则配置模块：通过 system_rule 表动态读取规则。
- 统计分析模块：总览、自习室使用情况、预约趋势。
- 日志模块：AOP 记录操作人、操作类型、操作结果、IP。

## 3. 安全设计

- Spring Security + JWT：无状态认证。
- RBAC：`ROLE_STUDENT` 与 `ROLE_ADMIN`。
- BCrypt 密码加密。
- 限流：`IpRateLimitFilter` 基于 Redis 每秒限流。
- 统一异常：`GlobalExceptionHandler`。

## 4. 并发与一致性

- 预约核心逻辑使用事务控制。
- Redis `setIfAbsent` 做座位预约短锁，避免同座并发抢占。
- 数据库预约冲突校验（时间重叠检测）。
- 定时任务每分钟扫描超时未签到记录并自动违约。

## 5. 数据库实体映射

核心表：`user / study_room / seat / reservation / violation / system_rule / operation_log`。

关系：
- `study_room 1-n seat`
- `user 1-n reservation`
- `reservation 1-n violation`（实际业务上常见为 0/1）
- `user 1-n operation_log`
