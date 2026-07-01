package com.hostel.model;

import java.sql.Date;
import java.sql.Timestamp;

public class CanteenServiceLog {
    private int logId, markedBy;
    private Date serviceDate;
    private String status, remarks; // RENDERED / NOT_RENDERED
    private Timestamp markedAt;

    public CanteenServiceLog() {}

    public int getLogId() { return logId; }
    public void setLogId(int l) { this.logId = l; }
    public int getMarkedBy() { return markedBy; }
    public void setMarkedBy(int m) { this.markedBy = m; }
    public Date getServiceDate() { return serviceDate; }
    public void setServiceDate(Date d) { this.serviceDate = d; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String r) { this.remarks = r; }
    public Timestamp getMarkedAt() { return markedAt; }
    public void setMarkedAt(Timestamp t) { this.markedAt = t; }
}
