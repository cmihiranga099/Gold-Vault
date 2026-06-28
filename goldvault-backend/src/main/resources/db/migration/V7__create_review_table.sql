CREATE TABLE shop_review (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id     BIGINT       NOT NULL,
    customer_id BIGINT       NOT NULL,
    ticket_id   BIGINT       NOT NULL,
    rating      TINYINT      NOT NULL COMMENT '1 to 5 stars',
    comment     TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'VISIBLE' COMMENT 'VISIBLE or HIDDEN',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME,

    CONSTRAINT fk_review_shop     FOREIGN KEY (shop_id)     REFERENCES pawn_shop(id),
    CONSTRAINT fk_review_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_review_ticket   FOREIGN KEY (ticket_id)   REFERENCES pawn_ticket(id),

    -- One review per ticket (customer can't review same ticket twice)
    CONSTRAINT uq_review_ticket UNIQUE (ticket_id)
);