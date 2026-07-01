package com.hostel.model;

public class KitchenPlan {
    private int kitchenPlanId;
    private String planName;
    private double monthlyCharge;
    private boolean refundable, active;

    public KitchenPlan() {}

    public int getKitchenPlanId() { return kitchenPlanId; }
    public void setKitchenPlanId(int k) { this.kitchenPlanId = k; }
    public String getPlanName() { return planName; }
    public void setPlanName(String p) { this.planName = p; }
    public double getMonthlyCharge() { return monthlyCharge; }
    public void setMonthlyCharge(double m) { this.monthlyCharge = m; }
    public boolean isRefundable() { return refundable; }
    public void setRefundable(boolean r) { this.refundable = r; }
    public boolean isActive() { return active; }
    public void setActive(boolean a) { this.active = a; }

    @Override public String toString() { return planName + " (₹" + monthlyCharge + "/mo)"; }
}
