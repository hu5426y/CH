CREATE DATABASE IF NOT EXISTS study_room_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE study_room_db;

DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS violation;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS seat;
DROP TABLE IF EXISTS study_room;
DROP TABLE IF EXISTS system_rule;
DROP TABLE IF EXISTS user;

CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    gender VARCHAR(10) DEFAULT NULL,
    student_no VARCHAR(50) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    role VARCHAR(20) NOT NULL,
    credit_score INT NOT NULL DEFAULT 100,
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_role(role),
    INDEX idx_user_status(status),
    INDEX idx_user_student_no(student_no)
);

CREATE TABLE study_room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    floor INT NOT NULL,
    seat_count INT NOT NULL,
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_room_floor(floor),
    INDEX idx_room_status(status)
);

CREATE TABLE seat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    seat_no VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'FREE',
    under_maintenance TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_room_seat(room_id, seat_no),
    INDEX idx_seat_status(status),
    CONSTRAINT fk_seat_room FOREIGN KEY (room_id) REFERENCES study_room(id)
);

CREATE TABLE reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    checkin_time DATETIME DEFAULT NULL,
    leave_time DATETIME DEFAULT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_res_user(user_id),
    INDEX idx_res_seat(seat_id),
    INDEX idx_res_status(status),
    INDEX idx_res_time(start_time, end_time),
    CONSTRAINT fk_res_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_res_seat FOREIGN KEY (seat_id) REFERENCES seat(id)
);

CREATE TABLE violation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    violation_type VARCHAR(30) NOT NULL,
    violation_time DATETIME NOT NULL,
    score_deducted INT NOT NULL,
    process_status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_vio_user(user_id),
    INDEX idx_vio_type(violation_type),
    INDEX idx_vio_status(process_status),
    CONSTRAINT fk_vio_res FOREIGN KEY (reservation_id) REFERENCES reservation(id),
    CONSTRAINT fk_vio_user FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE system_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_key VARCHAR(80) NOT NULL UNIQUE,
    rule_value VARCHAR(255) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT DEFAULT NULL,
    operation_type VARCHAR(80) NOT NULL,
    content VARCHAR(255) NOT NULL,
    ip VARCHAR(50) DEFAULT NULL,
    operation_time DATETIME NOT NULL,
    result VARCHAR(255) DEFAULT NULL,
    INDEX idx_log_user(user_id),
    INDEX idx_log_type(operation_type),
    INDEX idx_log_time(operation_time)
);
