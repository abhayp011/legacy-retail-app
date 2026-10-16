package com.acme.retail.report;

import com.acme.retail.common.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Reporting reads across orders and customers through the v_sales_summary view. */
public class SalesReportDao {

    /** Rows: {saleDate, customerId, customerName, ordersCount, totalSales}. */
    public List<String[]> summary(String from, String to) {
        List<String[]> rows = new ArrayList<String[]>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(
                    "SELECT sale_date, customer_id, customer_name, orders_count, total_sales "
                            + "FROM v_sales_summary WHERE sale_date BETWEEN ? AND ? ORDER BY sale_date, customer_name");
            ps.setString(1, from);
            ps.setString(2, to);
            rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[] {rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5)});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return rows;
    }

    /** Rows: {customerName, totalSales}. */
    public List<String[]> topCustomers(int limit) {
        List<String[]> rows = new ArrayList<String[]>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery("SELECT customer_name, SUM(total_sales) AS t FROM v_sales_summary "
                    + "GROUP BY customer_name ORDER BY t DESC LIMIT " + limit);
            while (rs.next()) {
                rows.add(new String[] {rs.getString(1), rs.getString(2)});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, st, rs);
        }
        return rows;
    }
}
