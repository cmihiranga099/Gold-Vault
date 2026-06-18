-- GoldVault - V1: App user table
-- This is the only table needed for Phase 2 JWT auth to work.
-- All other tables (pawn_shop, customer, pawn_ticket, etc.) come in Phase 3.

CREATE TABLE IF NOT EXISTS app_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    email       VARCHAR(100) UNIQUE,
    full_name   VARCHAR(100),
    role        ENUM('ROLE_ADMIN','ROLE_SHOP_ADMIN','ROLE_STAFF','ROLE_CUSTOMER') NOT NULL,
    shop_id     BIGINT,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Default admin account: username=admin, password=Admin@123
INSERT INTO app_user (username, password, email, full_name, role, enabled)
VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'admin@goldvault.lk',
    'GoldVault Admin',
    'ROLE_ADMIN',
    TRUE
);

-- Default shop admin: username=shopadmin, password=Admin@123
INSERT INTO app_user (username, password, email, full_name, role, enabled)
VALUES (
    'shopadmin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'shop@goldvault.lk',
    'Shop Admin',
    'ROLE_SHOP_ADMIN',
    TRUE
);