CREATE TABLE promotion (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id         BIGINT          NOT NULL,
    title           VARCHAR(150)    NOT NULL,
    description     TEXT,
    promo_type      VARCHAR(30)     NOT NULL COMMENT 'REDUCED_INTEREST, BONUS_POINTS, FREE_RENEWAL, CUSTOM',
    promo_value     DECIMAL(10,2)   NULL     COMMENT 'e.g. 1.5 for 1.5% interest rate',
    starts_at       DATETIME        NOT NULL,
    ends_at         DATETIME        NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, EXPIRED, CANCELLED',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME,

    CONSTRAINT fk_promotion_shop FOREIGN KEY (shop_id) REFERENCES pawn_shop(id)
);