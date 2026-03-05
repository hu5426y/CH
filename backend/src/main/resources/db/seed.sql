USE study_room_db;

INSERT INTO user (username, password, real_name, gender, student_no, phone, role, credit_score, status)
VALUES
('admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5vFQbt1zsa3RqeM8cIvxZL8SaJIs/V6', '系统管理员', '男', NULL, '13800000000', 'ADMIN', 100, 'ENABLED'),
('20230001', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5vFQbt1zsa3RqeM8cIvxZL8SaJIs/V6', '张三', '男', '20230001', '13900000001', 'STUDENT', 100, 'ENABLED'),
('20230002', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5vFQbt1zsa3RqeM8cIvxZL8SaJIs/V6', '李四', '女', '20230002', '13900000002', 'STUDENT', 95, 'ENABLED');

INSERT INTO study_room (name, floor, seat_count, open_time, close_time, status)
VALUES
('一号自习室', 1, 50, '08:00:00', '22:00:00', 'OPEN'),
('二号自习室', 2, 50, '08:00:00', '22:00:00', 'OPEN');

INSERT INTO seat (room_id, seat_no, status, under_maintenance)
VALUES
(1, 'A-01', 'FREE', 0),
(1, 'A-02', 'FREE', 0),
(1, 'A-03', 'MAINTENANCE', 1),
(2, 'B-01', 'FREE', 0),
(2, 'B-02', 'FREE', 0);

INSERT INTO system_rule (rule_key, rule_value, description)
VALUES
('MAX_RESERVATION_HOURS', '4', '最大预约时长(小时)'),
('ADVANCE_RESERVATION_HOURS', '72', '最早可提前预约时间(小时)'),
('CHECKIN_TIMEOUT_MINUTES', '15', '超时签到分钟数'),
('CREDIT_THRESHOLD', '60', '最低可预约信用分'),
('VIOLATION_DEDUCT_SCORE', '10', '违约扣分值');
