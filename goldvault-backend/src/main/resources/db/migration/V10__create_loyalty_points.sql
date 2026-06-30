ALTER TABLE customer
    ADD COLUMN loyalty_points INT NOT NULL DEFAULT 0 AFTER kyc_status;

CREATE TABLE loyalty_transaction (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id   BIGINT       NOT NULL,
    ticket_id     BIGINT       NULL,
    points        INT          NOT NULL COMMENT 'positive = earned, negative = redeemed',
    reason        VARCHAR(50)  NOT NULL COMMENT 'ON_TIME_REDEMPTION, PAYMENT, REVIEW, REDEEMED_FOR_DISCOUNT, MANUAL_ADJUSTMENT',
    description   VARCHAR(255) NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_loyalty_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_loyalty_ticket   FOREIGN KEY (ticket_id)   REFERENCES pawn_ticket(id)
);