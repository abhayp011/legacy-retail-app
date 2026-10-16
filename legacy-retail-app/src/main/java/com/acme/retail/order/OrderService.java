package com.acme.retail.order;

import com.acme.retail.billing.InvoiceBean;
import com.acme.retail.billing.InvoiceDao;
import com.acme.retail.common.AppConfig;
import com.acme.retail.common.DBUtil;
import com.acme.retail.common.StringUtil;
import com.acme.retail.customer.CustomerService;
import com.acme.retail.customer.CustomerVO;
import com.acme.retail.inventory.InventoryService;
import com.acme.retail.product.ProductDao;
import com.acme.retail.product.ProductVO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private static OrderService instance;

    private CustomerService customerService = CustomerService.getInstance();
    private ProductDao productDao = new ProductDao();
    private OrderDao orderDao = new OrderDao();
    private InvoiceDao invoiceDao = new InvoiceDao();
    private InventoryService inventoryService = new InventoryService();

    public static synchronized OrderService getInstance() {
        if (instance == null) {
            instance = new OrderService();
        }
        return instance;
    }

    /**
     * Places an order. items: each entry is {productId, quantity}.
     *
     * Rules: customer must be ACTIVE; products must exist and be active; discount 5% above 1000 and 10% above
     * 5000 (on the subtotal); tax is tax.rate on the discounted amount; outstanding invoices + this order must
     * stay within the customer's credit limit; stock is reserved in the same transaction as the order insert.
     */
    public OrderVO placeOrder(long customerId, List<long[]> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        CustomerVO customer = customerService.getActive(customerId);

        List<OrderItemVO> lines = new ArrayList<OrderItemVO>();
        double subtotal = 0;
        for (long[] it : items) {
            ProductVO p = productDao.findById(it[0]);
            if (p == null || !p.isActive()) {
                throw new IllegalArgumentException("Invalid product: " + it[0]);
            }
            int qty = (int) it[1];
            if (qty <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            OrderItemVO line = new OrderItemVO();
            line.setProductId(p.getId());
            line.setQty(qty);
            line.setUnitPrice(p.getUnitPrice());
            line.setLineTotal(StringUtil.round2(p.getUnitPrice() * qty));
            subtotal += line.getLineTotal();
            lines.add(line);
        }
        subtotal = StringUtil.round2(subtotal);

        double discountRate = 0;
        if (subtotal > 5000) {
            discountRate = 0.10;
        } else if (subtotal > 1000) {
            discountRate = 0.05;
        }
        double discount = StringUtil.round2(subtotal * discountRate);
        double taxRate = Double.parseDouble(AppConfig.get("tax.rate"));
        double tax = StringUtil.round2((subtotal - discount) * taxRate);
        double total = StringUtil.round2(subtotal - discount + tax);

        double outstanding = invoiceDao.outstandingFor(customerId);
        if (outstanding + total > customer.getCreditLimit()) {
            throw new IllegalStateException("Credit limit exceeded");
        }

        OrderVO order = new OrderVO();
        order.setCustomerId(customerId);
        order.setStatus("NEW");
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setTax(tax);
        order.setTotal(total);

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            order.setOrderNo(OrderNumberGenerator.next());
            long orderId = orderDao.insertOrder(conn, order);
            for (OrderItemVO line : lines) {
                inventoryService.reserve(conn, line.getProductId(), line.getQty());   // fails if stock is short
                line.setOrderId(orderId);
                orderDao.insertItem(conn, line);
            }
            conn.commit();
            order.setId(orderId);
        } catch (SQLException e) {
            DBUtil.rollbackQuietly(conn);
            throw new IllegalStateException("Could not place order: " + e.getMessage(), e);
        } finally {
            DBUtil.close(conn, null, null);
        }
        order.setItems(lines);

        // Separate transaction: an invoice failure does NOT undo the committed order (it is only logged).
        try {
            new InvoiceBean().createFromOrder(order.getId());
        } catch (Exception e) {
            System.err.println("WARN: invoice creation failed for " + order.getOrderNo() + ": " + e.getMessage());
        }
        System.out.println("Order placed: " + order.getOrderNo());
        return order;
    }

    /** Only NEW or CONFIRMED orders can be cancelled. Stock is returned. The invoice is left untouched. */
    public OrderVO cancelOrder(long orderId) {
        OrderVO order = orderDao.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        if (!"NEW".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
            throw new IllegalStateException("Order cannot be cancelled in status " + order.getStatus());
        }
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            for (OrderItemVO item : order.getItems()) {
                inventoryService.release(conn, item.getProductId(), item.getQty());
            }
            orderDao.updateStatus(conn, orderId, "CANCELLED");   // audit row is written by a DB trigger
            conn.commit();
            order.setStatus("CANCELLED");
        } catch (SQLException e) {
            DBUtil.rollbackQuietly(conn);
            throw new IllegalStateException("Could not cancel order: " + e.getMessage(), e);
        } finally {
            DBUtil.close(conn, null, null);
        }
        return order;
    }

    public OrderVO get(long orderId) {
        return orderDao.findById(orderId);
    }

    public List<OrderVO> search(String status, String from, String to, Long customerId) {
        return orderDao.search(status, from, to, customerId);
    }
}
