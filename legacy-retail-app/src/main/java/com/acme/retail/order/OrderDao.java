package com.acme.retail.order;

import com.acme.retail.common.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    /** Insert on the caller's connection (caller owns the transaction). Returns the generated id. */
    public long insertOrder(Connection conn, OrderVO o) throws SQLException {
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            ps = conn.prepareStatement(
                    "INSERT INTO orders (order_no, customer_id, status, subtotal, discount, tax, total) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, o.getOrderNo());
            ps.setLong(2, o.getCustomerId());
            ps.setString(3, o.getStatus());
            ps.setDouble(4, o.getSubtotal());
            ps.setDouble(5, o.getDiscount());
            ps.setDouble(6, o.getTax());
            ps.setDouble(7, o.getTotal());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            if (!keys.next()) {
                throw new SQLException("No generated key for order");
            }
            return keys.getLong(1);
        } finally {
            DBUtil.close(null, ps, keys);
        }
    }

    public void insertItem(Connection conn, OrderItemVO item) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(
                    "INSERT INTO order_items (order_id, product_id, qty, unit_price, line_total) VALUES (?, ?, ?, ?, ?)");
            ps.setLong(1, item.getOrderId());
            ps.setLong(2, item.getProductId());
            ps.setInt(3, item.getQty());
            ps.setDouble(4, item.getUnitPrice());
            ps.setDouble(5, item.getLineTotal());
            ps.executeUpdate();
        } finally {
            DBUtil.close(null, ps, null);
        }
    }

    public void updateStatus(Connection conn, long orderId, String status) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("UPDATE orders SET status = ? WHERE id = ?");
            ps.setString(1, status);
            ps.setLong(2, orderId);
            ps.executeUpdate();
        } finally {
            DBUtil.close(null, ps, null);
        }
    }

    /** Loads the order together with its items. */
    public OrderVO findById(long id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(
                    "SELECT id, order_no, customer_id, status, subtotal, discount, tax, total, created_at "
                            + "FROM orders WHERE id = ?");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            OrderVO o = mapOrder(rs);
            o.setItems(loadItems(conn, id));
            return o;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }

    public List<OrderVO> findByCustomer(long customerId) {
        List<OrderVO> result = new ArrayList<OrderVO>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(
                    "SELECT id, order_no, customer_id, status, subtotal, discount, tax, total, created_at "
                            + "FROM orders WHERE customer_id = ? ORDER BY created_at DESC");
            ps.setLong(1, customerId);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return result;
    }

    /** Search by any combination of filters (all optional). Built by string concatenation. */
    public List<OrderVO> search(String status, String fromDate, String toDate, Long customerId) {
        List<OrderVO> result = new ArrayList<OrderVO>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            st = conn.createStatement();
            StringBuffer sql = new StringBuffer(
                    "SELECT id, order_no, customer_id, status, subtotal, discount, tax, total, created_at "
                            + "FROM orders WHERE 1=1");
            if (status != null && status.length() > 0) {
                sql.append(" AND status = '" + status + "'");
            }
            if (fromDate != null && fromDate.length() > 0) {
                sql.append(" AND created_at >= '" + fromDate + " 00:00:00'");
            }
            if (toDate != null && toDate.length() > 0) {
                sql.append(" AND created_at <= '" + toDate + " 23:59:59'");
            }
            if (customerId != null) {
                sql.append(" AND customer_id = " + customerId);
            }
            sql.append(" ORDER BY created_at DESC");
            rs = st.executeQuery(sql.toString());
            while (rs.next()) {
                result.add(mapOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, st, rs);
        }
        return result;
    }

    private List<OrderItemVO> loadItems(Connection conn, long orderId) throws SQLException {
        List<OrderItemVO> items = new ArrayList<OrderItemVO>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(
                    "SELECT id, order_id, product_id, qty, unit_price, line_total FROM order_items WHERE order_id = ?");
            ps.setLong(1, orderId);
            rs = ps.executeQuery();
            while (rs.next()) {
                OrderItemVO i = new OrderItemVO();
                i.setId(rs.getLong("id"));
                i.setOrderId(rs.getLong("order_id"));
                i.setProductId(rs.getLong("product_id"));
                i.setQty(rs.getInt("qty"));
                i.setUnitPrice(rs.getDouble("unit_price"));
                i.setLineTotal(rs.getDouble("line_total"));
                items.add(i);
            }
        } finally {
            DBUtil.close(null, ps, rs);
        }
        return items;
    }

    private OrderVO mapOrder(ResultSet rs) throws SQLException {
        OrderVO o = new OrderVO();
        o.setId(rs.getLong("id"));
        o.setOrderNo(rs.getString("order_no"));
        o.setCustomerId(rs.getLong("customer_id"));
        o.setStatus(rs.getString("status"));
        o.setSubtotal(rs.getDouble("subtotal"));
        o.setDiscount(rs.getDouble("discount"));
        o.setTax(rs.getDouble("tax"));
        o.setTotal(rs.getDouble("total"));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        return o;
    }
}
