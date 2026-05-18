-- =============================================================================
-- V8: Tạo bảng files (upload lifecycle) + thêm document fields vào driver_profiles
-- =============================================================================

-- Step 1: Tạo enum-like check cho target_type và status
CREATE TABLE files (
                       id              BIGSERIAL       PRIMARY KEY,
                       uploader_id     BIGINT          NOT NULL,
                       s3_key          VARCHAR(500)    NOT NULL,
                       url             VARCHAR(1000)   NOT NULL,
                       target_type     VARCHAR(30)     NOT NULL
                           CHECK (target_type IN (
                                                  'AVATAR_CUSTOMER',
                                                  'AVATAR_DRIVER',
                                                  'CMND_FRONT',
                                                  'CMND_BACK',
                                                  'LICENSE_FRONT',
                                                  'LICENSE_BACK',
                                                  'VEHICLE_PHOTO'
                               )),
                       target_id       BIGINT,
                       status          VARCHAR(10)     NOT NULL DEFAULT 'TEMP'
                           CHECK (status IN ('TEMP', 'ACTIVE')),
                       content_type    VARCHAR(100)    NOT NULL,
                       size            BIGINT          NOT NULL,
                       created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

                       CONSTRAINT fk_files_uploader
                           FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Index để cleanup job query TEMP files nhanh
CREATE INDEX idx_files_status_created
    ON files(status, created_at)
    WHERE status = 'TEMP';

-- Index để lấy files theo target
CREATE INDEX idx_files_target
    ON files(target_type, target_id);

-- Step 2: Thêm document fields vào driver_profiles
ALTER TABLE driver_profiles
    ADD COLUMN cmnd_front_url       VARCHAR(1000),
    ADD COLUMN cmnd_back_url        VARCHAR(1000),
    ADD COLUMN license_front_url    VARCHAR(1000),
    ADD COLUMN license_back_url     VARCHAR(1000),
    ADD COLUMN vehicle_photo_url    VARCHAR(1000);