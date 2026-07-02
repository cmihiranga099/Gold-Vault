CREATE TABLE shop_api_key (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id     BIGINT       NOT NULL,
    api_key     VARCHAR(64)  NOT NULL UNIQUE COMMENT 'SHA-256 hashed key stored here',
    key_prefix  VARCHAR(8)   NOT NULL COMMENT 'First 8 chars shown to admin for identification',
    label       VARCHAR(100) NULL     COMMENT 'Human label e.g. "Main POS", "Branch 2 POS"',
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    last_used   DATETIME     NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_api_key_shop FOREIGN KEY (shop_id) REFERENCES pawn_shop(id)
);