-- GoldVault - V6: Link app_user accounts to customer records
-- A ROLE_CUSTOMER user needs a corresponding row in `customer` so the
-- customer-facing endpoints (tickets, listings, payments) know which
-- customer record belongs to the logged-in user.

ALTER TABLE app_user
    ADD COLUMN customer_id BIGINT NULL,
    ADD CONSTRAINT fk_user_customer FOREIGN KEY (customer_id) REFERENCES customer(id);