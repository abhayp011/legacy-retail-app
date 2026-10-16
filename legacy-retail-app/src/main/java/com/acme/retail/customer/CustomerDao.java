package com.acme.retail.customer;

import com.acme.retail.common.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    public CustomerVO findById(long id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(
                    "SELECT id, name, email, phone, status, credit_limit, created_at FROM customers WHERE id = ?");
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

    public CustomerVO findByEmail(String email) {
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery(
                    "SELECT id, name, email, phone, status, credit_limit, created_at FROM customers WHERE email = '"
                            + email + "'");
            return rs.next() ? map(rs) : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBUtil.close(conn, st, rs);
        }
    }

    public List<CustomerVO> searchByName(String name) {
        List<CustomerVO> result = new ArrayList<CustomerVO>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery(
                    "SELECT id, name, email, phone, status, credit_limit, created_at FROM customers WHERE name LIKE '%"
                            + name + "%' ORDER BY name");
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

    /** Returns the generated id, or -1 on failure. */
    public long insert(CustomerVO c) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(
                    "INSERT INTO customers (name, email, phone, status, credit_limit) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, c.getName());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getStatus());
            ps.setDouble(5, c.getCreditLimit());
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

    public boolean updateStatus(long id, String status) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement("UPDATE customers SET status = ? WHERE id = ?");
            ps.setString(1, status);
            ps.setLong(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, ps, null);
        }
    }

    private CustomerVO map(ResultSet rs) throws SQLException {
        CustomerVO c = new CustomerVO();
        c.setId(rs.getLong("id"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setStatus(rs.getString("status"));
        c.setCreditLimit(rs.getDouble("credit_limit"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        return c;
    }
}
