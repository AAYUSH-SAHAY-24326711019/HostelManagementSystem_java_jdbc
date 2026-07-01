package com.hostel.model;

import java.sql.Timestamp;

public class Admin {
    private int adminId;
    private String username, passwordHash, salt, fullName, email, mobile;
    private boolean active;
    private Timestamp lastLogin;

    public Admin() {}

    public int getAdminId() { return adminId; }
    public void setAdminId(int a) { this.adminId = a; }
    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String p) { this.passwordHash = p; }
    public String getSalt() { return salt; }
    public void setSalt(String s) { this.salt = s; }
    public String getFullName() { return fullName; }
    public void setFullName(String f) { this.fullName = f; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public String getMobile() { return mobile; }
    public void setMobile(String m) { this.mobile = m; }
    public boolean isActive() { return active; }
    public void setActive(boolean a) { this.active = a; }
    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp t) { this.lastLogin = t; }
}
