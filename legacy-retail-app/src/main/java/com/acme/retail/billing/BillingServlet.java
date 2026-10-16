package com.acme.retail.billing;

import com.acme.retail.common.StringUtil;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** GET ?action=get&id=   POST action=pay&id=&amount=&method= | action=runOverdue (ADMIN only) */
public class BillingServlet extends HttpServlet {

    private InvoiceBean bean = new InvoiceBean();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        InvoiceVO inv = bean.get(Long.parseLong(req.getParameter("id")));
        if (inv == null) {
            resp.setStatus(404);
            out.print("{\"error\":\"not found\"}");
            return;
        }
        out.print("{\"id\":" + inv.getId() + ",\"orderId\":" + inv.getOrderId() + ",\"amount\":" + inv.getAmount()
                + ",\"paid\":" + inv.getPaidAmount() + ",\"dueDate\":\"" + inv.getDueDate() + "\",\"status\":\""
                + inv.getStatus() + "\"}");
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String action = req.getParameter("action");
        try {
            if ("pay".equals(action)) {
                String status = bean.recordPayment(Long.parseLong(req.getParameter("id")),
                        Double.parseDouble(req.getParameter("amount")), req.getParameter("method"));
                out.print("{\"status\":\"" + status + "\"}");
            } else if ("runOverdue".equals(action)) {
                if (!"ADMIN".equals(req.getSession().getAttribute("role"))) {
                    resp.setStatus(403);
                    out.print("{\"error\":\"admin only\"}");
                    return;
                }
                out.print("{\"markedOverdue\":" + bean.runOverdueJob() + "}");
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
}
