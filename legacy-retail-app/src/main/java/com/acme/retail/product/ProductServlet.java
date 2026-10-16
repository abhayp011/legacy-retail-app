package com.acme.retail.product;

import com.acme.retail.common.StringUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** GET ?action=get&id= | ?action=list&category=   POST action=updatePrice (ADMIN only) */
public class ProductServlet extends HttpServlet {

    private ProductDao dao = new ProductDao();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String action = req.getParameter("action");
        if ("get".equals(action)) {
            ProductVO p = dao.findById(Long.parseLong(req.getParameter("id")));
            if (p == null) {
                resp.setStatus(404);
                out.print("{\"error\":\"not found\"}");
            } else {
                out.print(toJson(p));
            }
        } else if ("list".equals(action)) {
            List<ProductVO> list = dao.listByCategory(req.getParameter("category"));
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
        if (!"ADMIN".equals(req.getSession().getAttribute("role"))) {
            resp.setStatus(403);
            out.print("{\"error\":\"admin only\"}");
            return;
        }
        if ("updatePrice".equals(req.getParameter("action"))) {
            double price = Double.parseDouble(req.getParameter("price"));
            if (price <= 0) {
                resp.setStatus(400);
                out.print("{\"error\":\"price must be positive\"}");
                return;
            }
            out.print("{\"ok\":" + dao.updatePrice(Long.parseLong(req.getParameter("id")), price) + "}");
        } else {
            resp.setStatus(400);
            out.print("{\"error\":\"unknown action\"}");
        }
    }

    private String toJson(ProductVO p) {
        return "{\"id\":" + p.getId() + ",\"sku\":\"" + StringUtil.esc(p.getSku()) + "\",\"name\":\""
                + StringUtil.esc(p.getName()) + "\",\"category\":\"" + StringUtil.esc(p.getCategory())
                + "\",\"unitPrice\":" + p.getUnitPrice() + "}";
    }
}
