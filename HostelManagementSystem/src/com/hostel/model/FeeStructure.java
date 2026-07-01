package com.hostel.model;

import java.sql.Date;

public class FeeStructure {
    private int feeId, girlId;
    private double monthlyStayBill, emergencyDeposit, electricityDeposit, wifiDeposit, planExtraCharge, kitchenCharge;
    private Date effectiveDate;

    public FeeStructure() {}

    public int getFeeId() { return feeId; }
    public void setFeeId(int f) { this.feeId = f; }
    public int getGirlId() { return girlId; }
    public void setGirlId(int g) { this.girlId = g; }
    public double getMonthlyStayBill() { return monthlyStayBill; }
    public void setMonthlyStayBill(double m) { this.monthlyStayBill = m; }
    public double getEmergencyDeposit() { return emergencyDeposit; }
    public void setEmergencyDeposit(double e) { this.emergencyDeposit = e; }
    public double getElectricityDeposit() { return electricityDeposit; }
    public void setElectricityDeposit(double e) { this.electricityDeposit = e; }
    public double getWifiDeposit() { return wifiDeposit; }
    public void setWifiDeposit(double w) { this.wifiDeposit = w; }
    public double getPlanExtraCharge() { return planExtraCharge; }
    public void setPlanExtraCharge(double p) { this.planExtraCharge = p; }
    public double getKitchenCharge() { return kitchenCharge; }
    public void setKitchenCharge(double k) { this.kitchenCharge = k; }
    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date d) { this.effectiveDate = d; }
}
