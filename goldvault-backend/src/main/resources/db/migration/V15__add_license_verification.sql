DROP PROCEDURE IF EXISTS gv_add_license_columns;

CREATE PROCEDURE gv_add_license_columns()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'pawn_shop'
          AND COLUMN_NAME = 'license_document_url'
    ) THEN
        ALTER TABLE pawn_shop
            ADD COLUMN license_document_url VARCHAR(500) NULL
                COMMENT 'Uploaded pawn broker license file path' AFTER address;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'pawn_shop'
          AND COLUMN_NAME = 'license_status'
    ) THEN
        ALTER TABLE pawn_shop
            ADD COLUMN license_status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                COMMENT 'PENDING, VERIFIED, REJECTED' AFTER license_document_url;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'pawn_shop'
          AND COLUMN_NAME = 'license_reject_reason'
    ) THEN
        ALTER TABLE pawn_shop
            ADD COLUMN license_reject_reason TEXT NULL
                COMMENT 'Reason if admin rejects the license' AFTER license_status;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'pawn_shop'
          AND COLUMN_NAME = 'license_verified_at'
    ) THEN
        ALTER TABLE pawn_shop
            ADD COLUMN license_verified_at DATETIME NULL
                COMMENT 'When admin marked license as verified' AFTER license_reject_reason;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'pawn_shop'
          AND COLUMN_NAME = 'license_verified_by'
    ) THEN
        ALTER TABLE pawn_shop
            ADD COLUMN license_verified_by VARCHAR(100) NULL
                COMMENT 'Admin username who verified' AFTER license_verified_at;
    END IF;
END;

CALL gv_add_license_columns();

DROP PROCEDURE gv_add_license_columns;
