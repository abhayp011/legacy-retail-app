package com.acme.retail.auth;

public class UserVO {
    private long id;
    private String username;
    private String passwordMd5;
    private String role;
    private boolean active;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordMd5() { return passwordMd5; }
    public void setPasswordMd5(String passwordMd5) { this.passwordMd5 = passwordMd5; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
