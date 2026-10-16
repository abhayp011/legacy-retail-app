package com.acme.retail.billing;

import com.acme.retail.common.DBUtil;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class InvoiceDao {

    public long insert(InvoiceVO inv) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(
                    "INSERT INTO invoices (order_id, customer_id, amount, paid_amount, due_date, status) "
                            + "VALUES (?, ?, ?, 0, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, inv.getOrderId());
            ps.setLong(2, inv.getCustomerId());
            ps.setDouble(3, inv.getAmount());
            ps.setDate(4, inv.getDueDate());
            ps.setString(5, inv.getStatus());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getLong(1) : -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            DBUtil.close(conn, ps, keys);
        }
    }

    public InvoiceVO findById(long id) {
        return findOne("SELECT id, order_id, customer_id, amount, paid_amount, due_date, status FROM invoices WHERE id = ?", id);
    }

    public InvoiceVO findByOrder(long orderId) {
        return findOne("SELECT id, order_id, customer_id, amount, paid_amount, due_date, status FROM invoices WHERE order_id = ?", orderId);
    }

    /** Unpaid balance of OPEN and OVERDUE invoices (same logic also exists in the fn_customer_outstanding SQL function). */
    public double outstandingFor(long customerId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(amount - paid_amount), 0) FROM invoices "
                            + "WHERE customer_id = ? AND status IN ('OPEN', 'OVERDUE')");
            ps.setLong(1, customerId);
            rs = ps.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }

    /** Runs the mark_overdue_invoices stored procedure; returns how many invoices changed. */
    public int markOverdue() {
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = DBUtil.getConnection();
            cs = conn.prepareCall("{call mark_overdue_invoices(?)}");
            cs.registerOutParameter(1, Types.INTEGER);
            cs.execute();
            return cs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            DBUtil.close(conn, cs, null);
        }
    }

    private InvoiceVO findOne(String sql, long param) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, param);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            InvoiceVO i = new InvoiceVO();
            i.setId(rs.getLong("id"));
            i.setOrderId(rs.getLong("order_id"));
            i.setCustomerId(rs.getLong("customer_id"));
            i.setAmount(rs.getDouble("amount"));
            i.setPaidAmount(rs.getDouble("paid_amount"));
            i.setDueDate(rs.getDate("due_date"));
            i.setStatus(rs.getString("status"));
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
}
