package com.acme.retail.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/** Connection helper used by every DAO. Prefers the container DataSource, falls back to DriverManager. */
public class DBUtil {

    private static DBUtil instance;

    private DBUtil() {
    }

    public static DBUtil getInstance() {
        if (instance == null) {
            instance = new DBUtil();
        }
        return instance;
    }

    public static Connection getConnection() throws SQLException {
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/RetailDS");
            return ds.getConnection();
        } catch (NamingException e) {
            // dev mode: no container datasource, connect directly
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException cnf) {
                throw new SQLException("MySQL driver not found");
            }
            return DriverManager.getConnection(
                    AppConfig.get("db.url"), AppConfig.get("db.user"), AppConfig.get("db.password"));
        }
    }

    public static void close(Connection conn, Statement st, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (st != null) st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void rollbackQuietly(Connection conn) {
        try {
            if (conn != null) conn.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
