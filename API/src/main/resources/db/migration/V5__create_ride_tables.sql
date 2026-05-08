-- =============================================
-- V5: Ride tables
-- =============================================

CREATE TABLE driver_profiles (
                                 user_id     BIGINT       PRIMARY KEY,
                                 full_name   VARCHAR(100) NOT NULL,
                                 avatar_url  VARCHAR(500),
                                 rating      DECIMAL(3,2) NOT NULL DEFAULT 5.00,
                                 is_online   BOOLEAN      NOT NULL DEFAULT FALSE,
                                 current_lat DECIMAL(10,8),
                                 current_lng DECIMAL(11,8),
                                 created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                                 updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),

                                 CONSTRAINT fk_driver_profiles_user
                                     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE vehicles (
                          id           BIGSERIAL    PRIMARY KEY,
                          driver_id    BIGINT       NOT NULL,
                          plate_number VARCHAR(20)  NOT NULL UNIQUE,
                          brand        VARCHAR(50)  NOT NULL,
                          model        VARCHAR(50)  NOT NULL,
                          color        VARCHAR(30)  NOT NULL,
                          vehicle_type VARCHAR(10)  NOT NULL CHECK (vehicle_type IN ('CAR', 'BIKE')),
                          created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),

                          CONSTRAINT fk_vehicles_driver
                              FOREIGN KEY (driver_id) REFERENCES driver_profiles(user_id) ON DELETE CASCADE
);

CREATE TABLE ride_requests (
                               id              BIGSERIAL    PRIMARY KEY,
                               customer_id     BIGINT       NOT NULL,
                               pickup_lat      DECIMAL(10,8) NOT NULL,
                               pickup_lng      DECIMAL(11,8) NOT NULL,
                               pickup_address  VARCHAR(500)  NOT NULL,
                               dropoff_lat     DECIMAL(10,8) NOT NULL,
                               dropoff_lng     DECIMAL(11,8) NOT NULL,
                               dropoff_address VARCHAR(500)  NOT NULL,
                               vehicle_type    VARCHAR(10)  NOT NULL CHECK (vehicle_type IN ('CAR', 'BIKE')),
                               status          VARCHAR(20)  NOT NULL DEFAULT 'SEARCHING'
                                   CHECK (status IN ('SEARCHING', 'MATCHED', 'EXPIRED')),
                               created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
                               expired_at      TIMESTAMP    NOT NULL,

                               CONSTRAINT fk_ride_requests_customer
                                   FOREIGN KEY (customer_id) REFERENCES customer_profiles(user_id)
);

CREATE TABLE rides (
                       id          BIGSERIAL    PRIMARY KEY,
                       request_id  BIGINT       NOT NULL UNIQUE,
                       driver_id   BIGINT       NOT NULL,
                       customer_id BIGINT       NOT NULL,
                       status      VARCHAR(20)  NOT NULL DEFAULT 'MATCHED'
                           CHECK (status IN ('MATCHED','DRIVER_ARRIVED','IN_PROGRESS','COMPLETED','CANCELLED')),
                       pickup_lat      DECIMAL(10,8) NOT NULL,
                       pickup_lng      DECIMAL(11,8) NOT NULL,
                       dropoff_lat     DECIMAL(10,8) NOT NULL,
                       dropoff_lng     DECIMAL(11,8) NOT NULL,
                       start_time      TIMESTAMP,
                       end_time        TIMESTAMP,
                       distance_km     DECIMAL(8,2),
                       fare            DECIMAL(10,2),
                       cancelled_by    VARCHAR(20),
                       cancel_reason   VARCHAR(500),
                       created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
                       updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),

                       CONSTRAINT fk_rides_request
                           FOREIGN KEY (request_id) REFERENCES ride_requests(id),
                       CONSTRAINT fk_rides_driver
                           FOREIGN KEY (driver_id) REFERENCES driver_profiles(user_id),
                       CONSTRAINT fk_rides_customer
                           FOREIGN KEY (customer_id) REFERENCES customer_profiles(user_id)
);

CREATE INDEX idx_rides_driver_id   ON rides(driver_id);
CREATE INDEX idx_rides_customer_id ON rides(customer_id);
CREATE INDEX idx_rides_status      ON rides(status);