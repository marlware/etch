CREATE DATABASE IF NOT EXISTS etch_orders;
CREATE DATABASE IF NOT EXISTS etch_notifications;

CREATE USER IF NOT EXISTS 'etch'@'%' IDENTIFIED BY 'etch';
GRANT ALL PRIVILEGES ON etch_orders.* TO 'etch'@'%';
GRANT ALL PRIVILEGES ON etch_notifications.* TO 'etch'@'%';
FLUSH PRIVILEGES;
