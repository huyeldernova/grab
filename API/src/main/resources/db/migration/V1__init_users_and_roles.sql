-- =============================================================================
-- V1: Initialize Identity & Role tables
-- Theo Schema Design Document, Nhóm 1 (rút gọn cho Phase 1)
-- =============================================================================

CREATE TABLE users (
                       id                   BIGSERIAL    PRIMARY KEY,
                       email                VARCHAR(100) NOT NULL,
                       phone                VARCHAR(20),
                       password_hash        VARCHAR(255) NOT NULL,
                       status               VARCHAR(20)  NOT NULL DEFAULT 'INACTIVE',
                       email_verified_at    TIMESTAMP,
                       phone_verified_at    TIMESTAMP,
                       last_login_at        TIMESTAMP,
                       created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       deleted_at           TIMESTAMP,

                       CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE','INACTIVE','BANNED'))
);

-- Partial unique indexes — UNIQUE chỉ áp dụng cho row chưa soft-delete.
-- Cho phép tạo lại account sau khi soft-delete.
CREATE UNIQUE INDEX uq_users_email
    ON users(email)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_users_phone
    ON users(phone)
    WHERE deleted_at IS NULL AND phone IS NOT NULL;

-- Index cho query thống kê & list theo status
CREATE INDEX idx_users_status
    ON users(status)
    WHERE deleted_at IS NULL;


CREATE TABLE user_roles (
                            user_id     BIGINT       NOT NULL,
                            role        VARCHAR(20)  NOT NULL,
                            granted_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            PRIMARY KEY (user_id, role),
                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            CONSTRAINT chk_user_roles_role
                                CHECK (role IN ('CUSTOMER','DRIVER','MERCHANT','ADMIN'))
);

-- Index cho query "list tất cả users có role X"
CREATE INDEX idx_user_roles_role ON user_roles(role);