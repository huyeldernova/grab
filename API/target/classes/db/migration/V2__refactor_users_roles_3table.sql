-- =============================================================================
-- V2: Refactor User-Role relationship from composite PK (V1) to 3-table junction
-- Reason: chosen Option B (Role entity with metadata)
--
-- Changes:
--   1. DROP user_roles (composite PK from V1) — no data to migrate (empty table)
--   2. CREATE roles (id BIGSERIAL, name UNIQUE, description)
--   3. CREATE user_has_role (id BIGSERIAL, user_id FK, role_id FK + UNIQUE)
--   4. ALTER users ADD username
--   5. SEED 4 default roles
-- =============================================================================

-- Step 1: Drop composite-PK user_roles from V1
DROP TABLE IF EXISTS user_roles;

-- Step 2: Create roles entity (with metadata fields)
CREATE TABLE roles (
                       id           BIGSERIAL    PRIMARY KEY,
                       name         VARCHAR(20)  NOT NULL UNIQUE,
                       description  VARCHAR(255),
                       created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Step 3: Create junction table
CREATE TABLE user_has_role (
                               id           BIGSERIAL    PRIMARY KEY,
                               user_id      BIGINT       NOT NULL,
                               role_id      BIGINT       NOT NULL,
                               granted_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_uhr_user
                                   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               CONSTRAINT fk_uhr_role
                                   FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,

    -- prevent duplicate (user, role) pair — same user shouldn't have CUSTOMER twice
                               CONSTRAINT uq_uhr_user_role UNIQUE (user_id, role_id)
);

-- Index for "list all users with role X" query
CREATE INDEX idx_uhr_role_id ON user_has_role(role_id);

-- Index for "get all roles of user X" query
CREATE INDEX idx_uhr_user_id ON user_has_role(user_id);


-- Step 4: Add username column to users
ALTER TABLE users
    ADD COLUMN username VARCHAR(50);


-- Step 5: Seed default roles
INSERT INTO roles (name, description) VALUES
                                          ('CUSTOMER', 'Regular end-user — books rides, orders food, requests delivery'),
                                          ('DRIVER',   'Provides ride and delivery services'),
                                          ('MERCHANT', 'Owns shop or restaurant — sells products via the platform'),
                                          ('ADMIN',    'System administrator — full access to admin tools');