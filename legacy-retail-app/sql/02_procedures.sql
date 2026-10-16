USE retail;
DELIMITER //

-- Stock movement. delta < 0 reserves stock, delta > 0 returns it. Refuses to go below zero.
CREATE PROCEDURE adjust_stock(IN p_product_id BIGINT, IN p_delta INT)
BEGIN
    DECLARE v_qty INT;

    SELECT stock_qty INTO v_qty FROM inventory WHERE product_id = p_product_id FOR UPDATE;

    IF v_qty IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Unknown product in inventory';
    END IF;
    IF v_qty + p_delta < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Insufficient stock';
    END IF;

    UPDATE inventory SET stock_qty = stock_qty + p_delta WHERE product_id = p_product_id;
END//

-- Payment posting. Runs in its own transaction and COMMITs itself.
CREATE PROCEDURE apply_payment(IN p_invoice_id BIGINT, IN p_amount DECIMAL(12,2),
                               IN p_method VARCHAR(20), OUT p_status VARCHAR(10))
BEGIN
    DECLARE v_amount DECIMAL(12,2);
    DECLARE v_paid   DECIMAL(12,2);

    START TRANSACTION;

    SELECT amount, paid_amount INTO v_amount, v_paid FROM invoices WHERE id = p_invoice_id FOR UPDATE;

    IF v_amount IS NULL THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invoice not found';
    END IF;
    IF v_paid + p_amount > v_amount THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Payment exceeds invoice balance';
    END IF;

    INSERT INTO payments (invoice_id, amount, method) VALUES (p_invoice_id, p_amount, p_method);

    -- status is assigned first so it is evaluated against the paid amount from before this payment
    UPDATE invoices
       SET status = IF(paid_amount + p_amount >= amount, 'PAID', status),
           paid_amount = paid_amount + p_amount
     WHERE id = p_invoice_id;

    SELECT status INTO p_status FROM invoices WHERE id = p_invoice_id;
    COMMIT;
END//

-- Nightly job: OPEN invoices past their due date become OVERDUE.
CREATE PROCEDURE mark_overdue_invoices(OUT p_count INT)
BEGIN
    UPDATE invoices SET status = 'OVERDUE' WHERE status = 'OPEN' AND due_date < CURDATE();
    SET p_count = ROW_COUNT();
END//

-- Same calculation the Java code does in InvoiceDao.outstandingFor (duplicated logic).
CREATE FUNCTION fn_customer_outstanding(p_customer_id BIGINT) RETURNS DECIMAL(12,2)
READS SQL DATA
BEGIN
    DECLARE v_total DECIMAL(12,2);
    SELECT COALESCE(SUM(amount - paid_amount), 0) INTO v_total
      FROM invoices
     WHERE customer_id = p_customer_id AND status IN ('OPEN', 'OVERDUE');
    RETURN v_total;
END//

DELIMITER ;
