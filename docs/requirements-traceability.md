# 需求-实现追踪矩阵（依据论文文档）

## 1. 功能需求映射

- 学生登录/注册/个人中心：
  - 后端：`AuthController`、`UserController`
  - 前端：`LoginView.vue`、`RegisterView.vue`、`ProfileView.vue`

- 座位查询与筛选：
  - 后端：`SeatController#list`
  - 前端：`SeatReservationView.vue`

- 在线预约/取消/签到/签离：
  - 后端：`ReservationController`、`ReservationServiceImpl`
  - 前端：`SeatReservationView.vue`、`MyReservationsView.vue`

- 管理员用户管理：
  - 后端：`UserController` 管理员接口
  - 前端：`UserManageView.vue`

- 自习室与座位管理：
  - 后端：`StudyRoomController`、`SeatController`
  - 前端：`RoomManageView.vue`、`SeatManageView.vue`

- 违约管理：
  - 后端：`ViolationController`、`ReservationViolationSchedule`
  - 前端：`ViolationManageView.vue`

- 规则配置：
  - 后端：`RuleController`、`RuleServiceImpl`
  - 前端：`RuleConfigView.vue`

- 数据统计分析：
  - 后端：`StatisticsController`、`StatisticsServiceImpl`
  - 前端：`DashboardView.vue`

- 系统日志：
  - 后端：`OperationLogAspect`、`OperationLogController`
  - 前端：`LogView.vue`

## 2. 非功能需求映射

- 性能：Redis 缓存、预约加锁、索引设计（`schema.sql`）。
- 安全：JWT + RBAC + BCrypt + 限流过滤器。
- 稳定性：全局异常处理、事务回滚、定时任务自动纠偏。
- 可扩展性：模块化分层 + 规则表驱动参数。
- 部署一致性：Dockerfile + docker-compose。
