-- ============================================================================
--  GIRLS HOSTEL MANAGEMENT SYSTEM - MySQL Schema
--  Final Year MCA Project
--  Run this entire script in MySQL Workbench (or `mysql -u root -p < hostel_management.sql`)
--  before running the Java application.
-- ============================================================================

DROP DATABASE IF EXISTS hostel_management;
CREATE DATABASE hostel_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hostel_management;

-- ----------------------------------------------------------------------------
-- 1. HOSTEL CONFIGURATION  (admin can rename the hostel from the control panel)
-- ----------------------------------------------------------------------------
CREATE TABLE hostel_config (
    config_id       INT PRIMARY KEY AUTO_INCREMENT,
    hostel_name     VARCHAR(150) NOT NULL DEFAULT 'My Girls Hostel',
    address         VARCHAR(255),
    contact_number  VARCHAR(20),
    email           VARCHAR(100),
    established_year INT,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 2. ADMIN
-- ----------------------------------------------------------------------------
CREATE TABLE admin (
    admin_id        INT PRIMARY KEY AUTO_INCREMENT,
    username        VARCHAR(50) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    salt            VARCHAR(64) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(100),
    mobile          VARCHAR(20),
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login      TIMESTAMP NULL
);

-- ----------------------------------------------------------------------------
-- 3. ROOM PLANS (plan 1: single room+washroom, plan 2: shared room+shared washroom)
-- ----------------------------------------------------------------------------
CREATE TABLE room_plans (
    plan_id         INT PRIMARY KEY AUTO_INCREMENT,
    plan_name       VARCHAR(100) NOT NULL,
    description     VARCHAR(255),
    base_charge     DECIMAL(10,2) NOT NULL DEFAULT 0,
    extra_charge    DECIMAL(10,2) NOT NULL DEFAULT 0,
    is_active       TINYINT(1) DEFAULT 1
);

-- ----------------------------------------------------------------------------
-- 4. KITCHEN / CANTEEN PLANS
-- ----------------------------------------------------------------------------
CREATE TABLE kitchen_plans (
    kitchen_plan_id INT PRIMARY KEY AUTO_INCREMENT,
    plan_name       VARCHAR(100) NOT NULL,
    monthly_charge  DECIMAL(10,2) NOT NULL DEFAULT 0,
    is_refundable   TINYINT(1) DEFAULT 1,
    is_active       TINYINT(1) DEFAULT 1
);

-- ----------------------------------------------------------------------------
-- 5. ROOMS
-- ----------------------------------------------------------------------------
CREATE TABLE rooms (
    room_id         INT PRIMARY KEY AUTO_INCREMENT,
    room_number     VARCHAR(20) UNIQUE NOT NULL,
    plan_id         INT NOT NULL,
    capacity        INT NOT NULL DEFAULT 1,
    occupied_count  INT NOT NULL DEFAULT 0,
    floor_number    INT,
    is_active       TINYINT(1) DEFAULT 1,
    FOREIGN KEY (plan_id) REFERENCES room_plans(plan_id)
);

-- ----------------------------------------------------------------------------
-- 6. GIRLS (main resident table)
-- ----------------------------------------------------------------------------
CREATE TABLE girls (
    girl_id         INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL,
    gender          VARCHAR(10) DEFAULT 'Female',
    age             INT NOT NULL,
    dob             DATE,
    mobile          VARCHAR(20) NOT NULL,
    email           VARCHAR(100),
    aadhar_number   VARCHAR(20) UNIQUE NOT NULL,
    college_name    VARCHAR(150),
    address         VARCHAR(255),
    photo_path      VARCHAR(255),
    room_id         INT,
    plan_id         INT,
    kitchen_plan_id INT,
    admission_date  DATE NOT NULL,
    leaving_date    DATE NULL,
    status          ENUM('ACTIVE','LEFT') DEFAULT 'ACTIVE',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(room_id),
    FOREIGN KEY (plan_id) REFERENCES room_plans(plan_id),
    FOREIGN KEY (kitchen_plan_id) REFERENCES kitchen_plans(kitchen_plan_id)
);

-- ----------------------------------------------------------------------------
-- 7. GIRL STATUS HISTORY (track active/left transitions)
-- ----------------------------------------------------------------------------
CREATE TABLE girl_status_history (
    history_id      INT PRIMARY KEY AUTO_INCREMENT,
    girl_id         INT NOT NULL,
    status          ENUM('ACTIVE','LEFT') NOT NULL,
    change_date     DATE NOT NULL,
    reason          VARCHAR(255),
    FOREIGN KEY (girl_id) REFERENCES girls(girl_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------------------
-- 8. PARENTS / GUARDIAN  (relation_type: FATHER, MOTHER, GUARDIAN)
-- ----------------------------------------------------------------------------
CREATE TABLE parents (
    parent_id       INT PRIMARY KEY AUTO_INCREMENT,
    girl_id         INT NOT NULL,
    relation_type   ENUM('FATHER','MOTHER','GUARDIAN') NOT NULL,
    name            VARCHAR(100) NOT NULL,
    mobile          VARCHAR(20) NOT NULL,
    email           VARCHAR(100),
    aadhar_number   VARCHAR(20),
    occupation      VARCHAR(100),
    address         VARCHAR(255),
    FOREIGN KEY (girl_id) REFERENCES girls(girl_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------------------
-- 9. GIRL CREDENTIALS (login account generated by admin; temp password flag)
-- ----------------------------------------------------------------------------
CREATE TABLE girl_credentials (
    credential_id   INT PRIMARY KEY AUTO_INCREMENT,
    girl_id         INT UNIQUE NOT NULL,
    username        VARCHAR(50) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    salt            VARCHAR(64) NOT NULL,
    is_temp_password TINYINT(1) DEFAULT 1,
    last_login      TIMESTAMP NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (girl_id) REFERENCES girls(girl_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------------------
-- 10. PARENT CREDENTIALS (parents log in via the girl's account, but with their
--     own password so multiple parents/guardians can each have a login)
-- ----------------------------------------------------------------------------
CREATE TABLE parent_credentials (
    credential_id   INT PRIMARY KEY AUTO_INCREMENT,
    parent_id       INT UNIQUE NOT NULL,
    girl_id         INT NOT NULL,
    username        VARCHAR(50) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    salt            VARCHAR(64) NOT NULL,
    is_temp_password TINYINT(1) DEFAULT 1,
    last_login      TIMESTAMP NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES parents(parent_id) ON DELETE CASCADE,
    FOREIGN KEY (girl_id) REFERENCES girls(girl_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------------------
-- 11. FEE STRUCTURE (per-girl charges fixed at admission)
-- ----------------------------------------------------------------------------
CREATE TABLE fee_structure (
    fee_id              INT PRIMARY KEY AUTO_INCREMENT,
    girl_id             INT UNIQUE NOT NULL,
    monthly_stay_bill   DECIMAL(10,2) NOT NULL,
    emergency_deposit   DECIMAL(10,2) NOT NULL,
    electricity_deposit DECIMAL(10,2) NOT NULL,
    wifi_deposit        DECIMAL(10,2) NOT NULL DEFAULT 0,
    plan_extra_charge   DECIMAL(10,2) NOT NULL DEFAULT 0,
    kitchen_charge      DECIMAL(10,2) NOT NULL DEFAULT 0,
    effective_date      DATE NOT NULL,
    FOREIGN KEY (girl_id) REFERENCES girls(girl_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------------------
-- 12. BILLS
-- ----------------------------------------------------------------------------
CREATE TABLE bills (
    bill_id         INT PRIMARY KEY AUTO_INCREMENT,
    girl_id         INT NOT NULL,
    bill_type       ENUM('MONTHLY_STAY','EMERGENCY_DEPOSIT','ELECTRICITY_DEPOSIT','WIFI_DEPOSIT','KITCHEN','FINE','OTHER') NOT NULL,
    bill_month      INT,
    bill_year       INT,
    amount          DECIMAL(10,2) NOT NULL,
    due_date        DATE NOT NULL,
    status          ENUM('UNPAID','PAID','PARTIAL') DEFAULT 'UNPAID',
    generated_date  DATE NOT NULL,
    generated_by    INT,
    remarks         VARCHAR(255),
    FOREIGN KEY (girl_id) REFERENCES girls(girl_id) ON DELETE CASCADE,
    FOREIGN KEY (generated_by) REFERENCES admin(admin_id)
);

-- ----------------------------------------------------------------------------
-- 13. PAYMENTS
-- ----------------------------------------------------------------------------
CREATE TABLE payments (
    payment_id      INT PRIMARY KEY AUTO_INCREMENT,
    bill_id         INT NOT NULL,
    girl_id         INT NOT NULL,
    amount_paid     DECIMAL(10,2) NOT NULL,
    payment_date    DATE NOT NULL,
    payment_mode    ENUM('CASH','CARD','UPI','BANK_TRANSFER','OTHER') DEFAULT 'CASH',
    received_by     INT,
    receipt_no      VARCHAR(50) UNIQUE,
    FOREIGN KEY (bill_id) REFERENCES bills(bill_id) ON DELETE CASCADE,
    FOREIGN KEY (girl_id) REFERENCES girls(girl_id) ON DELETE CASCADE,
    FOREIGN KEY (received_by) REFERENCES admin(admin_id)
);

-- ----------------------------------------------------------------------------
-- 14. DUES (auto-derived shortfall tracking)
-- ----------------------------------------------------------------------------
CREATE TABLE dues (
    due_id          INT PRIMARY KEY AUTO_INCREMENT,
    girl_id         INT NOT NULL,
    bill_id         INT NOT NULL,
    amount_due      DECIMAL(10,2) NOT NULL,
    due_date        DATE NOT NULL,
    status          ENUM('PENDING','CLEARED') DEFAULT 'PENDING',
    cleared_date    DATE NULL,
    FOREIGN KEY (girl_id) REFERENCES girls(girl_id) ON DELETE CASCADE,
    FOREIGN KEY (bill_id) REFERENCES bills(bill_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------------------
-- 15. FINES
-- ----------------------------------------------------------------------------
CREATE TABLE fines (
    fine_id         INT PRIMARY KEY AUTO_INCREMENT,
    girl_id         INT NOT NULL,
    reason          VARCHAR(255) NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    fine_date       DATE NOT NULL,
    status          ENUM('UNPAID','PAID') DEFAULT 'UNPAID',
    imposed_by      INT,
    FOREIGN KEY (girl_id) REFERENCES girls(girl_id) ON DELETE CASCADE,
    FOREIGN KEY (imposed_by) REFERENCES admin(admin_id)
);

-- ----------------------------------------------------------------------------
-- 16. CANTEEN SERVICE LOG (daily tick: rendered / not rendered)
-- ----------------------------------------------------------------------------
CREATE TABLE canteen_service_log (
    log_id          INT PRIMARY KEY AUTO_INCREMENT,
    service_date    DATE UNIQUE NOT NULL,
    status          ENUM('RENDERED','NOT_RENDERED') NOT NULL,
    remarks         VARCHAR(255),
    marked_by       INT,
    marked_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (marked_by) REFERENCES admin(admin_id)
);

-- ----------------------------------------------------------------------------
-- 17. CANTEEN REFUNDS (auto-generated for all active girls when a day is
--     marked NOT_RENDERED)
-- ----------------------------------------------------------------------------
CREATE TABLE canteen_refunds (
    refund_id       INT PRIMARY KEY AUTO_INCREMENT,
    girl_id         INT NOT NULL,
    log_id          INT NOT NULL,
    refund_amount   DECIMAL(10,2) NOT NULL,
    refund_date     DATE,
    status          ENUM('PENDING','PAID') DEFAULT 'PENDING',
    FOREIGN KEY (girl_id) REFERENCES girls(girl_id) ON DELETE CASCADE,
    FOREIGN KEY (log_id) REFERENCES canteen_service_log(log_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------------------
-- 18. NOTICES
-- ----------------------------------------------------------------------------
CREATE TABLE notices (
    notice_id       INT PRIMARY KEY AUTO_INCREMENT,
    title           VARCHAR(150) NOT NULL,
    message         TEXT NOT NULL,
    target_type     ENUM('SPECIFIC_GIRL','ALL_GIRLS','SPECIFIC_PARENT','ALL_PARENTS') NOT NULL,
    target_girl_id  INT NULL,
    created_by      INT NOT NULL,
    created_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (target_girl_id) REFERENCES girls(girl_id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES admin(admin_id)
);

-- ----------------------------------------------------------------------------
-- 19. NOTICE RECIPIENTS (fan-out / read tracking)
-- ----------------------------------------------------------------------------
CREATE TABLE notice_recipients (
    recipient_id    INT PRIMARY KEY AUTO_INCREMENT,
    notice_id       INT NOT NULL,
    girl_id         INT NULL,
    parent_id       INT NULL,
    is_read         TINYINT(1) DEFAULT 0,
    read_at         TIMESTAMP NULL,
    FOREIGN KEY (notice_id) REFERENCES notices(notice_id) ON DELETE CASCADE,
    FOREIGN KEY (girl_id) REFERENCES girls(girl_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES parents(parent_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------------------
-- 20. COMPLAINTS
-- ----------------------------------------------------------------------------
CREATE TABLE complaints (
    complaint_id    INT PRIMARY KEY AUTO_INCREMENT,
    girl_id         INT NOT NULL,
    subject         VARCHAR(150) NOT NULL,
    description     TEXT NOT NULL,
    status          ENUM('OPEN','IN_PROGRESS','RESOLVED') DEFAULT 'OPEN',
    filed_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_date   TIMESTAMP NULL,
    admin_remarks   VARCHAR(255),
    FOREIGN KEY (girl_id) REFERENCES girls(girl_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------------------
-- 21. CHAT MESSAGES (persisted log of the socket chat application)
-- ----------------------------------------------------------------------------
CREATE TABLE chat_messages (
    message_id      INT PRIMARY KEY AUTO_INCREMENT,
    sender_type     ENUM('ADMIN','GIRL','PARENT') NOT NULL,
    sender_id       INT NOT NULL,
    sender_name     VARCHAR(100),
    receiver_type   ENUM('ADMIN','GIRL','PARENT','BROADCAST') NOT NULL,
    receiver_id     INT NULL,
    message         TEXT NOT NULL,
    sent_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read         TINYINT(1) DEFAULT 0
);

-- ----------------------------------------------------------------------------
-- 22. AUDIT LOG
-- ----------------------------------------------------------------------------
CREATE TABLE audit_log (
    log_id          INT PRIMARY KEY AUTO_INCREMENT,
    user_type       ENUM('ADMIN','GIRL','PARENT','SYSTEM') NOT NULL,
    user_id         INT,
    action          VARCHAR(255) NOT NULL,
    details         VARCHAR(500),
    log_timestamp   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 23. PASSWORD RESET REQUESTS
-- ----------------------------------------------------------------------------
CREATE TABLE password_reset_requests (
    request_id      INT PRIMARY KEY AUTO_INCREMENT,
    user_type       ENUM('ADMIN','GIRL','PARENT') NOT NULL,
    user_id         INT NOT NULL,
    reset_token     VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP NOT NULL,
    is_used         TINYINT(1) DEFAULT 0
);

-- ----------------------------------------------------------------------------
-- 24. GENERATED REPORTS LOG
-- ----------------------------------------------------------------------------
CREATE TABLE reports_generated (
    report_id       INT PRIMARY KEY AUTO_INCREMENT,
    report_type     VARCHAR(100) NOT NULL,
    related_girl_id INT NULL,
    generated_by_type ENUM('ADMIN','GIRL','PARENT') NOT NULL,
    generated_by_id INT,
    generated_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_path       VARCHAR(255),
    FOREIGN KEY (related_girl_id) REFERENCES girls(girl_id) ON DELETE SET NULL
);

-- ============================================================================
--  SEED DATA
-- ============================================================================

INSERT INTO hostel_config (hostel_name, address, contact_number, email, established_year)
VALUES ('Sunrise Girls Hostel', 'Near XYZ College, Patna, Bihar', '9999999999', 'admin@hostel.local', 2015);

-- Default admin login -> username: admin / password: Admin@123
-- (Password hashing scheme: SHA-256(password + salt), see PasswordUtil.java)
INSERT INTO admin (username, password_hash, salt, full_name, email, mobile)
VALUES ('admin', 'PLACEHOLDER_HASH', 'PLACEHOLDER_SALT', 'Hostel Administrator', 'admin@hostel.local', '9999999999');

INSERT INTO room_plans (plan_name, description, base_charge, extra_charge) VALUES
('Single Room + Washroom', 'Private single occupancy room with attached washroom', 6000.00, 1500.00),
('Shared Room + Shared Washroom', 'Double/triple sharing room with common washroom', 4000.00, 500.00);

INSERT INTO kitchen_plans (plan_name, monthly_charge, is_refundable) VALUES
('Monthly Canteen Supply', 3000.00, 1);

INSERT INTO rooms (room_number, plan_id, capacity) VALUES
('S-101', 1, 1), ('S-102', 1, 1), ('S-103', 1, 1),
('SH-201', 2, 3), ('SH-202', 2, 3), ('SH-203', 2, 3);

-- ============================================================================
--  NOTE: The default admin password hash/salt above is a placeholder.
--  On first run, the Java application's DatabaseInitializer will detect the
--  placeholder and automatically set a real bcrypt-style salted hash for
--  username 'admin' / password 'Admin@123' so you can log in immediately.
-- ============================================================================
