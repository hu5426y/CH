# 接口清单（核心）

统一返回：

```json
{ "code": 0, "message": "success", "data": {} }
```

## 1. 认证

- `POST /api/auth/login` 登录
- `POST /api/auth/register` 学生注册

## 2. 学生端

- `GET /api/user/profile` 个人信息
- `PUT /api/user/profile` 修改资料
- `PUT /api/user/password` 修改密码
- `GET /api/rooms` 自习室列表
- `GET /api/seats` 座位筛选
- `POST /api/reservation/add` 预约申请
- `POST /api/reservation/{id}/cancel` 取消预约
- `POST /api/reservation/{id}/checkin` 签到
- `POST /api/reservation/{id}/checkout` 签离
- `GET /api/reservation/my` 个人预约记录

## 3. 管理员端

- `GET /api/admin/users` 学生列表
- `PUT /api/admin/users/status` 启停账号
- `PUT /api/admin/users/reset-password` 重置密码

- `POST /api/admin/rooms` 新增自习室
- `PUT /api/admin/rooms/{id}` 编辑自习室
- `DELETE /api/admin/rooms/{id}` 删除自习室

- `POST /api/admin/seats` 新增座位
- `PUT /api/admin/seats/{id}` 编辑座位
- `DELETE /api/admin/seats/{id}` 删除座位

- `GET /api/admin/reservation` 全部预约查询
- `POST /api/admin/reservation/{id}/cancel` 管理员取消预约

- `GET /api/admin/violation` 违约记录查询
- `POST /api/admin/violation/{id}/revoke` 撤销违约

- `GET /api/admin/rule` 规则列表
- `POST /api/admin/rule` 规则更新

- `GET /api/admin/statistics/overview` 总览
- `GET /api/admin/statistics/room-usage` 自习室使用统计
- `GET /api/admin/statistics/trend?days=7` 预约趋势

- `GET /api/admin/logs` 操作日志
