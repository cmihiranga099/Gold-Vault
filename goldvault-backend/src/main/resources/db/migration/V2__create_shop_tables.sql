-- GoldVault - V2: Pawn shop, branch, and subscription tables

CREATE TABLE pawn_shop (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    reg_number  VARCHAR(50)  UNIQUE,
    owner_name  VARCHAR(100),
    phone       VARCHAR(20),
    email       VARCHAR(100),
    address     TEXT,
    status      ENUM('PENDING','ACTIVE','SUSPENDED') NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE branch (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id     BIGINT NOT NULL,
    name        VARCHAR(100) NOT NULL,
    address     VARCHAR(255),
    phone       VARCHAR(20),
    is_main     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_branch_shop FOREIGN KEY (shop_id) REFERENCES pawn_shop(id)
);

CREATE TABLE subscription (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id     BIGINT NOT NULL,
    plan        ENUM('BASIC','STANDARD','PREMIUM') NOT NULL,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    status      ENUM('ACTIVE','EXPIRED','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscription_shop FOREIGN KEY (shop_id) REFERENCES pawn_shop(id)
);

-- Now that pawn_shop exists, link app_user to it properly
ALTER TABLE app_user
    ADD CONSTRAINT fk_user_shop FOREIGN KEY (shop_id) REFERENCES pawn_shop(id);

-- Seed one demo shop + branch so Phase 4 (pawn tickets) has something to attach to
INSERT INTO pawn_shop (name, reg_number, owner_name, phone, email, address, status)
VALUES ('GoldVault Demo Pawn Centre', 'REG-2026-001', 'Chamod Perera', '0771234567',
        'demo@goldvault.lk', '123 Galle Road, Colombo 03', 'ACTIVE');

INSERT INTO branch (shop_id, name, address, phone, is_main)
VALUES (1, 'Colombo Main Branch', '123 Galle Road, Colombo 03', '0771234567', TRUE);

-- Link the seeded shopadmin user (from V1) to this shop
UPDATE app_user SET shop_id = 1 WHERE username = 'shopadmin';