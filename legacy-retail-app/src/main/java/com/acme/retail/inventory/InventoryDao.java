package com.acme.retail.inventory;

import com.acme.retail.common.DBUtil;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryDao {

    public int getStock(long productId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement("SELECT stock_qty FROM inventory WHERE product_id = ?");
            ps.setLong(1, productId);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }

    /**
     * Adjusts stock through the adjust_stock stored procedure, on the CALLER's connection so the change
     * takes part in the caller's transaction. The procedure raises an error if stock would go negative.
     */
    public void adjustStock(Connection conn, long productId, int delta) throws SQLException {
        CallableStatement cs = null;
        try {
            cs = conn.prepareCall("{call adjust_stock(?, ?)}");
            cs.setLong(1, productId);
            cs.setInt(2, delta);
            cs.execute();
        } finally {
            if (cs != null) cs.close();
        }
    }
}
