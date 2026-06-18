-- GoldVault - V3: Customer, pawn ticket, gold item, and payment tables

CREATE TABLE customer (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id        BIGINT NOT NULL,
    full_name      VARCHAR(100) NOT NULL,
    nic            VARCHAR(20)  NOT NULL UNIQUE,
    phone          VARCHAR(20),
    email          VARCHAR(100),
    address        TEXT,
    dob            DATE,
    kyc_status     ENUM('PENDING','VERIFIED','REJECTED') NOT NULL DEFAULT 'PENDING',
    nic_photo_url  VARCHAR(500),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_customer_shop FOREIGN KEY (shop_id) REFERENCES pawn_shop(id)
);

CREATE TABLE pawn_ticket (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_number   VARCHAR(30) NOT NULL UNIQUE,
    customer_id     BIGINT NOT NULL,
    shop_id         BIGINT NOT NULL,
    branch_id       BIGINT,
    loan_amount     DECIMAL(12,2) NOT NULL,
    interest_rate   DECIMAL(5,2)  NOT NULL,
    interest_type   ENUM('FLAT','REDUCING') NOT NULL DEFAULT 'FLAT',
    pawn_date       DATE NOT NULL,
    expiry_date     DATE NOT NULL,
    status          ENUM('ACTIVE','REDEEMED','EXPIRED','AUCTIONED') NOT NULL DEFAULT 'ACTIVE',
    qr_code         TEXT,
    notes           TEXT,
    created_by      BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_ticket_shop FOREIGN KEY (shop_id) REFERENCES pawn_shop(id),
    CONSTRAINT fk_ticket_branch FOREIGN KEY (branch_id) REFERENCES branch(id)
);

CREATE TABLE gold_item (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id        BIGINT NOT NULL,
    description      VARCHAR(200) NOT NULL,
    gold_type        ENUM('NECKLACE','RING','BANGLE','EARRING','CHAIN','OTHER') NOT NULL DEFAULT 'OTHER',
    weight_grams     DECIMAL(8,3) NOT NULL,
    purity           ENUM('24K','22K','21K','18K','916','750','OTHER') NOT NULL,
    estimated_value  DECIMAL(12,2),
    photo_url        VARCHAR(500),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_ticket FOREIGN KEY (ticket_id) REFERENCES pawn_ticket(id)
);

CREATE TABLE payment (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id         BIGINT NOT NULL,
    amount            DECIMAL(12,2) NOT NULL,
    payment_type      ENUM('INTEREST','PARTIAL','FULL_REDEMPTION') NOT NULL,
    payment_method    ENUM('CASH','CARD','ONLINE_TRANSFER','LANKAQR') NOT NULL DEFAULT 'CASH',
    reference_number  VARCHAR(100),
    payment_date      DATETIME NOT NULL,
    received_by       BIGINT,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_ticket FOREIGN KEY (ticket_id) REFERENCES pawn_ticket(id)
);

CREATE INDEX idx_customer_nic ON customer(nic);
CREATE INDEX idx_ticket_status ON pawn_ticket(status);
CREATE INDEX idx_ticket_expiry ON pawn_ticket(expiry_date);
CREATE INDEX idx_ticket_customer ON pawn_ticket(customer_id);