CREATE TABLE customer_profiles (
                                   user_id     BIGINT       PRIMARY KEY REFERENCES users(id),
                                   full_name   VARCHAR(100) NOT NULL,
                                   avatar_url  VARCHAR(500),
                                   date_of_birth DATE,
                                   gender      VARCHAR(10)  CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
                                   created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                                   updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);