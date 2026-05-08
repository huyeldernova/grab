CREATE TABLE driver_applications (
                                     id              BIGSERIAL       PRIMARY KEY,
                                     user_id         BIGINT          NOT NULL,
                                     full_name       VARCHAR(100)    NOT NULL,
                                     license_number  VARCHAR(50)     NOT NULL,
                                     vehicle_type    VARCHAR(10)     NOT NULL CHECK (vehicle_type IN ('CAR', 'BIKE')),
                                     plate_number    VARCHAR(20)     NOT NULL,
                                     brand           VARCHAR(50),
                                     model           VARCHAR(50),
                                     color           VARCHAR(30),
                                     status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                                         CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
                                     reject_reason   VARCHAR(500),
                                     created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
                                     updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

                                     CONSTRAINT fk_driver_app_user
                                         FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- 1 user chỉ có 1 đơn PENDING tại 1 thời điểm
                                     CONSTRAINT uq_driver_app_pending
                                         UNIQUE (user_id, status)
);