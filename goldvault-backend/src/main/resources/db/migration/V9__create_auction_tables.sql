CREATE TABLE auction (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id       BIGINT       NOT NULL,
    shop_id         BIGINT       NOT NULL,
    starting_price  DECIMAL(12,2) NOT NULL,
    current_bid     DECIMAL(12,2) NULL,
    winning_bid_id  BIGINT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN, CLOSED, CANCELLED',
    starts_at       DATETIME     NOT NULL,
    ends_at         DATETIME     NOT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME,

    CONSTRAINT fk_auction_ticket FOREIGN KEY (ticket_id) REFERENCES pawn_ticket(id),
    CONSTRAINT fk_auction_shop   FOREIGN KEY (shop_id)   REFERENCES pawn_shop(id),
    CONSTRAINT uq_auction_ticket UNIQUE (ticket_id)
);

CREATE TABLE auction_bid (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    auction_id   BIGINT       NOT NULL,
    bidder_name  VARCHAR(150) NOT NULL,
    bidder_phone VARCHAR(20)  NOT NULL,
    amount       DECIMAL(12,2) NOT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bid_auction FOREIGN KEY (auction_id) REFERENCES auction(id)
);

ALTER TABLE auction ADD CONSTRAINT fk_auction_winning_bid FOREIGN KEY (winning_bid_id) REFERENCES auction_bid(id);