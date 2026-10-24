-- Durable outbox: written in the same transaction as the purchase and access grant.
CREATE TABLE android_purchase_acknowledgements (
    purchase_id BIGINT PRIMARY KEY REFERENCES purchases(id) ON DELETE RESTRICT,
    purchase_token TEXT NOT NULL,
    google_order_id VARCHAR(255),
    acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
CREATE INDEX android_purchase_ack_pending ON android_purchase_acknowledgements (acknowledged, next_attempt_at);
