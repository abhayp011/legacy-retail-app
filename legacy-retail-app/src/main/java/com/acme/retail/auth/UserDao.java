package com.acme.retail.auth;

import com.acme.retail.common.DBUtil;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDao {

    public UserVO findByUsername(String username) {
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery("SELECT id, username, password_md5, role, active FROM users WHERE username = '"
                    + username + "'");
            if (rs.next()) {
                UserVO u = new UserVO();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordMd5(rs.getString("password_md5"));
                u.setRole(rs.getString("role"));
                u.setActive(rs.getInt("active") == 1);
                return u;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBUtil.close(conn, st, rs);
        }
    }
}
