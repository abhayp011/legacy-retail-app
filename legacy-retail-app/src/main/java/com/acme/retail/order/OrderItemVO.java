package com.acme.retail.order;

public class OrderItemVO {
    private long id;
    private long orderId;
    private long productId;
    private int qty;
    private double unitPrice;
    private double lineTotal;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public long getProductId() { return productId; }
    public void setProductId(long productId) { this.productId = productId; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public double getLineTotal() { return lineTotal; }
    public void setLineTotal(double lineTotal) { this.lineTotal = lineTotal; }
}
