USE retail;

-- Passwords: admin / admin123, clerk / clerk123 (stored as MD5)
INSERT INTO users (username, password_md5, role) VALUES
  ('admin', MD5('admin123'), 'ADMIN'),
  ('clerk', MD5('clerk123'), 'USER');

INSERT INTO customers (name, email, phone, status, credit_limit) VALUES
  ('Asha Traders',    'asha@traders.example',   '+91-98200-11111', 'ACTIVE',    50000.00),
  ('Bharat Stores',   'accounts@bharat.example','+91-98200-22222', 'ACTIVE',     5000.00),
  ('Chandra Retail',  'info@chandra.example',   '+91-98200-33333', 'SUSPENDED', 20000.00);

INSERT INTO products (sku, name, category, unit_price, active) VALUES
  ('KB-100', 'Mechanical Keyboard', 'ACCESSORIES',  600.00, 1),
  ('MS-200', 'Wireless Mouse',      'ACCESSORIES',  250.00, 1),
  ('MN-300', '24in Monitor',        'DISPLAYS',    9000.00, 1),
  ('LP-400', 'Laptop Stand',        'ACCESSORIES',  800.00, 1),
  ('CB-500', 'HDMI Cable',          'CABLES',       150.00, 1),
  ('OLD-900','Discontinued Webcam', 'ACCESSORIES',  500.00, 0);

INSERT INTO inventory (product_id, stock_qty, reorder_level) VALUES
  (1, 100, 20), (2, 200, 40), (3, 10, 5), (4, 50, 10), (5, 500, 100), (6, 0, 0);
