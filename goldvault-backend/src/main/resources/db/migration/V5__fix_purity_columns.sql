-- GoldVault - V5: Fix purity columns to VARCHAR
-- Hibernate's schema validator expects VARCHAR for columns mapped via @Converter,
-- not native MySQL ENUM. This migration converts all purity columns accordingly.

ALTER TABLE gold_item
    MODIFY COLUMN purity VARCHAR(10) NOT NULL;

ALTER TABLE gold_rate
    MODIFY COLUMN purity VARCHAR(10) NOT NULL;

ALTER TABLE gold_listing
    MODIFY COLUMN purity VARCHAR(10) NOT NULL;