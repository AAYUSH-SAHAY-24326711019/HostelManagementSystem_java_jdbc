package com.hostel.model;

public class Parent {
    private int parentId, girlId;
    private String relationType; // FATHER / MOTHER / GUARDIAN
    private String name, mobile, email, aadharNumber, occupation, address;

    public Parent() {}

    public int getParentId() { return parentId; }
    public void setParentId(int p) { this.parentId = p; }
    public int getGirlId() { return girlId; }
    public void setGirlId(int g) { this.girlId = g; }
    public String getRelationType() { return relationType; }
    public void setRelationType(String r) { this.relationType = r; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public String getMobile() { return mobile; }
    public void setMobile(String m) { this.mobile = m; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public String getAadharNumber() { return aadharNumber; }
    public void setAadharNumber(String a) { this.aadharNumber = a; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String o) { this.occupation = o; }
    public String getAddress() { return address; }
    public void setAddress(String a) { this.address = a; }

    @Override public String toString() { return relationType + ": " + name; }
}
