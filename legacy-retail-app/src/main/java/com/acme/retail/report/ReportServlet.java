package com.acme.retail.report;

import com.acme.retail.common.AppConfig;
import com.acme.retail.common.DateUtil;
import com.acme.retail.common.StringUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** GET ?action=summary&from=&to= | ?action=top&limit= | ?action=export&from=&to= (also saves a CSV on disk) */
public class ReportServlet extends HttpServlet {

    private SalesReportDao dao = new SalesReportDao();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("summary".equals(action)) {
            resp.setContentType("application/json");
            List<String[]> rows = dao.summary(req.getParameter("from"), req.getParameter("to"));
            StringBuffer sb = new StringBuffer("[");
            for (int i = 0; i < rows.size(); i++) {
                String[] r = rows.get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"date\":\"").append(r[0]).append("\",\"customer\":\"").append(StringUtil.esc(r[2]))
                        .append("\",\"orders\":").append(r[3]).append(",\"sales\":").append(r[4]).append("}");
            }
            resp.getWriter().print(sb.append("]").toString());
        } else if ("top".equals(action)) {
            resp.setContentType("application/json");
            int limit = StringUtil.isEmpty(req.getParameter("limit")) ? 5 : Integer.parseInt(req.getParameter("limit"));
            List<String[]> rows = dao.topCustomers(limit);
            StringBuffer sb = new StringBuffer("[");
            for (int i = 0; i < rows.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("{\"customer\":\"").append(StringUtil.esc(rows.get(i)[0])).append("\",\"sales\":")
                        .append(rows.get(i)[1]).append("}");
            }
            resp.getWriter().print(sb.append("]").toString());
        } else if ("export".equals(action)) {
            export(req, resp);
        } else {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"unknown action\"}");
        }
    }

    private void export(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<String[]> rows = dao.summary(req.getParameter("from"), req.getParameter("to"));
        File dir = new File(AppConfig.get("report.dir"));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "sales_" + DateUtil.compact() + ".csv");
        FileWriter fw = new FileWriter(file);
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/csv");
        String header = "date,customer_id,customer,orders,sales";
        fw.write(header + "\n");
        out.println(header);
        for (String[] r : rows) {
            String line = r[0] + "," + r[1] + ",\"" + r[2] + "\"," + r[3] + "," + r[4];
            fw.write(line + "\n");
            out.println(line);
        }
        fw.close();
        System.out.println("Report written: " + file.getAbsolutePath());
    }
}
