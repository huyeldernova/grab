CREATE TABLE saved_places (
                              id           BIGSERIAL     PRIMARY KEY,
                              user_id      BIGINT        NOT NULL REFERENCES customer_profiles(user_id),
                              label        VARCHAR(50)   NOT NULL,
                              address_text VARCHAR(500)  NOT NULL,
                              ward         VARCHAR(100),
                              district     VARCHAR(100),
                              city         VARCHAR(100),
                              latitude     DECIMAL(10,8) NOT NULL,
                              longitude    DECIMAL(11,8) NOT NULL,
                              is_default   BOOLEAN       NOT NULL DEFAULT FALSE,
                              created_at   TIMESTAMP     NOT NULL DEFAULT NOW(),
                              updated_at   TIMESTAMP     NOT NULL DEFAULT NOW(),
                              deleted_at   TIMESTAMP
);