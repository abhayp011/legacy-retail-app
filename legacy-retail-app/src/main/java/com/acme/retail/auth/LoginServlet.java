package com.acme.retail.auth;

import com.acme.retail.common.StringUtil;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginServlet extends HttpServlet {

    private UserDao userDao = new UserDao();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        if (StringUtil.isEmpty(username) || StringUtil.isEmpty(password)) {
            resp.setStatus(400);
            out.print("{\"error\":\"username and password are required\"}");
            return;
        }
        UserVO user = userDao.findByUsername(username);
        if (user == null || !user.isActive() || !user.getPasswordMd5().equals(StringUtil.md5(password))) {
            resp.setStatus(401);
            out.print("{\"error\":\"invalid credentials\"}");
            return;
        }
        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);
        session.setAttribute("role", user.getRole());
        session.setMaxInactiveInterval(30 * 60);
        System.out.println("Login: " + user.getUsername());
        out.print("{\"username\":\"" + StringUtil.esc(user.getUsername()) + "\",\"role\":\"" + user.getRole() + "\"}");
    }
}
