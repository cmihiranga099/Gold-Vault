-- Online repayment flow: customer submits a bank-transfer payment with an
-- uploaded receipt; shop staff reviews and approves (creates a real Payment
-- row via the existing PaymentService) or rejects it.

CREATE TABLE payment_submission (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id            BIGINT NOT NULL,
    customer_id          BIGINT NOT NULL,
    amount               DECIMAL(12,2) NOT NULL,
    payment_type         ENUM('INTEREST','PARTIAL','FULL_REDEMPTION','RENEWAL') NOT NULL,
    bank_name            VARCHAR(100),
    reference_number     VARCHAR(100) NOT NULL,
    receipt_url          VARCHAR(500) NOT NULL,
    status               ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    rejection_reason     VARCHAR(500),
    resulting_payment_id BIGINT,
    reviewed_by          BIGINT,
    reviewed_at          DATETIME,
    submitted_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_submission_ticket   FOREIGN KEY (ticket_id) REFERENCES pawn_ticket(id),
    CONSTRAINT fk_payment_submission_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_payment_submission_payment  FOREIGN KEY (resulting_payment_id) REFERENCES payment(id)
);

CREATE INDEX idx_payment_submission_ticket ON payment_submission(ticket_id);
CREATE INDEX idx_payment_submission_status ON payment_submission(status);
CREATE INDEX idx_payment_submission_customer ON payment_submission(customer_id);