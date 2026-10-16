package com.acme.retail.order;

import com.acme.retail.common.StringUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * GET  ?action=get&id= | ?action=search&status=&from=&to=&customerId=
 * POST action=place&customerId=&items=productId:qty,productId:qty | action=cancel&id=
 */
public class OrderServlet extends HttpServlet {

    private OrderService service = OrderService.getInstance();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String action = req.getParameter("action");
        if ("get".equals(action)) {
            OrderVO o = service.get(Long.parseLong(req.getParameter("id")));
            if (o == null) {
                resp.setStatus(404);
                out.print("{\"error\":\"not found\"}");
            } else {
                out.print(toJson(o, true));
            }
        } else if ("search".equals(action)) {
            Long customerId = StringUtil.isEmpty(req.getParameter("customerId"))
                    ? null : Long.valueOf(req.getParameter("customerId"));
            List<OrderVO> list = service.search(req.getParameter("status"), req.getParameter("from"),
                    req.getParameter("to"), customerId);
            StringBuffer sb = new StringBuffer("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(list.get(i), false));
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
            if ("place".equals(action)) {
                List<long[]> items = new ArrayList<long[]>();
                String raw = req.getParameter("items");
                if (raw != null) {
                    for (String part : raw.split(",")) {
                        String[] kv = part.trim().split(":");
                        items.add(new long[] {Long.parseLong(kv[0]), Long.parseLong(kv[1])});
                    }
                }
                OrderVO o = service.placeOrder(Long.parseLong(req.getParameter("customerId")), items);
                resp.setStatus(201);
                out.print(toJson(o, true));
            } else if ("cancel".equals(action)) {
                out.print(toJson(service.cancelOrder(Long.parseLong(req.getParameter("id"))), false));
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

    private String toJson(OrderVO o, boolean withItems) {
        StringBuffer sb = new StringBuffer();
        sb.append("{\"id\":").append(o.getId()).append(",\"orderNo\":\"").append(o.getOrderNo())
                .append("\",\"customerId\":").append(o.getCustomerId()).append(",\"status\":\"")
                .append(o.getStatus()).append("\",\"subtotal\":").append(o.getSubtotal())
                .append(",\"discount\":").append(o.getDiscount()).append(",\"tax\":").append(o.getTax())
                .append(",\"total\":").append(o.getTotal());
        if (withItems) {
            sb.append(",\"items\":[");
            for (int i = 0; i < o.getItems().size(); i++) {
                OrderItemVO it = o.getItems().get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"productId\":").append(it.getProductId()).append(",\"qty\":").append(it.getQty())
                        .append(",\"unitPrice\":").append(it.getUnitPrice()).append(",\"lineTotal\":")
                        .append(it.getLineTotal()).append("}");
            }
            sb.append("]");
        }
        return sb.append("}").toString();
    }
}
