package com.hostel.model;

import java.sql.Date;

public class Girl {
    private int girlId;
    private String name, gender, mobile, email, aadharNumber, collegeAddress;
    private String address, photoPath;
    private int age, roomId, planId, kitchenPlanId;
    private Date dob, admissionDate, leavingDate;
    private String status; // ACTIVE / LEFT
    // transient display helpers
    private String roomNumber, planName, kitchenPlanName;

    public Girl() {}

    // ---------- getters & setters ----------
    public int getGirlId() { return girlId; }
    public void setGirlId(int girlId) { this.girlId = girlId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public Date getDob() { return dob; }
    public void setDob(Date dob) { this.dob = dob; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAadharNumber() { return aadharNumber; }
    public void setAadharNumber(String a) { this.aadharNumber = a; }
    public String getCollegeAddress() { return collegeAddress; }
    public void setCollegeAddress(String c) { this.collegeAddress = c; }
    public String getAddress() { return address; }
    public void setAddress(String a) { this.address = a; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String p) { this.photoPath = p; }
    public int getRoomId() { return roomId; }
    public void setRoomId(int r) { this.roomId = r; }
    public int getPlanId() { return planId; }
    public void setPlanId(int p) { this.planId = p; }
    public int getKitchenPlanId() { return kitchenPlanId; }
    public void setKitchenPlanId(int k) { this.kitchenPlanId = k; }
    public Date getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(Date d) { this.admissionDate = d; }
    public Date getLeavingDate() { return leavingDate; }
    public void setLeavingDate(Date d) { this.leavingDate = d; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String r) { this.roomNumber = r; }
    public String getPlanName() { return planName; }
    public void setPlanName(String p) { this.planName = p; }
    public String getKitchenPlanName() { return kitchenPlanName; }
    public void setKitchenPlanName(String k) { this.kitchenPlanName = k; }

    @Override public String toString() { return name + " (" + aadharNumber + ")"; }
}
