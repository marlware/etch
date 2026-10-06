CREATE TABLE notifications (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT      NOT NULL,
    channel         VARCHAR(20) NOT NULL,
    recipient       VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    retry_count     INT         NOT NULL DEFAULT 0,
    correlation_id  VARCHAR(64),
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         BIGINT      NOT NULL DEFAULT 0
) ENGINE = InnoDB;

CREATE INDEX idx_notifications_order_id ON notifications (order_id);
