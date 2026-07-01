package com.hostel.model;

import java.sql.Date;

public class Due {
    private int dueId, girlId, billId;
    private double amountDue;
    private Date dueDate, clearedDate;
    private String status, girlName, billType;

    public Due() {}

    public int getDueId() { return dueId; }
    public void setDueId(int d) { this.dueId = d; }
    public int getGirlId() { return girlId; }
    public void setGirlId(int g) { this.girlId = g; }
    public int getBillId() { return billId; }
    public void setBillId(int b) { this.billId = b; }
    public double getAmountDue() { return amountDue; }
    public void setAmountDue(double a) { this.amountDue = a; }
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date d) { this.dueDate = d; }
    public Date getClearedDate() { return clearedDate; }
    public void setClearedDate(Date d) { this.clearedDate = d; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public String getGirlName() { return girlName; }
    public void setGirlName(String n) { this.girlName = n; }
    public String getBillType() { return billType; }
    public void setBillType(String t) { this.billType = t; }
}
