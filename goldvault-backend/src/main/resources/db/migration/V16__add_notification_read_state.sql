-- Adds in-app "read" tracking to the notification table so the topbar
-- notification bell can show/clear unread ticket-expiry alerts per customer.
-- AML flags (admin) and promotions (customer) are surfaced live from their
-- own tables (aml_flag.status / promotion.created_at) rather than duplicated
-- here — see NotificationFeedService.

ALTER TABLE notification
    ADD COLUMN is_read BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN read_at DATETIME NULL;

CREATE INDEX idx_notification_customer_read ON notification(customer_id, is_read);