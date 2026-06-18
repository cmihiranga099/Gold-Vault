-- GoldVault - V4: Gold marketplace and notification tables

CREATE TABLE gold_rate (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id         BIGINT NOT NULL,
    purity          ENUM('24K','22K','21K','18K','916','750') NOT NULL,
    rate_per_gram   DECIMAL(10,2) NOT NULL,
    effective_date  DATE NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rate_shop FOREIGN KEY (shop_id) REFERENCES pawn_shop(id)
);

CREATE TABLE gold_listing (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id   BIGINT NOT NULL,
    description   VARCHAR(200),
    weight_grams  DECIMAL(8,3) NOT NULL,
    purity        ENUM('24K','22K','21K','18K','916','750') NOT NULL,
    asking_price  DECIMAL(12,2),
    status        ENUM('OPEN','UNDER_REVIEW','SOLD','WITHDRAWN') NOT NULL DEFAULT 'OPEN',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_listing_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
);

CREATE TABLE gold_offer (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id    BIGINT NOT NULL,
    shop_id       BIGINT NOT NULL,
    offer_price   DECIMAL(12,2) NOT NULL,
    message       TEXT,
    status        ENUM('PENDING','ACCEPTED','REJECTED','WITHDRAWN') NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_offer_listing FOREIGN KEY (listing_id) REFERENCES gold_listing(id),
    CONSTRAINT fk_offer_shop FOREIGN KEY (shop_id) REFERENCES pawn_shop(id)
);

CREATE TABLE notification (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id  BIGINT,
    user_id      BIGINT,
    type         ENUM('DUE_REMINDER','PAYMENT_CONFIRM','OFFER_RECEIVED','AUCTION_NOTICE') NOT NULL,
    channel      ENUM('SMS','EMAIL','WHATSAPP','PUSH') NOT NULL,
    message      TEXT NOT NULL,
    sent         BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at      DATETIME,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
);

CREATE INDEX idx_listing_status ON gold_listing(status);
CREATE INDEX idx_offer_status ON gold_offer(status);
CREATE INDEX idx_notification_sent ON notification(sent);