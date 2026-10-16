package com.acme.retail.customer;

import com.acme.retail.common.StringUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** GET ?action=get&id= | ?action=search&name=   POST action=register|suspend|activate */
public class CustomerServlet extends HttpServlet {

    private CustomerService service = CustomerService.getInstance();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String action = req.getParameter("action");
        if ("get".equals(action)) {
            CustomerVO c = service.get(Long.parseLong(req.getParameter("id")));
            if (c == null) {
                resp.setStatus(404);
                out.print("{\"error\":\"not found\"}");
            } else {
                out.print(toJson(c));
            }
        } else if ("search".equals(action)) {
            List<CustomerVO> list = service.search(req.getParameter("name"));
            StringBuffer sb = new StringBuffer("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(list.get(i)));
            }
            out.print(sb.append("]").toString());
        } else {
            resp.setStatus(400);
            out.print("{\"error\":\"unknown action\"}");
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String action = req.getParameter("action");
        try {
            if ("register".equals(action)) {
                double limit = StringUtil.isEmpty(req.getParameter("creditLimit"))
                        ? 0 : Double.parseDouble(req.getParameter("creditLimit"));
                CustomerVO c = service.register(req.getParameter("name"), req.getParameter("email"),
                        req.getParameter("phone"), limit);
                resp.setStatus(201);
                out.print(toJson(c));
            } else if ("suspend".equals(action)) {
                out.print("{\"ok\":" + service.suspend(Long.parseLong(req.getParameter("id"))) + "}");
            } else if ("activate".equals(action)) {
                out.print("{\"ok\":" + service.activate(Long.parseLong(req.getParameter("id"))) + "}");
            } else {
                resp.setStatus(400);
                out.print("{\"error\":\"unknown action\"}");
            }
        } catch (IllegalArgumentException e) {
            resp.setStatus(400);
            out.print("{\"error\":\"" + StringUtil.esc(e.getMessage()) + "\"}");
        } catch (IllegalStateException e) {
            resp.setStatus(409);
            out.print("{\"error\":\"" + StringUtil.esc(e.getMessage()) + "\"}");
        }
    }

    private String toJson(CustomerVO c) {
        return "{\"id\":" + c.getId() + ",\"name\":\"" + StringUtil.esc(c.getName()) + "\",\"email\":\""
                + StringUtil.esc(c.getEmail()) + "\",\"status\":\"" + c.getStatus() + "\",\"creditLimit\":"
                + c.getCreditLimit() + "}";
    }
}
