package com.hostel.model;

import java.sql.Date;

public class Payment {
    private int paymentId, billId, girlId, receivedBy;
    private double amountPaid;
    private Date paymentDate;
    private String paymentMode, receiptNo, girlName, billType;

    public Payment() {}

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int p) { this.paymentId = p; }
    public int getBillId() { return billId; }
    public void setBillId(int b) { this.billId = b; }
    public int getGirlId() { return girlId; }
    public void setGirlId(int g) { this.girlId = g; }
    public int getReceivedBy() { return receivedBy; }
    public void setReceivedBy(int r) { this.receivedBy = r; }
    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double a) { this.amountPaid = a; }
    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date d) { this.paymentDate = d; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String m) { this.paymentMode = m; }
    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String r) { this.receiptNo = r; }
    public String getGirlName() { return girlName; }
    public void setGirlName(String n) { this.girlName = n; }
    public String getBillType() { return billType; }
    public void setBillType(String t) { this.billType = t; }
}
