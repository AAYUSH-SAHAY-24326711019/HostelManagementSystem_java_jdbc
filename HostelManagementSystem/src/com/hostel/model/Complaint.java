package com.hostel.model;

import java.sql.Timestamp;

public class Complaint {
    private int complaintId, girlId;
    private String subject, description, status, adminRemarks, girlName;
    private Timestamp filedDate, resolvedDate;

    public Complaint() {}

    public int getComplaintId() { return complaintId; }
    public void setComplaintId(int c) { this.complaintId = c; }
    public int getGirlId() { return girlId; }
    public void setGirlId(int g) { this.girlId = g; }
    public String getSubject() { return subject; }
    public void setSubject(String s) { this.subject = s; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public String getAdminRemarks() { return adminRemarks; }
    public void setAdminRemarks(String a) { this.adminRemarks = a; }
    public String getGirlName() { return girlName; }
    public void setGirlName(String n) { this.girlName = n; }
    public Timestamp getFiledDate() { return filedDate; }
    public void setFiledDate(Timestamp t) { this.filedDate = t; }
    public Timestamp getResolvedDate() { return resolvedDate; }
    public void setResolvedDate(Timestamp t) { this.resolvedDate = t; }
}
