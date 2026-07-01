package com.hostel.model;

import java.sql.Date;

public class Bill {
    private int billId, girlId, generatedBy;
    private String billType, status, remarks, girlName;
    private Integer billMonth, billYear;
    private double amount;
    private Date dueDate, generatedDate;

    public Bill() {}

    public int getBillId() { return billId; }
    public void setBillId(int b) { this.billId = b; }
    public int getGirlId() { return girlId; }
    public void setGirlId(int g) { this.girlId = g; }
    public String getGirlName() { return girlName; }
    public void setGirlName(String n) { this.girlName = n; }
    public String getBillType() { return billType; }
    public void setBillType(String t) { this.billType = t; }
    public Integer getBillMonth() { return billMonth; }
    public void setBillMonth(Integer m) { this.billMonth = m; }
    public Integer getBillYear() { return billYear; }
    public void setBillYear(Integer y) { this.billYear = y; }
    public double getAmount() { return amount; }
    public void setAmount(double a) { this.amount = a; }
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date d) { this.dueDate = d; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public Date getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(Date d) { this.generatedDate = d; }
    public int getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(int g) { this.generatedBy = g; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String r) { this.remarks = r; }
}
