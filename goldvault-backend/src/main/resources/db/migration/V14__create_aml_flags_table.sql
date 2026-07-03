CREATE TABLE aml_flag (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id   BIGINT       NOT NULL,
    shop_id       BIGINT       NOT NULL,
    ticket_id     BIGINT       NULL,
    flag_type     VARCHAR(50)  NOT NULL COMMENT 'LARGE_TRANSACTION, HIGH_VOLUME, RAPID_CYCLING, MULTIPLE_SHOPS',
    description   TEXT         NOT NULL,
    amount        DECIMAL(14,2) NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN, REVIEWED, DISMISSED',
    reviewed_by   VARCHAR(100) NULL,
    reviewed_at   DATETIME     NULL,
    review_note   TEXT         NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_aml_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_aml_shop     FOREIGN KEY (shop_id)     REFERENCES pawn_shop(id),
    CONSTRAINT fk_aml_ticket   FOREIGN KEY (ticket_id)   REFERENCES pawn_ticket(id)
);

CREATE INDEX idx_aml_customer ON aml_flag(customer_id);
CREATE INDEX idx_aml_status   ON aml_flag(status);