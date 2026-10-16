package com.acme.retail.billing;

import com.acme.retail.common.DateUtil;
import com.acme.retail.common.StringUtil;
import com.acme.retail.order.OrderDao;
import com.acme.retail.order.OrderVO;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/** Billing facade (stateless session bean). Invoices are due 30 days after creation. */
@Stateless
public class InvoiceBean {

    private InvoiceDao invoiceDao = new InvoiceDao();
    private PaymentDao paymentDao = new PaymentDao();
    private OrderDao orderDao = new OrderDao();   // billing reads orders; orders create invoices (circular)

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public InvoiceVO createFromOrder(long orderId) {
        OrderVO order = orderDao.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        InvoiceVO existing = invoiceDao.findByOrder(orderId);
        if (existing != null) {
            return existing;
        }
        InvoiceVO inv = new InvoiceVO();
        inv.setOrderId(orderId);
        inv.setCustomerId(order.getCustomerId());
        inv.setAmount(order.getTotal());
        inv.setDueDate(DateUtil.plusDays(30));
        inv.setStatus("OPEN");
        long id = invoiceDao.insert(inv);
        if (id < 0) {
            throw new IllegalStateException("Could not create invoice for order " + orderId);
        }
        inv.setId(id);
        return inv;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String recordPayment(long invoiceId, double amount, String method) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        InvoiceVO inv = invoiceDao.findById(invoiceId);
        if (inv == null) {
            throw new IllegalArgumentException("Invoice not found: " + invoiceId);
        }
        if ("PAID".equals(inv.getStatus())) {
            throw new IllegalStateException("Invoice already paid");
        }
        return paymentDao.applyPayment(invoiceId, amount, StringUtil.isEmpty(method) ? "CASH" : method);
    }

    public InvoiceVO get(long invoiceId) {
        return invoiceDao.findById(invoiceId);
    }

    public int runOverdueJob() {
        return invoiceDao.markOverdue();
    }
}
