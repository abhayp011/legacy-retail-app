-- Acme Retail legacy schema (MySQL 8)
CREATE DATABASE IF NOT EXISTS retail CHARACTER SET utf8mb4;
USE retail;

CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_md5  CHAR(32)     NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    active        TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE customers (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    status        VARCHAR(12)  NOT NULL DEFAULT 'ACTIVE',
    credit_limit  DECIMAL(12,2) NOT NULL DEFAULT 10000.00,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE products (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku           VARCHAR(30)  NOT NULL UNIQUE,
    name          VARCHAR(120) NOT NULL,
    category      VARCHAR(40),
    unit_price    DECIMAL(10,2) NOT NULL,
    active        TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE product_price_history (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id    BIGINT       NOT NULL,
    old_price     DECIMAL(10,2),
    new_price     DECIMAL(10,2),
    changed_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pph_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;

CREATE TABLE inventory (
    product_id    BIGINT       NOT NULL PRIMARY KEY,
    stock_qty     INT          NOT NULL DEFAULT 0,
    reorder_level INT          NOT NULL DEFAULT 10,
    CONSTRAINT fk_inv_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;

CREATE TABLE orders (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no      VARCHAR(30)  NOT NULL UNIQUE,
    customer_id   BIGINT       NOT NULL,
    status        VARCHAR(12)  NOT NULL DEFAULT 'NEW',
    subtotal      DECIMAL(12,2) NOT NULL,
    discount      DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax           DECIMAL(12,2) NOT NULL DEFAULT 0,
    total         DECIMAL(12,2) NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
) ENGINE=InnoDB;

CREATE TABLE order_items (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id      BIGINT       NOT NULL,
    product_id    BIGINT       NOT NULL,
    qty           INT          NOT NULL,
    unit_price    DECIMAL(10,2) NOT NULL,
    line_total    DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_items_order   FOREIGN KEY (order_id)   REFERENCES orders(id),
    CONSTRAINT fk_items_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;

CREATE TABLE order_audit (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id      BIGINT       NOT NULL,
    old_status    VARCHAR(12),
    new_status    VARCHAR(12),
    changed_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE invoices (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id      BIGINT       NOT NULL UNIQUE,
    customer_id   BIGINT       NOT NULL,
    amount        DECIMAL(12,2) NOT NULL,
    paid_amount   DECIMAL(12,2) NOT NULL DEFAULT 0,
    due_date      DATE         NOT NULL,
    status        VARCHAR(10)  NOT NULL DEFAULT 'OPEN',
    CONSTRAINT fk_inv_order    FOREIGN KEY (order_id)    REFERENCES orders(id),
    CONSTRAINT fk_inv_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
) ENGINE=InnoDB;

CREATE TABLE payments (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id    BIGINT       NOT NULL,
    amount        DECIMAL(12,2) NOT NULL,
    method        VARCHAR(20)  NOT NULL,
    paid_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pay_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
) ENGINE=InnoDB;

CREATE INDEX idx_orders_customer ON orders (customer_id);
CREATE INDEX idx_orders_status   ON orders (status);
CREATE INDEX idx_items_order     ON order_items (order_id);
CREATE INDEX idx_invoices_cust   ON invoices (customer_id, status);
