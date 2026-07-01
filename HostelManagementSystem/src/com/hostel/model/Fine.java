package com.hostel.model;

import java.sql.Date;

public class Fine {
    private int fineId, girlId, imposedBy;
    private String reason, status, girlName;
    private double amount;
    private Date fineDate;

    public Fine() {}

    public int getFineId() { return fineId; }
    public void setFineId(int f) { this.fineId = f; }
    public int getGirlId() { return girlId; }
    public void setGirlId(int g) { this.girlId = g; }
    public int getImposedBy() { return imposedBy; }
    public void setImposedBy(int i) { this.imposedBy = i; }
    public String getReason() { return reason; }
    public void setReason(String r) { this.reason = r; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public String getGirlName() { return girlName; }
    public void setGirlName(String n) { this.girlName = n; }
    public double getAmount() { return amount; }
    public void setAmount(double a) { this.amount = a; }
    public Date getFineDate() { return fineDate; }
    public void setFineDate(Date d) { this.fineDate = d; }
}
