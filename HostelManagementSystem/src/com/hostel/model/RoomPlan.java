package com.hostel.model;

public class RoomPlan {
    private int planId;
    private String planName, description;
    private double baseCharge, extraCharge;
    private boolean active;

    public RoomPlan() {}

    public int getPlanId() { return planId; }
    public void setPlanId(int p) { this.planId = p; }
    public String getPlanName() { return planName; }
    public void setPlanName(String n) { this.planName = n; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public double getBaseCharge() { return baseCharge; }
    public void setBaseCharge(double b) { this.baseCharge = b; }
    public double getExtraCharge() { return extraCharge; }
    public void setExtraCharge(double e) { this.extraCharge = e; }
    public boolean isActive() { return active; }
    public void setActive(boolean a) { this.active = a; }

    @Override public String toString() { return planName; }
}
