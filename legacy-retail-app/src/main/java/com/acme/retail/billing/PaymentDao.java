package com.acme.retail.billing;

import com.acme.retail.common.DBUtil;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class PaymentDao {

    /**
     * Records a payment via the apply_payment stored procedure, which locks the invoice row, rejects
     * overpayment, inserts the payment, updates the invoice and COMMITs itself. Returns the new invoice status.
     */
    public String applyPayment(long invoiceId, double amount, String method) {
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = DBUtil.getConnection();
            cs = conn.prepareCall("{call apply_payment(?, ?, ?, ?)}");
            cs.setLong(1, invoiceId);
            cs.setDouble(2, amount);
            cs.setString(3, method);
            cs.registerOutParameter(4, Types.VARCHAR);
            cs.execute();
            return cs.getString(4);
        } catch (SQLException e) {
            throw new IllegalStateException("Payment failed: " + e.getMessage(), e);
        } finally {
            DBUtil.close(conn, cs, null);
        }
    }
}
