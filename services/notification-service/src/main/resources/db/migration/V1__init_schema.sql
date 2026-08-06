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

CREATE TABLE notification_audit (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_id BIGINT      NOT NULL,
    event           VARCHAR(40) NOT NULL,
    details         VARCHAR(1000),
    timestamp       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;

CREATE INDEX idx_notification_audit_notification_id ON notification_audit (notification_id);

CREATE TABLE notification_dlt (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_id   BIGINT,
    order_id          BIGINT       NOT NULL,
    channel           VARCHAR(20)  NOT NULL,
    original_payload  TEXT,
    failure_reason    VARCHAR(500) NOT NULL,
    retry_count       INT          NOT NULL DEFAULT 0,
    failed_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;

CREATE INDEX idx_notification_dlt_failed_at ON notification_dlt (failed_at);
