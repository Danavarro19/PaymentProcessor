CREATE INDEX idx_payments_status_created_customer
    ON payments (status, created_at, customer_id);