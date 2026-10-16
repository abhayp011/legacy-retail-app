USE retail;

-- Daily sales per customer, excluding cancelled orders. Joins orders with customers.
CREATE OR REPLACE VIEW v_sales_summary AS
SELECT DATE(o.created_at)  AS sale_date,
       c.id                AS customer_id,
       c.name              AS customer_name,
       COUNT(*)            AS orders_count,
       SUM(o.total)        AS total_sales
  FROM orders o
  JOIN customers c ON c.id = o.customer_id
 WHERE o.status <> 'CANCELLED'
 GROUP BY DATE(o.created_at), c.id, c.name;
