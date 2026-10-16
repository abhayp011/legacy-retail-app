package com.acme.retail.billing;

import java.sql.Date;

public class InvoiceVO {
    private long id;
    private long orderId;
    private long customerId;
    private double amount;
    private double paidAmount;
    private Date dueDate;
    private String status;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public long getCustomerId() { return customerId; }
    public void setCustomerId(long customerId) { this.customerId = customerId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
