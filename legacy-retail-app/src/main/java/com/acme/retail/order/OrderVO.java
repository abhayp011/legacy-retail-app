package com.acme.retail.order;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OrderVO {
    private long id;
    private String orderNo;
    private long customerId;
    private String status;
    private double subtotal;
    private double discount;
    private double tax;
    private double total;
    private Timestamp createdAt;
    private List<OrderItemVO> items = new ArrayList<OrderItemVO>();

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public long getCustomerId() { return customerId; }
    public void setCustomerId(long customerId) { this.customerId = customerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public List<OrderItemVO> getItems() { return items; }
    public void setItems(List<OrderItemVO> items) { this.items = items; }
}
