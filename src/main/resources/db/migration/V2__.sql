ALTER TABLE user_subscription
    ADD unsubscribed_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE user_subscription
    DROP COLUMN is_subscribed;