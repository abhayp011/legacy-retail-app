package com.acme.retail.product;

public class ProductVO {
    private long id;
    private String sku;
    private String name;
    private String category;
    private double unitPrice;
    private boolean active;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
