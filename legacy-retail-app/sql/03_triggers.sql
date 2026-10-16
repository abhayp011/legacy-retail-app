USE retail;
DELIMITER //

-- Every status change on an order is written to order_audit.
CREATE TRIGGER trg_orders_status_audit AFTER UPDATE ON orders
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO order_audit (order_id, old_status, new_status)
        VALUES (OLD.id, OLD.status, NEW.status);
    END IF;
END//

-- Every price change is written to product_price_history.
CREATE TRIGGER trg_products_price_history BEFORE UPDATE ON products
FOR EACH ROW
BEGIN
    IF OLD.unit_price <> NEW.unit_price THEN
        INSERT INTO product_price_history (product_id, old_price, new_price)
        VALUES (OLD.id, OLD.unit_price, NEW.unit_price);
    END IF;
END//

-- Integrity guard.
CREATE TRIGGER trg_orders_total_check BEFORE INSERT ON orders
FOR EACH ROW
BEGIN
    IF NEW.total < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Negative order total';
    END IF;
END//

DELIMITER ;
