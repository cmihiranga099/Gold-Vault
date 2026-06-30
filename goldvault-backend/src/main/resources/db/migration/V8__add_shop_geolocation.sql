ALTER TABLE pawn_shop
    ADD COLUMN latitude  DECIMAL(10,7) NULL AFTER address,
    ADD COLUMN longitude DECIMAL(10,7) NULL AFTER latitude;