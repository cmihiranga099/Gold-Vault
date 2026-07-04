ALTER TABLE pawn_shop
    ADD COLUMN license_document_url VARCHAR(500) NULL  COMMENT 'Uploaded pawn broker license file path'   AFTER address,
    ADD COLUMN license_status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                                                 COMMENT 'PENDING, VERIFIED, REJECTED'                     AFTER license_document_url,
    ADD COLUMN license_reject_reason TEXT        NULL  COMMENT 'Reason if admin rejects the license'       AFTER license_status,
    ADD COLUMN license_verified_at  DATETIME     NULL  COMMENT 'When admin marked license as verified'     AFTER license_reject_reason,
    ADD COLUMN license_verified_by  VARCHAR(100) NULL  COMMENT 'Admin username who verified'               AFTER license_verified_at;