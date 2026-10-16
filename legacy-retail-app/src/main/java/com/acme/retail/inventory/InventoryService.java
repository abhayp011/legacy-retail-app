package com.acme.retail.inventory;

import java.sql.Connection;
import java.sql.SQLException;

public class InventoryService {

    private InventoryDao dao = new InventoryDao();

    public int available(long productId) {
        return dao.getStock(productId);
    }

    /** Reserve stock inside the caller's transaction. */
    public void reserve(Connection conn, long productId, int qty) throws SQLException {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        dao.adjustStock(conn, productId, -qty);
    }

    /** Put stock back (order cancellation), inside the caller's transaction. */
    public void release(Connection conn, long productId, int qty) throws SQLException {
        dao.adjustStock(conn, productId, qty);
    }
}
