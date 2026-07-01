CREATE TABLE market_gold_rate (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    purity        VARCHAR(10)   NOT NULL COMMENT 'K24, K22, K21, K18, etc.',
    rate_per_gram DECIMAL(10,2) NOT NULL COMMENT 'LKR per gram, sourced from world price',
    source        VARCHAR(50)   NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL or AUTO',
    effective_date DATE         NOT NULL,
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uq_market_rate_purity_date (purity, effective_date)
);