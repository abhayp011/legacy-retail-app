package com.acme.retail.product;

import com.acme.retail.common.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    public ProductVO findById(long id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(
                    "SELECT id, sku, name, category, unit_price, active FROM products WHERE id = ?");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }

    public ProductVO findBySku(String sku) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(
                    "SELECT id, sku, name, category, unit_price, active FROM products WHERE sku = ?");
            ps.setString(1, sku);
            rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }

    public List<ProductVO> listByCategory(String category) {
        List<ProductVO> result = new ArrayList<ProductVO>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            st = conn.createStatement();
            String sql = "SELECT id, sku, name, category, unit_price, active FROM products WHERE active = 1";
            if (category != null && category.length() > 0) {
                sql = sql + " AND category = '" + category + "'";
            }
            rs = st.executeQuery(sql + " ORDER BY name");
            while (rs.next()) {
                result.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, st, rs);
        }
        return result;
    }

    /** Price changes are recorded in product_price_history by a database trigger. */
    public boolean updatePrice(long id, double newPrice) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement("UPDATE products SET unit_price = ? WHERE id = ?");
            ps.setDouble(1, newPrice);
            ps.setLong(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, ps, null);
        }
    }

    private ProductVO map(ResultSet rs) throws SQLException {
        ProductVO p = new ProductVO();
        p.setId(rs.getLong("id"));
        p.setSku(rs.getString("sku"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));
        p.setUnitPrice(rs.getDouble("unit_price"));
        p.setActive(rs.getInt("active") == 1);
        return p;
    }
}
